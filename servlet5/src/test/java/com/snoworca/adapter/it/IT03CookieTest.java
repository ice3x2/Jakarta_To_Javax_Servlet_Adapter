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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter5.ServletConverter;

/**
 * IT03 — Cookie round-trip integration test (servlet5 module).
 *
 * <p>Boots a Tomcat 10.0.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and issues two real HTTP requests:
 * (1) a GET that triggers {@code HttpServletResponse#addCookie} with six attributes
 * (name, value, path, domain, maxAge, secure, httpOnly) and asserts every attribute is emitted
 * in the {@code Set-Cookie} response header, and (2) a follow-up GET that sends the cookie
 * back via the {@code Cookie} request header so the servlet observes the name/value pair via
 * {@link HttpServletRequest#getCookies()}.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT03CookieTest {

    private static final String COOKIE_NAME = "sid";
    private static final String COOKIE_VALUE = "abc123";
    private static final String COOKIE_PATH = "/";
    private static final String COOKIE_DOMAIN = "localhost";
    private static final int COOKIE_MAX_AGE = 3600;

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldRoundTripCookieAttributes() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new CookieServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("cookie", "/cookie", jakartaServlet);
            runner.start();

            // (1) First request: server sets cookie with 6 attributes
            HttpURLConnection setConn = (HttpURLConnection) new URL(runner.baseUrl() + "/cookie?op=set").openConnection();
            setConn.setRequestMethod("GET");
            setConn.setConnectTimeout(5000);
            setConn.setReadTimeout(5000);

            int setStatus = setConn.getResponseCode();
            assertEquals(200, setStatus, "set status");

            String setCookie = findSetCookieHeader(setConn.getHeaderFields(), COOKIE_NAME);
            assertNotNull(setCookie, "Set-Cookie header for " + COOKIE_NAME + " present");

            String setCookieLower = setCookie.toLowerCase(Locale.ROOT);

            // 6 attributes asserted: name, value, path, domain, max-age, secure, http-only
            assertTrue(setCookie.startsWith(COOKIE_NAME + "=" + COOKIE_VALUE),
                    "Set-Cookie starts with name=value, was: " + setCookie);
            assertTrue(setCookieLower.contains("path=" + COOKIE_PATH),
                    "Set-Cookie contains path=" + COOKIE_PATH + ", was: " + setCookie);
            assertTrue(setCookieLower.contains("domain=" + COOKIE_DOMAIN),
                    "Set-Cookie contains domain=" + COOKIE_DOMAIN + ", was: " + setCookie);
            assertTrue(setCookieLower.contains("max-age=" + COOKIE_MAX_AGE),
                    "Set-Cookie contains max-age=" + COOKIE_MAX_AGE + ", was: " + setCookie);
            assertTrue(setCookieLower.contains("secure"),
                    "Set-Cookie contains Secure, was: " + setCookie);
            assertTrue(setCookieLower.contains("httponly"),
                    "Set-Cookie contains HttpOnly, was: " + setCookie);
            // discard response body to release the connection
            readAll(setConn.getInputStream());

            // (2) Second request: client sends Cookie back, servlet echoes name=value
            HttpURLConnection echoConn = (HttpURLConnection) new URL(runner.baseUrl() + "/cookie?op=echo").openConnection();
            echoConn.setRequestMethod("GET");
            echoConn.setRequestProperty("Cookie", COOKIE_NAME + "=" + COOKIE_VALUE);
            echoConn.setConnectTimeout(5000);
            echoConn.setReadTimeout(5000);

            int echoStatus = echoConn.getResponseCode();
            String echoed = new String(readAll(echoConn.getInputStream()), StandardCharsets.UTF_8);

            assertEquals(200, echoStatus, "echo status");
            assertEquals(COOKIE_NAME + "=" + COOKIE_VALUE, echoed, "echoed cookie name=value");
        }
    }

    private static String findSetCookieHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            if (!"Set-Cookie".equalsIgnoreCase(key)) {
                continue;
            }
            for (String value : e.getValue()) {
                if (value != null && value.startsWith(name + "=")) {
                    return value;
                }
            }
        }
        return null;
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
    static final class CookieServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String op = req.getParameter("op");
            if ("set".equals(op)) {
                Cookie c = new Cookie(COOKIE_NAME, COOKIE_VALUE);
                c.setPath(COOKIE_PATH);
                c.setDomain(COOKIE_DOMAIN);
                c.setMaxAge(COOKIE_MAX_AGE);
                c.setSecure(true);
                c.setHttpOnly(true);
                resp.addCookie(c);
                resp.setStatus(200);
                resp.setContentType("text/plain; charset=UTF-8");
                byte[] payload = "ok".getBytes(StandardCharsets.UTF_8);
                resp.setContentLength(payload.length);
                resp.getOutputStream().write(payload);
                return;
            }

            // echo: serialize first cookie that matches COOKIE_NAME
            Cookie[] cookies = req.getCookies();
            String body = "";
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (COOKIE_NAME.equals(c.getName())) {
                        body = c.getName() + "=" + c.getValue();
                        break;
                    }
                }
            }
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
