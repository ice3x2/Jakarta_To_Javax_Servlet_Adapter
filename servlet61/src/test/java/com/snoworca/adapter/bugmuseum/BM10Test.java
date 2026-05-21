package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * BM10 — servlet61 mirror of servlet5 BugMuseum BM10 (NFR-CONV-001 / D12 single-thread sync lock-in).
 *
 * adapter.javax.servlet61.http.HttpUpgradeHandler uses a static ThreadLocal to pass the jakarta
 * upgrade handler class into the no-arg constructor. The behavior lock-in: ThreadLocal value is
 * consumed exactly once per same-thread synchronous instantiation, then cleared.
 *
 * Unlike BM01..BM09 (defect regressions), BM10 is a behavior lock — it MUST pass today and start
 * failing only if the single-thread sync assumption is broken.
 *
 * @req FR-TEST-004
 */
class BM10Test {

    @Test
    @DisplayName("BM10: HttpUpgradeHandler ThreadLocal single-thread sync invocation lock-in (NFR-CONV-001)")
    void shouldReproduceBug10() throws Exception {
        // Step 1: set the jakarta upgrade handler class via the static ThreadLocal seam.
        adapter.javax.servlet61.http.HttpUpgradeHandler.setJakartaUpgradeHandlerClass(
            DummyJakartaUpgradeHandler.class);

        // Step 2: a same-thread synchronous instantiation must succeed (consumes the ThreadLocal value).
        adapter.javax.servlet61.http.HttpUpgradeHandler instance =
            new adapter.javax.servlet61.http.HttpUpgradeHandler();
        assertNotNull(instance, "same-thread synchronous instantiation must succeed");
        assertNotNull(instance.getHttpUpgradeHandler(),
            "wrapped jakarta upgrade handler must be initialized");

        // Step 3: a subsequent same-thread instantiation must fail because the ThreadLocal was
        //         cleared by the previous constructor (the documented lock-in: ThreadLocal value
        //         is used exactly once per same-thread synchronous call). If the lock-in breaks
        //         (e.g. ThreadLocal leaks across calls or threads), this assertion fails.
        assertThrows(IllegalStateException.class,
            () -> new adapter.javax.servlet61.http.HttpUpgradeHandler(),
            "ThreadLocal must be cleared after each same-thread synchronous instantiation");
    }

    /**
     * Minimal jakarta.servlet.http.HttpUpgradeHandler implementation used only by BM10
     * to drive the ThreadLocal-based instantiation flow.
     */
    public static class DummyJakartaUpgradeHandler implements jakarta.servlet.http.HttpUpgradeHandler {
        public DummyJakartaUpgradeHandler() {
        }

        @Override
        public void init(jakarta.servlet.http.WebConnection wc) {
        }

        @Override
        public void destroy() {
        }
    }
}
