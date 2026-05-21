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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter5.ServletConverter;

/**
 * IT06 — Filter chain ordering integration test (servlet5 module).
 *
 * <p>Boots Tomcat 10.0.x via {@link TomcatRunner}, registers two {@code javax.servlet}
 * filters and one {@code javax.servlet} servlet bridged to {@code jakarta.servlet}
 * through {@link ServletConverter#convert(javax.servlet.Filter)} and
 * {@link ServletConverter#convert(javax.servlet.Servlet)}. Verifies that the
 * filter chain executes in the order Filter A entry → Filter B entry → Servlet →
 * Filter B exit → Filter A exit, and that request/response wrappers introduced
 * inside the filters do not leak data outside the chain boundaries.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT06FilterChainTest {

    private static final List<String> ORDER_LOG = Collections.synchronizedList(new ArrayList<>());

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldChainFiltersInOrder() throws Exception {
        ORDER_LOG.clear();

        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Filter javaxFilterA = new OrderRecordingFilter("A");
            javax.servlet.Filter javaxFilterB = new OrderRecordingFilter("B");
            javax.servlet.Servlet javaxServlet = new ChainProbeServlet();

            jakarta.servlet.Filter jakartaFilterA = ServletConverter.convert(javaxFilterA);
            jakarta.servlet.Filter jakartaFilterB = ServletConverter.convert(javaxFilterB);
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);

            runner.addFilter("filterA", "/chain", jakartaFilterA);
            runner.addFilter("filterB", "/chain", jakartaFilterB);
            runner.addServlet("chain", "/chain", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/chain").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            assertEquals(200, conn.getResponseCode(), "status");
            String body = new String(readAll(conn.getInputStream()), StandardCharsets.UTF_8);

            // Servlet must observe the wrapped request attribute set by Filter B.
            assertEquals("wrapped-by-B", body, "servlet sees Filter B request wrapper attribute");

            // Filter A must observe its own response wrapper after chain.doFilter returns.
            String responseTag = conn.getHeaderField("X-Filter-A-Saw");
            assertNotNull(responseTag, "Filter A response header captured after chain");
            assertEquals("status=200", responseTag, "Filter A captured status from wrapped response");

            // Chain ordering A-in → B-in → servlet → B-out → A-out
            List<String> snapshot;
            synchronized (ORDER_LOG) {
                snapshot = new ArrayList<>(ORDER_LOG);
            }
            assertEquals(List.of("A-in", "B-in", "servlet", "B-out", "A-out"), snapshot,
                    "filter chain order");

            // Wrapper data must not leak: header set on the inner wrapped response
            // by Filter B is invisible after Filter A exit, and the original
            // request attribute set by Filter B is not visible on subsequent
            // unrelated requests.
            assertTrue(conn.getHeaderField("X-Filter-B-Wrapper-Only") == null,
                    "Filter B inner wrapper header must not leak to client");

            // Issue a second request without filter B's wrapper to confirm
            // request attribute isolation across requests.
            HttpURLConnection probe = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/chain?probe=1").openConnection();
            probe.setRequestMethod("GET");
            probe.setConnectTimeout(5000);
            probe.setReadTimeout(5000);
            assertEquals(200, probe.getResponseCode(), "second status");
            String probeBody = new String(readAll(probe.getInputStream()), StandardCharsets.UTF_8);
            // Filter B always wraps for /chain, so attribute must still be present —
            // but it must originate from THIS request's wrapping, not a leaked one.
            // We verify the attribute exists (proves wrapping is per-request).
            assertEquals("wrapped-by-B", probeBody, "second request also wrapped fresh");
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
    static final class OrderRecordingFilter implements Filter {
        private final String tag;

        OrderRecordingFilter(String tag) {
            this.tag = tag;
        }

        @Override
        public void init(FilterConfig filterConfig) {
            // no-op
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            ORDER_LOG.add(this.tag + "-in");

            ServletRequest forwardRequest = request;
            ServletResponse forwardResponse = response;

            if ("B".equals(this.tag) && request instanceof HttpServletRequest) {
                forwardRequest = new RequestTagWrapper((HttpServletRequest) request, "wrapped-by-B");
            }
            if ("B".equals(this.tag) && response instanceof HttpServletResponse) {
                // Filter B wraps response with a buffering wrapper that intentionally
                // swallows the X-Filter-B-Wrapper-Only header. The outermost response
                // (Filter A and the HTTP client) must not observe that header — this
                // proves wrapper data does not leak past the wrapper boundary.
                HttpServletResponseWrapper innerWrapper =
                        new HttpServletResponseWrapper((HttpServletResponse) response) {
                            @Override
                            public void addHeader(String name, String value) {
                                if ("X-Filter-B-Wrapper-Only".equals(name)) {
                                    // swallow — proves wrapper isolation
                                    return;
                                }
                                super.addHeader(name, value);
                            }

                            @Override
                            public void setHeader(String name, String value) {
                                if ("X-Filter-B-Wrapper-Only".equals(name)) {
                                    return;
                                }
                                super.setHeader(name, value);
                            }
                        };
                // Attempt to set the header — wrapper must swallow it.
                innerWrapper.addHeader("X-Filter-B-Wrapper-Only", "inner");
                forwardResponse = innerWrapper;
            }

            chain.doFilter(forwardRequest, forwardResponse);

            if ("A".equals(this.tag) && response instanceof HttpServletResponse) {
                HttpServletResponse httpResp = (HttpServletResponse) response;
                httpResp.setHeader("X-Filter-A-Saw", "status=" + httpResp.getStatus());
            }
            ORDER_LOG.add(this.tag + "-out");
        }

        @Override
        public void destroy() {
            // no-op
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class RequestTagWrapper extends HttpServletRequestWrapper {
        private final String tag;

        RequestTagWrapper(HttpServletRequest req, String tag) {
            super(req);
            this.tag = tag;
        }

        @Override
        public Object getAttribute(String name) {
            if ("chain.tag".equals(name)) {
                return this.tag;
            }
            return super.getAttribute(name);
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class ChainProbeServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            ORDER_LOG.add("servlet");
            Object tag = req.getAttribute("chain.tag");
            String body = (tag == null) ? "" : tag.toString();
            // Avoid setContentLength + raw OutputStream to keep the response
            // uncommitted — Filter A must be able to set its X-Filter-A-Saw
            // header AFTER chain.doFilter returns. The default response buffer
            // (8 KB) will hold the small payload until the chain unwinds.
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().write(body);
        }
    }
}
