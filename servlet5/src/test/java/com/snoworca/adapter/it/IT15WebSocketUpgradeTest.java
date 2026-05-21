package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter5.ServletConverter;

/**
 * IT15 — HTTP/1.1 Upgrade mechanism integration test (servlet5 module).
 *
 * <p>Boots a Tomcat 10.0.x instance via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged to {@code jakarta.servlet} through
 * {@link ServletConverter#convert(javax.servlet.Servlet)}, and issues a raw HTTP/1.1
 * {@code Connection: Upgrade} handshake. The javax servlet sets status 101 + {@code Upgrade} /
 * {@code Connection} response headers and invokes
 * {@link HttpServletRequest#upgrade(Class)} with a mock {@link HttpUpgradeHandler}.
 *
 * <p>The test verifies only the HTTP/1.1 upgrade <em>handshake</em> mechanics (101 response +
 * upgrade headers) — the WebSocket protocol itself (RFC 6455 frame layer) is intentionally out of
 * scope. A raw {@link Socket} is used rather than {@link java.net.HttpURLConnection} because the
 * latter does not surface 101 responses to the caller (Java's URL stack treats 1xx as an
 * intermediate / non-final status and may swallow it).
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT15WebSocketUpgradeTest {

    private static final String UPGRADE_PROTOCOL = "websocket";
    private static final String SEC_WS_KEY = "dGhlIHNhbXBsZSBub25jZQ==";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldUpgradeHttp() throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new UpgradeServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("upgrade", "/upgrade", jakartaServlet);
            runner.start();

            String request =
                    "GET /upgrade HTTP/1.1\r\n"
                            + "Host: localhost:" + runner.port() + "\r\n"
                            + "Upgrade: " + UPGRADE_PROTOCOL + "\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Sec-WebSocket-Key: " + SEC_WS_KEY + "\r\n"
                            + "Sec-WebSocket-Version: 13\r\n"
                            + "\r\n";

            try (Socket socket = new Socket("localhost", runner.port())) {
                socket.setSoTimeout(5000);
                OutputStream out = socket.getOutputStream();
                out.write(request.getBytes(StandardCharsets.ISO_8859_1));
                out.flush();

                ParsedResponse response = parseResponse(socket.getInputStream());

                // Status line: HTTP/1.1 101 Switching Protocols (reason phrase may vary per impl).
                assertEquals(101, response.statusCode,
                        "upgrade handshake must yield HTTP 101 status, got: " + response.statusLine);

                // Connection: Upgrade header (case-insensitive lookup, value must contain 'upgrade').
                String connection = response.header("Connection");
                assertNotNull(connection, "response must carry 'Connection' header for upgrade");
                assertTrue(connection.toLowerCase().contains("upgrade"),
                        "Connection header must contain 'upgrade' token, got: " + connection);

                // Upgrade: websocket header.
                String upgrade = response.header("Upgrade");
                assertNotNull(upgrade, "response must carry 'Upgrade' header");
                assertEquals(UPGRADE_PROTOCOL, upgrade.trim().toLowerCase(),
                        "Upgrade header must echo the requested protocol token");
            }
        }
    }

    /**
     * Parses the HTTP status line + headers from the raw socket stream. Body bytes (if any) are
     * left in the stream; this test does not consume them.
     *
     * @req FR-TEST-003
     */
    private static ParsedResponse parseResponse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        String statusLine = reader.readLine();
        if (statusLine == null) {
            throw new IOException("server closed connection before status line");
        }
        // Status line format: HTTP/1.1 <code> <reason>
        String[] parts = statusLine.split(" ", 3);
        if (parts.length < 2) {
            throw new IOException("malformed status line: " + statusLine);
        }
        int statusCode = Integer.parseInt(parts[1]);

        Map<String, String> headers = new LinkedHashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                headers.put(name.toLowerCase(), value);
            }
        }
        return new ParsedResponse(statusLine, statusCode, headers);
    }

    /**
     * @req FR-TEST-003
     */
    private static final class ParsedResponse {
        final String statusLine;
        final int statusCode;
        final Map<String, String> headers;

        ParsedResponse(String statusLine, int statusCode, Map<String, String> headers) {
            this.statusLine = statusLine;
            this.statusCode = statusCode;
            this.headers = headers;
        }

        String header(String name) {
            return headers.get(name.toLowerCase());
        }
    }

    /**
     * javax servlet that performs the HTTP/1.1 upgrade handshake. Sets response status 101 and the
     * {@code Upgrade} / {@code Connection} response headers, then calls
     * {@link HttpServletRequest#upgrade(Class)} so Tomcat completes the protocol switch.
     *
     * @req FR-TEST-003
     */
    static final class UpgradeServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            // Validate the upgrade request before flipping the connection. Tomcat won't reject the
            // request on our behalf for arbitrary upgrade tokens — the servlet owns the policy.
            String upgrade = req.getHeader("Upgrade");
            String connection = req.getHeader("Connection");
            if (upgrade == null || connection == null
                    || !connection.toLowerCase().contains("upgrade")) {
                resp.sendError(400, "Missing Upgrade/Connection headers");
                return;
            }

            // 101 Switching Protocols + advertise the new protocol on the response. Per Servlet 4
            // and RFC 7230 §6.7, the server signals the switch with status 101 plus the
            // Upgrade/Connection headers; the upgrade handler then takes over the connection.
            resp.setStatus(101);
            resp.setHeader("Upgrade", upgrade);
            resp.setHeader("Connection", "Upgrade");
            req.upgrade(NoopUpgradeHandler.class);
        }
    }

    /**
     * Minimal {@link HttpUpgradeHandler} that does nothing — the upgrade handshake mechanics are
     * the unit under test, not the post-upgrade protocol traffic. {@code init()} immediately
     * yields without reading from / writing to the {@link WebConnection}; Tomcat will close the
     * connection when the client (this test) disconnects.
     *
     * @req FR-TEST-003
     */
    public static final class NoopUpgradeHandler implements HttpUpgradeHandler {
        public NoopUpgradeHandler() {
            // No-arg constructor required by HttpUpgradeHandler contract — Tomcat instantiates
            // the handler via reflection after the upgrade handshake completes.
        }

        @Override
        public void init(WebConnection wc) {
            // Intentional no-op. The handshake (HTTP 101 + upgrade headers) is the contract under
            // test; post-upgrade frame traffic is out of scope. Register a no-op WriteListener to
            // avoid IllegalStateException if Tomcat probes the output state after init.
            try {
                wc.getOutputStream().setWriteListener(new WriteListener() {
                    @Override
                    public void onWritePossible() {
                        // no-op
                    }

                    @Override
                    public void onError(Throwable t) {
                        // no-op
                    }
                });
            } catch (IOException ignored) {
                // best-effort — Tomcat owns the connection lifecycle from here.
            }
        }

        @Override
        public void destroy() {
            // No resources to release.
        }
    }
}
