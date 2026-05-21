package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
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
 * IT09 — HTTP 308 Permanent Redirect integration test (servlet61 module only).
 *
 * <p>Boots Tomcat 11.0.x via {@link TomcatRunner} and registers a {@code javax.servlet}
 * {@link HttpServlet} bridged through {@link ServletConverter#convert(javax.servlet.Servlet)}.
 * The servlet emits a 308 Permanent Redirect by setting status 308 and a {@code Location}
 * header — exercising the jakarta 6.1 redirect path end-to-end through the adapter (jakarta
 * → javax) where the Servlet 6.1 {@code sendRedirect(String, int, boolean)} 3-arg overload
 * on the jakarta side delegates to {@code setStatus} + {@code setHeader("Location", ...)}
 * + optional {@code resetBuffer} + {@code flushBuffer} (SRS §6.2.2 policy E, FR-CONV-014).
 *
 * <p>This test is servlet61-only: jakarta 5.0 / 6.0 do not expose the 3-arg
 * {@code sendRedirect} overload, so the scenario is not applicable to servlet5 / servlet6
 * modules.
 *
 * <p>DoD (T-PH002-09): response status 308 + Location header value matches the configured
 * target URL exactly.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT09Redirect308Test {

    private static final String TARGET_LOCATION = "https://example.com/new-path";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldRedirect308() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new Redirect308Servlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("redirect308", "/redirect308", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/redirect308").openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            String location = conn.getHeaderField("Location");

            assertEquals(308, status, "response status must be 308 Permanent Redirect");
            assertNotNull(location, "Location header must be present on 308 response");
            assertEquals(TARGET_LOCATION, location,
                    "Location header must match the configured redirect target exactly");

            conn.disconnect();
        }
    }

    /**
     * javax-side servlet emitting HTTP 308 by setting status + Location header. The adapter
     * bridges these calls onto the jakarta-side response; Tomcat 11.0.x then serializes the
     * 308 status line and Location header onto the wire.
     *
     * @req FR-TEST-003
     */
    static final class Redirect308Servlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            resp.setStatus(308);
            resp.setHeader("Location", TARGET_LOCATION);
        }
    }
}
