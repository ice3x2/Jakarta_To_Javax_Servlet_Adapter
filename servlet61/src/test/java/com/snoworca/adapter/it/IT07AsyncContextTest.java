package com.snoworca.adapter.it;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter61.ServletConverter;

/**
 * IT07 — AsyncContext startAsync/complete integration test (servlet61 module).
 *
 * <p>Boots Tomcat 11.0.x via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged through {@link ServletConverter#convert(javax.servlet.Servlet)}.
 * The servlet calls {@link HttpServletRequest#startAsync()}, hands the
 * {@link AsyncContext} off to a separate executor thread, writes the response body
 * there, then invokes {@link AsyncContext#complete()}. The test asserts the client
 * receives the body before the awaitility 5-second timeout, proving the adapter
 * propagates async lifecycle correctly across the javax ↔ jakarta boundary.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT07AsyncContextTest {

    private static final String BODY = "async-ok";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldCompleteAsync() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (TomcatRunner runner = new TomcatRunner()) {
            AsyncProbeServlet probe = new AsyncProbeServlet(executor);
            javax.servlet.Servlet javaxServlet = probe;
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("async", "/async", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/async").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            assertEquals(200, conn.getResponseCode(), "status");
            String body = new String(readAll(conn.getInputStream()), StandardCharsets.UTF_8);
            assertEquals(BODY, body, "async body");

            await().atMost(Duration.ofSeconds(5))
                    .untilTrue(probe.completed);
            assertTrue(probe.completed.get(), "AsyncContext.complete invoked");
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
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
    static final class AsyncProbeServlet extends HttpServlet {
        private final ExecutorService executor;
        final AtomicBoolean completed = new AtomicBoolean(false);

        AsyncProbeServlet(ExecutorService executor) {
            this.executor = executor;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            AsyncContext ctx = req.startAsync();
            ctx.setTimeout(5000);
            this.executor.submit(() -> {
                try {
                    HttpServletResponse asyncResp = (HttpServletResponse) ctx.getResponse();
                    asyncResp.setStatus(200);
                    asyncResp.setContentType("text/plain; charset=UTF-8");
                    asyncResp.getWriter().write(BODY);
                    asyncResp.getWriter().flush();
                } catch (IOException ignored) {
                    // best-effort — completion still invoked in finally
                } finally {
                    ctx.complete();
                    this.completed.set(true);
                }
            });
        }
    }
}
