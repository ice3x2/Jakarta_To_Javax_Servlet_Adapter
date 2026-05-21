package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter61.ServletConverter;

/**
 * IT11 — {@code setStatus(int, String)} deprecated reason-phrase fallback integration test
 * (servlet61 module).
 *
 * <p>Boots a Tomcat 11.0.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and exercises both setStatus overloads
 * on the javax surface. The jakarta side ({@code jakarta.servlet 6.1}) has REMOVED
 * {@code setStatus(int, String)} entirely (SRS §6.2.2 policy E1) — adapter forwards to
 * {@code setStatus(int)} only, dropping the reason phrase silently. Two scenarios:
 * (1) two-argument {@code setStatus(int, String)} — adapter forwards status code but the reason
 *     phrase MUST NOT surface on the HTTP status line (jakarta 6.1 removed the API; Tomcat 11
 *     emits the standard reason phrase regardless);
 * (2) one-argument {@code setStatus(int)} — adapter forwards status code as-is.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT11SetStatusTest {

    private static final int STATUS_CREATED = 201;
    private static final int STATUS_ACCEPTED = 202;
    private static final String CUSTOM_REASON = "Custom-Brew-Phrase";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldIgnoreReasonPhrase() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new TwoArgSetStatusServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("setstatus2", "/setstatus2", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/setstatus2").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            byte[] body = readAll(conn.getInputStream());
            String bodyText = new String(body, StandardCharsets.UTF_8);

            assertEquals(STATUS_CREATED, status, "status code propagated through deprecated 2-arg setStatus");
            // jakarta.servlet 6.1 REMOVED setStatus(int, String); adapter (SRS §6.2.2 E1) forwards
            // to setStatus(int) only. Tomcat 11 emits the standard reason phrase or omits it
            // entirely (HttpURLConnection.getResponseMessage() then returns null). Both outcomes
            // prove the application-supplied custom reason MUST NOT appear on the wire.
            if (responseMessage != null && responseMessage.contains(CUSTOM_REASON)) {
                throw new AssertionError(
                        "deprecated 2-arg setStatus(int, String) must NOT surface custom reason phrase '"
                                + CUSTOM_REASON + "' on HTTP status line, got: " + responseMessage);
            }
            assertEquals("two-arg-invoked", bodyText, "servlet body marker confirms 2-arg path executed");
        }
    }

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldSetStatusCode() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new OneArgSetStatusServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("setstatus1", "/setstatus1", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/setstatus1").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            byte[] body = readAll(conn.getInputStream());
            String bodyText = new String(body, StandardCharsets.UTF_8);

            assertEquals(STATUS_ACCEPTED, status, "status code propagated through 1-arg setStatus");
            assertEquals("one-arg-invoked", bodyText, "servlet body marker confirms 1-arg path executed");
        }
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
    static final class TwoArgSetStatusServlet extends HttpServlet {
        @Override
        @SuppressWarnings("deprecation")
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            resp.setStatus(STATUS_CREATED, CUSTOM_REASON);
            resp.setContentType("text/plain; charset=UTF-8");
            byte[] payload = "two-arg-invoked".getBytes(StandardCharsets.UTF_8);
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class OneArgSetStatusServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            resp.setStatus(STATUS_ACCEPTED);
            resp.setContentType("text/plain; charset=UTF-8");
            byte[] payload = "one-arg-invoked".getBytes(StandardCharsets.UTF_8);
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
