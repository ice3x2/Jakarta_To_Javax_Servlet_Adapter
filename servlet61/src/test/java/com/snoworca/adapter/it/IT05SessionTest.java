package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter61.ServletConverter;

/**
 * IT05 — HttpSession round-trip integration test (servlet61 module).
 *
 * <p>Boots Tomcat 11.0.x via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and exercises the
 * session lifecycle through three real HTTP requests carrying {@code JSESSIONID}:
 * (1) create session + set attribute, (2) retrieve attribute with the same
 * session cookie, (3) invalidate + re-issue session and verify a new id.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT05SessionTest {

    private static final String ATTR_NAME = "userId";
    private static final String ATTR_VALUE = "alice-42";
    private static final String JSESSIONID = "JSESSIONID";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldRoundTripSession() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new SessionServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("session", "/session", jakartaServlet);
            runner.start();

            // (1) First request: create session, set attribute
            HttpURLConnection setConn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/session?op=set").openConnection();
            setConn.setRequestMethod("GET");
            setConn.setConnectTimeout(5000);
            setConn.setReadTimeout(5000);

            assertEquals(200, setConn.getResponseCode(), "set status");
            String firstSessionId = new String(readAll(setConn.getInputStream()), StandardCharsets.UTF_8);
            assertNotNull(firstSessionId, "first session id body");
            assertTrue(firstSessionId.length() > 0, "first session id non-empty");

            String firstCookie = extractJsessionIdCookie(setConn.getHeaderFields());
            assertNotNull(firstCookie, "JSESSIONID Set-Cookie on first response");

            // (2) Second request: send JSESSIONID back, expect attribute value
            HttpURLConnection getConn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/session?op=get").openConnection();
            getConn.setRequestMethod("GET");
            getConn.setRequestProperty("Cookie", JSESSIONID + "=" + firstCookie);
            getConn.setConnectTimeout(5000);
            getConn.setReadTimeout(5000);

            assertEquals(200, getConn.getResponseCode(), "get status");
            String attrEcho = new String(readAll(getConn.getInputStream()), StandardCharsets.UTF_8);
            assertEquals(ATTR_VALUE, attrEcho, "attribute value echoed");

            // (3) Third request: invalidate prior session, create new one
            HttpURLConnection invConn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/session?op=invalidate").openConnection();
            invConn.setRequestMethod("GET");
            invConn.setRequestProperty("Cookie", JSESSIONID + "=" + firstCookie);
            invConn.setConnectTimeout(5000);
            invConn.setReadTimeout(5000);

            assertEquals(200, invConn.getResponseCode(), "invalidate status");
            String newSessionId = new String(readAll(invConn.getInputStream()), StandardCharsets.UTF_8);
            assertNotNull(newSessionId, "new session id body");
            assertTrue(newSessionId.length() > 0, "new session id non-empty");
            assertNotEquals(firstSessionId, newSessionId,
                    "new session id must differ from invalidated session id");
        }
    }

    private static String extractJsessionIdCookie(Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String key = e.getKey();
            if (key == null || !"Set-Cookie".equalsIgnoreCase(key)) {
                continue;
            }
            for (String value : e.getValue()) {
                if (value == null) {
                    continue;
                }
                if (value.startsWith(JSESSIONID + "=")) {
                    int eq = JSESSIONID.length() + 1;
                    int semi = value.indexOf(';', eq);
                    return (semi < 0) ? value.substring(eq) : value.substring(eq, semi);
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
    static final class SessionServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String op = req.getParameter("op");
            String body;
            if ("set".equals(op)) {
                HttpSession session = req.getSession(true);
                session.setAttribute(ATTR_NAME, ATTR_VALUE);
                body = session.getId();
            } else if ("get".equals(op)) {
                HttpSession session = req.getSession(false);
                Object value = (session == null) ? null : session.getAttribute(ATTR_NAME);
                body = (value == null) ? "" : value.toString();
            } else if ("invalidate".equals(op)) {
                HttpSession existing = req.getSession(false);
                if (existing != null) {
                    existing.invalidate();
                }
                HttpSession fresh = req.getSession(true);
                body = fresh.getId();
            } else {
                body = "";
            }
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
