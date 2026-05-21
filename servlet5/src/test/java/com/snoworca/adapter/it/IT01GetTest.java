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
 * IT01 — GET basic request/response integration test (servlet5 module).
 *
 * <p>Boots a Tomcat 10.0.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and issues a real HTTP GET.
 * Asserts status, body, {@code Content-Type}, and {@code Content-Length} are propagated
 * end-to-end through the adapter.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT01GetTest {

    private static final String BODY = "hello world";
    private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldReturnBodyAndStatus() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new GetServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("get", "/get", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/get").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            String contentType = conn.getContentType();
            int contentLength = conn.getContentLength();
            byte[] bodyBytes = readAll(conn.getInputStream());
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            assertEquals(200, status, "status code");
            assertEquals(BODY, body, "response body");
            assertNotNull(contentType, "Content-Type header");
            assertTrue(contentType.toLowerCase().startsWith("text/plain"),
                    "Content-Type starts with text/plain, was: " + contentType);
            assertTrue(contentType.toLowerCase().contains("charset=utf-8"),
                    "Content-Type contains charset=utf-8, was: " + contentType);
            assertEquals(BODY.getBytes(StandardCharsets.UTF_8).length, contentLength,
                    "Content-Length matches body byte length");
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
    static final class GetServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            byte[] payload = BODY.getBytes(StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType(CONTENT_TYPE);
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
