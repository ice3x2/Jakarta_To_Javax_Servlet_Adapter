package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter61.ServletConverter;

/**
 * IT12 — {@code setContentLengthLong(long)} F5/F6 boundary integration test (servlet61 module).
 *
 * <p>Boots a Tomcat 11.0.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and exercises end-to-end propagation
 * of {@code setContentLengthLong(long)} through the adapter bridge to Tomcat's wire output.
 *
 * <p>Per SRS FR-FIX-003 (D3 in the legacy defect catalogue), the original adapter narrowed
 * {@code setContentLengthLong(long)} to {@code (int)long} silently corrupting payload sizes ≥ 2GB
 * (typical for large file downloads). After the FR-FIX-003 fix the adapter delegates to the
 * native {@code setContentLengthLong(long)} method on both javax→jakarta and jakarta→javax sides.
 *
 * <p>Integration-level verification of the F5/F6 boundary value (≥ Integer.MAX_VALUE) is
 * structurally infeasible with embedded Tomcat: Tomcat overwrites the Content-Length header on
 * commit to match the actual body byte count, and streaming 3 GB through a unit-test HTTP request
 * is impractical. The deep narrowing-bug verification therefore lives at the unit level
 * (see {@code BugMuseumTest#BM05} which mocks the underlying response and verifies the
 * adapter calls {@code origin.setContentLengthLong(Long.MAX_VALUE)} — never narrowing to int).
 *
 * <p>This integration test verifies the orthogonal end-to-end contract:
 * (1) {@code shouldEmitLongContentLength} — calling the {@code long} overload with a value that
 *     fits in {@code int} but exercises the long code path produces a Content-Length header on
 *     the wire that exactly matches the body byte count, proving the long delegation path is
 *     plumbed end-to-end (servlet → adapter → Tomcat → wire).
 * (2) {@code shouldResolveConflict} — calling {@code setContentLengthLong(LONG_VAL)} then
 *     {@code setContentLength(SHORT_VAL)} on the same response, with the body matching the final
 *     (SHORT) value, produces Content-Length=SHORT_VAL on the wire, proving the two overloads
 *     interoperate consistently and the last-write-wins replacement semantics propagate.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT12ContentLengthLongTest {

    private static final long LONG_BODY_LENGTH = 1024L;
    private static final int SHORT_BODY_LENGTH = 42;

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldEmitLongContentLength() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new LongContentLengthServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("clLong", "/clLong", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/clLong").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try {
                int status = conn.getResponseCode();
                String contentLength = conn.getHeaderField("Content-Length");
                long contentLengthLong = conn.getContentLengthLong();

                assertEquals(200, status, "GET request returns 200");
                assertNotNull(contentLength, "Content-Length header must be present on the wire");
                // setContentLengthLong(1024L) → Tomcat preserves the value (matches actual body)
                // → wire carries Content-Length: 1024. Any future regression that re-introduces
                // (int) narrowing on the long path would still produce 1024 here (1024 fits in
                // int), but the call signature path is exercised, asserting the long overload is
                // routed end-to-end. The Long.MAX_VALUE narrowing case is covered by BM05 unit
                // test which mocks the origin response.
                assertEquals(Long.toString(LONG_BODY_LENGTH), contentLength,
                        "Content-Length header on wire must equal the long value set by the servlet");
                assertEquals(LONG_BODY_LENGTH, contentLengthLong,
                        "HttpURLConnection.getContentLengthLong() must read the long value");
            } finally {
                conn.disconnect();
            }
        }
    }

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldResolveConflict() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new ConflictingContentLengthServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("clConflict", "/clConflict", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/clConflict").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try {
                int status = conn.getResponseCode();
                String contentLength = conn.getHeaderField("Content-Length");

                assertEquals(200, status, "GET request returns 200");
                assertNotNull(contentLength, "Content-Length header must be present on the wire");
                // Per HttpServletResponse contract, setContentLength* calls replace (not append).
                // The servlet calls setContentLengthLong(LONG_BODY_LENGTH=1024) first, then
                // setContentLength(SHORT_BODY_LENGTH=42), then writes 42 bytes of body. The
                // adapter must forward both calls consistently so the underlying response
                // observes last-write-wins semantics — Content-Length on the wire must equal
                // the final (SHORT) value, not the LONG value.
                assertEquals(Integer.toString(SHORT_BODY_LENGTH), contentLength,
                        "last setContentLength* call wins — short int value must override prior long value");
            } finally {
                conn.disconnect();
            }
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class LongContentLengthServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            // Exercise the long overload end-to-end. The body byte count matches the set value
            // so Tomcat does not rewrite the Content-Length header.
            byte[] payload = new byte[(int) LONG_BODY_LENGTH];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) 'A';
            }
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLengthLong(LONG_BODY_LENGTH);
            try (OutputStream out = resp.getOutputStream()) {
                out.write(payload);
            }
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class ConflictingContentLengthServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            // First a long value, then an int value. HttpServletResponse contract: last call wins
            // (subsequent calls replace, not append). Body matches the final (SHORT) value.
            byte[] payload = new byte[SHORT_BODY_LENGTH];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) 'B';
            }
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLengthLong(LONG_BODY_LENGTH);
            resp.setContentLength(SHORT_BODY_LENGTH);
            try (OutputStream out = resp.getOutputStream()) {
                out.write(payload);
            }
        }
    }
}
