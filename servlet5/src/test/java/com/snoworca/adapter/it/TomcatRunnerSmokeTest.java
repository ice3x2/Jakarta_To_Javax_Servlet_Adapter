package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Smoke test for {@link TomcatRunner} (servlet5 module).
 *
 * <p>Verifies the runner boots a Tomcat instance, accepts a registered servlet,
 * serves a request, and shuts down cleanly via {@link AutoCloseable#close()}.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class TomcatRunnerSmokeTest {

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldBootAndShutdownCleanly() throws Exception {
        int boundPort;
        try (TomcatRunner runner = new TomcatRunner()) {
            runner.addServlet("hello", "/hello", new HelloServlet());
            runner.start();
            boundPort = runner.port();
            assertTrue(boundPort > 0, "port should be assigned");
            assertNotNull(runner.baseUrl(), "baseUrl should be non-null");

            String body = httpGet(runner.baseUrl() + "/hello");
            assertEquals("ok", body);
        }
        // After close: port must be released (we can rebind it).
        assertTrue(isPortFree(boundPort), "port should be free after shutdown");
    }

    private static String httpGet(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return r.lines().collect(Collectors.joining("\n"));
        }
    }

    private static boolean isPortFree(int port) {
        try (java.net.ServerSocket s = new java.net.ServerSocket(port)) {
            s.setReuseAddress(true);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    static final class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().write("ok");
        }
    }
}
