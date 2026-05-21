package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import adapter.servletElementConverter5.ServletConverter;

/**
 * IT13 — ErrorPage dispatch integration test (servlet5 module).
 *
 * <p>Boots a Tomcat 10.0.x instance via {@link TomcatRunner}, registers two
 * {@code javax.servlet} {@link HttpServlet}s bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}: (1) a "boom" servlet that throws
 * {@link NullPointerException} from {@code doGet}, and (2) an error-handler servlet mapped to
 * {@code /error}. The Tomcat embed Context is configured with an
 * {@link org.apache.tomcat.util.descriptor.web.ErrorPage} mapping the exception FQN to
 * {@code /error}.
 *
 * <p>The end-to-end contract under test: when a javax-surface servlet (wrapped by the adapter)
 * throws a runtime exception inside Tomcat's request dispatch loop, the container must (a)
 * resolve the ErrorPage mapping for the exception type, (b) forward the request to the configured
 * location, and (c) the error-handler servlet's body + status must reach the wire. This proves
 * the adapter does NOT swallow exceptions thrown by the wrapped servlet — Tomcat's ErrorPage
 * machinery still observes the exception type and routes the dispatch to the configured handler.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT13ErrorPageTest {

    private static final String ERROR_BODY_MARKER = "ERROR-PAGE-HANDLED";
    private static final int ERROR_STATUS = 500;

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldMapErrorPage() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxBoom = new BoomServlet();
            javax.servlet.Servlet javaxError = new ErrorHandlerServlet();
            jakarta.servlet.Servlet jakartaBoom = ServletConverter.convert(javaxBoom);
            jakarta.servlet.Servlet jakartaError = ServletConverter.convert(javaxError);

            runner.addServlet("boom", "/boom", jakartaBoom);
            runner.addServlet("error", "/error", jakartaError);
            runner.addErrorPage(NullPointerException.class.getName(), "/error");
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/boom").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try {
                int status = conn.getResponseCode();
                // Tomcat dispatches the ErrorPage forward and the handler writes status 500
                // explicitly. Verify the status surfaces on the wire.
                assertEquals(ERROR_STATUS, status, "ErrorPage handler must set status 500");

                // The error stream carries the handler servlet body (HttpURLConnection routes
                // bodies of >=400 status through getErrorStream()).
                InputStream bodyStream = conn.getErrorStream();
                assertNotNull(bodyStream, "error stream must be present for 500 response");
                byte[] body = readAll(bodyStream);
                String bodyText = new String(body, StandardCharsets.UTF_8);
                assertTrue(bodyText.contains(ERROR_BODY_MARKER),
                        "ErrorPage forward must dispatch to /error handler — expected body to contain '"
                                + ERROR_BODY_MARKER + "', got: " + bodyText);
            } finally {
                conn.disconnect();
            }
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
    static final class BoomServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            // Throw a runtime exception to trigger ErrorPage dispatch. The adapter must
            // propagate the exception to Tomcat — not swallow it — so the container's
            // ErrorPage machinery can resolve the exception-type mapping and forward.
            throw new NullPointerException("boom");
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class ErrorHandlerServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            // ErrorPage forward target. Set 500 explicitly + body marker so the assertion
            // can confirm the forward fired (vs. raw Tomcat 500 page which contains a
            // different marker).
            resp.setStatus(ERROR_STATUS);
            resp.setContentType("text/plain; charset=UTF-8");
            byte[] payload = ERROR_BODY_MARKER.getBytes(StandardCharsets.UTF_8);
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
