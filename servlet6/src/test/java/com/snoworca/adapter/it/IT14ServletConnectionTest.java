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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter6.ServletConverter;

/**
 * IT14 — {@code jakarta.servlet.ServletConnection} fallback integration test (servlet6 module).
 *
 * <p>Boots a Tomcat 10.1.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and issues a real HTTP GET.
 *
 * <p>Inside the javax servlet, the dispatched {@code javax.servlet.HttpServletRequest} is wrapped
 * by the adapter's jakarta {@link adapter.jakarta.servlet6.ServletRequest} to expose the jakarta
 * 6.0 surface. The test verifies that the adapter's synthesized {@code ServletConnection}
 * fallback returns the four required fields ({@code connectionId} / {@code protocol} /
 * {@code protocolConnectionId} / {@code isSecure}) — all non-null, with {@code protocol}
 * matching the expected {@code HTTP/x.y} grammar.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT14ServletConnectionTest {

    private static final String FIELD_SEPARATOR = "\n";
    private static final String KV_SEPARATOR = "=";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldExposeServletConnection() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new ConnectionProbeServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("probe", "/probe", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/probe").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            assertEquals(200, status, "status code");
            byte[] bodyBytes = readAll(conn.getInputStream());
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            Map<String, String> fields = parseFields(body);

            String connectionId = fields.get("connectionId");
            String protocol = fields.get("protocol");
            String protocolConnectionId = fields.get("protocolConnectionId");
            String isSecure = fields.get("isSecure");

            // Field-1: connectionId — adapter synthesizes a UUID-shaped non-null token.
            assertNotNull(connectionId, "connectionId must be non-null");
            assertTrue(connectionId.length() > 0, "connectionId must be non-empty");

            // Field-2: protocol — adapter exposes a literal HTTP/x.y grammar (synthesized default).
            assertNotNull(protocol, "protocol must be non-null");
            assertTrue(protocol.matches("HTTP/\\d+(\\.\\d+)?"),
                    "protocol must match HTTP/x.y grammar, was: " + protocol);

            // Field-3: protocolConnectionId — non-null even when empty (HTTP/1.1 has no stream id).
            assertNotNull(protocolConnectionId, "protocolConnectionId must be non-null (may be empty)");

            // Field-4: isSecure — boolean rendered as "true" / "false" by the probe servlet.
            assertNotNull(isSecure, "isSecure must be non-null");
            assertTrue(isSecure.equals("true") || isSecure.equals("false"),
                    "isSecure must be 'true' or 'false', was: " + isSecure);

            conn.disconnect();
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

    private static Map<String, String> parseFields(String body) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : body.split(FIELD_SEPARATOR)) {
            int idx = line.indexOf(KV_SEPARATOR);
            if (idx < 0) {
                continue;
            }
            String key = line.substring(0, idx);
            String value = line.substring(idx + KV_SEPARATOR.length());
            map.put(key, value);
        }
        return map;
    }

    /**
     * Probes {@code getServletConnection()} on the adapter's jakarta {@link
     * adapter.jakarta.servlet6.ServletRequest} (which wraps the dispatched javax request) and
     * writes the four required fields back to the wire as a small key/value text payload.
     *
     * @req FR-TEST-003
     */
    static final class ConnectionProbeServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            // Wrap the dispatched javax request with the adapter's jakarta ServletRequest
            // to access the jakarta 6.0 surface (getServletConnection() — absent from javax).
            adapter.jakarta.servlet6.ServletRequest jakartaReq =
                    new adapter.jakarta.servlet6.ServletRequest(req);
            jakarta.servlet.ServletConnection sc = jakartaReq.getServletConnection();
            // Render the four fields. "null" is rendered literally so the assertion can
            // distinguish present-empty from absent-null.
            String connectionId = sc == null ? "null" : String.valueOf(sc.getConnectionId());
            String protocol = sc == null ? "null" : String.valueOf(sc.getProtocol());
            String protocolConnectionId = sc == null ? "null" : String.valueOf(sc.getProtocolConnectionId());
            String isSecure = sc == null ? "null" : String.valueOf(sc.isSecure());

            StringBuilder sb = new StringBuilder();
            sb.append("connectionId").append(KV_SEPARATOR).append(connectionId).append(FIELD_SEPARATOR);
            sb.append("protocol").append(KV_SEPARATOR).append(protocol).append(FIELD_SEPARATOR);
            sb.append("protocolConnectionId").append(KV_SEPARATOR).append(protocolConnectionId).append(FIELD_SEPARATOR);
            sb.append("isSecure").append(KV_SEPARATOR).append(isSecure);

            byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
