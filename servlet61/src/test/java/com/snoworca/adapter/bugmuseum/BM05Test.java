package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * BM05 — servlet61 mirror of servlet5 BugMuseum BM05 (FR-FIX-003).
 *
 * adapter.jakarta.servlet61.ServletResponse.setContentLengthLong must delegate to origin's
 * setContentLengthLong without narrowing to int.
 *
 * @req FR-TEST-004
 */
class BM05Test {

    @Test
    @DisplayName("BM05: jakarta ServletResponse.setContentLengthLong must call origin.setContentLengthLong (no int narrowing) (FR-FIX-003)")
    void shouldReproduceBug05() {
        javax.servlet.ServletResponse origin = mock(javax.servlet.ServletResponse.class);
        adapter.jakarta.servlet61.ServletResponse adapterResp =
            new adapter.jakarta.servlet61.ServletResponse(origin);

        adapterResp.setContentLengthLong(Long.MAX_VALUE);

        verify(origin).setContentLengthLong(Long.MAX_VALUE);
        verify(origin, never()).setContentLength(anyInt());
    }
}
