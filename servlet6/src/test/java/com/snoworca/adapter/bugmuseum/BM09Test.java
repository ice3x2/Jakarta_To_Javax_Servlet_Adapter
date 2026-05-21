package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BM09 — servlet6 mirror of servlet5 BugMuseum BM09 (FR-FIX-006).
 *
 * adapter.jakarta.servlet6.ServletRequest.getContentLengthLong must delegate directly without
 * try/catch + int fallback. If origin.getContentLengthLong throws, the adapter must propagate the
 * exact exception (not swallow it and fall back to the lossy getContentLength()).
 *
 * @req FR-TEST-004
 */
class BM09Test {

    @Test
    @DisplayName("BM09: jakarta ServletRequest.getContentLengthLong must delegate directly without try/catch+int fallback (FR-FIX-006)")
    void shouldReproduceBug09() {
        javax.servlet.ServletRequest origin = mock(javax.servlet.ServletRequest.class);
        IllegalStateException boom = new IllegalStateException("origin failure");
        when(origin.getContentLengthLong()).thenThrow(boom);
        // The defective code silently swallows the exception and falls back to getContentLength().
        // If the adapter incorrectly delegates to getContentLength(), this stub returns 0; that
        // would let the defective code "succeed" with the wrong value. The assertion below
        // requires propagation of the exception, which is the only correct behavior.
        when(origin.getContentLength()).thenReturn(0);

        adapter.jakarta.servlet6.ServletRequest adapterReq =
            new adapter.jakarta.servlet6.ServletRequest(origin);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            adapterReq::getContentLengthLong,
            "adapter must propagate origin.getContentLengthLong() exceptions, not swallow them "
                + "and fall back to the lossy int getContentLength()");
        assertSame(boom, thrown, "the exact origin exception must be propagated");
    }
}
