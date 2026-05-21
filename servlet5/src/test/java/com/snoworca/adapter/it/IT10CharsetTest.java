package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter5.ServletConverter;

/**
 * IT10 — Charset UTF-8 round-trip + Content-Type charset fallback integration test (servlet5 module).
 *
 * <p>Boots a Tomcat 10.0.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and issues real HTTP POST requests
 * carrying Korean UTF-8 bodies. Two scenarios validate the F3 boundary (charset propagation
 * through the adapter):
 * (1) explicit {@code Content-Type: text/plain; charset=UTF-8} round-trip preserves Korean
 * characters byte-for-byte after javax→jakarta request bridging and jakarta→javax response
 * bridging; (2) requests without an explicit {@code charset} parameter on Content-Type still
 * surface readable bytes via {@link HttpServletRequest#getInputStream()} — the servlet decodes
 * the raw bytes explicitly as UTF-8, verifying the adapter does not corrupt the body.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT10CharsetTest {

    private static final String KOREAN_PAYLOAD = "안녕하세요, 스노우오르카!";
    private static final String CONTENT_TYPE_WITH_CHARSET = "text/plain; charset=UTF-8";
    private static final String CONTENT_TYPE_NO_CHARSET = "text/plain";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldRoundTripUtf8() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new CharsetEchoServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("charset", "/charset", jakartaServlet);
            runner.start();

            byte[] payload = KOREAN_PAYLOAD.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/charset").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE_WITH_CHARSET);
            conn.setFixedLengthStreamingMode(payload.length);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }

            int status = conn.getResponseCode();
            String responseContentType = conn.getHeaderField("Content-Type");
            byte[] echoedBytes = readAll(conn.getInputStream());
            String echoed = new String(echoedBytes, StandardCharsets.UTF_8);

            assertEquals(200, status, "status code");
            assertNotNull(responseContentType, "response Content-Type present");
            assertTrue(responseContentType.toLowerCase().contains("charset=utf-8"),
                    "response Content-Type advertises UTF-8 charset, got: " + responseContentType);
            assertEquals(KOREAN_PAYLOAD, echoed, "UTF-8 round-trip preserves Korean characters");
        }
    }

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldFallbackCharset() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new RawBytesEchoServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("raw", "/raw", jakartaServlet);
            runner.start();

            byte[] payload = KOREAN_PAYLOAD.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/raw").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            // No charset parameter — servlet must fall back to byte-level read and decode explicitly.
            conn.setRequestProperty("Content-Type", CONTENT_TYPE_NO_CHARSET);
            conn.setFixedLengthStreamingMode(payload.length);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }

            int status = conn.getResponseCode();
            String observedCharset = conn.getHeaderField("X-Observed-Charset");
            byte[] echoedBytes = readAll(conn.getInputStream());

            assertEquals(200, status, "status code");
            assertNotNull(observedCharset, "X-Observed-Charset header present");
            // When client omits charset, servlet API returns null (or container default) per Servlet spec.
            // Adapter must not fabricate a charset that corrupts the payload — servlet reads raw bytes.
            assertEquals(payload.length, echoedBytes.length, "raw byte length preserved through adapter");
            assertTrue(equalBytes(payload, echoedBytes), "raw bytes preserved byte-for-byte (no charset-induced corruption)");
        }
    }

    private static boolean equalBytes(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * @req FR-TEST-003
     */
    static final class CharsetEchoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.setCharacterEncoding("UTF-8");
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            try (ServletInputStream in = req.getInputStream()) {
                byte[] buf = new byte[1024];
                int n;
                while ((n = in.read(buf)) != -1) {
                    sink.write(buf, 0, n);
                }
            }
            String decoded = new String(sink.toByteArray(), StandardCharsets.UTF_8);
            byte[] out = decoded.getBytes(StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLength(out.length);
            resp.getOutputStream().write(out);
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class RawBytesEchoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String observedCharset = req.getCharacterEncoding();
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            try (ServletInputStream in = req.getInputStream()) {
                byte[] buf = new byte[1024];
                int n;
                while ((n = in.read(buf)) != -1) {
                    sink.write(buf, 0, n);
                }
            }
            byte[] body = sink.toByteArray();
            resp.setStatus(200);
            resp.setContentType("application/octet-stream");
            // Surface what the servlet observed for request charset (null when client omitted it).
            resp.setHeader("X-Observed-Charset", observedCharset == null ? "null" : observedCharset);
            resp.setContentLength(body.length);
            resp.getOutputStream().write(body);
        }
    }
}
