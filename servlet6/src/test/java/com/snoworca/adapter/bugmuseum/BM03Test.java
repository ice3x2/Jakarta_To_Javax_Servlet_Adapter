package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * BM03 — servlet6 mirror of servlet5 BugMuseum BM03 (FR-FIX-004).
 *
 * adapter.jakarta.servlet6.ServletContext.setResponseCharacterEncoding must delegate to origin's
 * setResponseCharacterEncoding, not setRequestCharacterEncoding.
 *
 * @req FR-TEST-004
 */
class BM03Test {

    @Test
    @DisplayName("BM03: jakarta ServletContext.setResponseCharacterEncoding must call origin.setResponseCharacterEncoding (FR-FIX-004)")
    void shouldReproduceBug03() {
        javax.servlet.ServletContext origin = mock(javax.servlet.ServletContext.class);
        adapter.jakarta.servlet6.ServletContext adapterCtx =
            new adapter.jakarta.servlet6.ServletContext(origin);

        adapterCtx.setResponseCharacterEncoding("UTF-8");

        verify(origin).setResponseCharacterEncoding("UTF-8");
        verify(origin, never()).setRequestCharacterEncoding(any());
    }
}
