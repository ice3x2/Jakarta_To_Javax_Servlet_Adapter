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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter6.ServletConverter;

/**
 * IT02 — POST request-body integration test (servlet6 module).
 *
 * <p>Boots a Tomcat 10.1.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and issues a real HTTP POST.
 * Two scenarios validate the F1 boundary (request body stream propagation through the adapter):
 * (1) {@code application/x-www-form-urlencoded} body must surface via
 * {@link HttpServletRequest#getParameter(String)}; (2) {@code Transfer-Encoding: chunked} raw body
 * must be fully readable via {@link HttpServletRequest#getInputStream()}.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT02PostTest {

    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String CONTENT_TYPE_OCTET = "application/octet-stream";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldParseFormUrlencoded() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new FormEchoServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("form", "/form", jakartaServlet);
            runner.start();

            String form = "name=" + URLEncoder.encode("스노우오르카", "UTF-8")
                    + "&value=" + URLEncoder.encode("hello world", "UTF-8");
            byte[] payload = form.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/form").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE_FORM);
            conn.setFixedLengthStreamingMode(payload.length);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }

            int status = conn.getResponseCode();
            String body = new String(readAll(conn.getInputStream()), StandardCharsets.UTF_8);

            assertEquals(200, status, "status code");
            assertEquals("name=스노우오르카;value=hello world", body, "echoed parameters");
        }
    }

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldReadChunkedBody() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new BodyEchoServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("body", "/body", jakartaServlet);
            runner.start();

            byte[] payload = buildPayload(20_000);

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/body").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE_OCTET);
            conn.setChunkedStreamingMode(4096);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }

            int status = conn.getResponseCode();
            String echoedLength = conn.getHeaderField("X-Echoed-Length");
            byte[] echoed = readAll(conn.getInputStream());

            assertEquals(200, status, "status code");
            assertNotNull(echoedLength, "X-Echoed-Length header present");
            assertEquals(String.valueOf(payload.length), echoedLength, "servlet read full body length");
            assertEquals(payload.length, echoed.length, "echoed body byte length matches sent");
            assertTrue(equalBytes(payload, echoed), "echoed body content matches sent");
        }
    }

    private static byte[] buildPayload(int size) {
        byte[] buf = new byte[size];
        for (int i = 0; i < size; i++) {
            buf[i] = (byte) (i % 256);
        }
        return buf;
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
    static final class FormEchoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.setCharacterEncoding("UTF-8");
            String name = req.getParameter("name");
            String value = req.getParameter("value");
            String echoed = "name=" + (name == null ? "" : name) + ";value=" + (value == null ? "" : value);
            byte[] out = echoed.getBytes(StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLength(out.length);
            resp.getOutputStream().write(out);
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class BodyEchoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            resp.setContentType(CONTENT_TYPE_OCTET);
            resp.setHeader("X-Echoed-Length", String.valueOf(body.length));
            resp.setContentLength(body.length);
            resp.getOutputStream().write(body);
        }
    }
}
