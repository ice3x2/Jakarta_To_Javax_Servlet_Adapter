package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * BM08 — servlet61 mirror of servlet5 BugMuseum BM08 (FR-FIX-005).
 *
 * adapter.javax.servlet61.ServletContext.setRequestCharacterEncoding must delegate to origin's
 * setRequestCharacterEncoding, not setResponseCharacterEncoding.
 *
 * @req FR-TEST-004
 */
class BM08Test {

    @Test
    @DisplayName("BM08: javax ServletContext.setRequestCharacterEncoding must call origin.setRequestCharacterEncoding (FR-FIX-005)")
    void shouldReproduceBug08() {
        jakarta.servlet.ServletContext origin = mock(jakarta.servlet.ServletContext.class);
        adapter.javax.servlet61.ServletContext adapterCtx =
            new adapter.javax.servlet61.ServletContext(origin);

        adapterCtx.setRequestCharacterEncoding("UTF-8");

        verify(origin).setRequestCharacterEncoding("UTF-8");
        // jakarta 6.1 added setResponseCharacterEncoding(Charset) overload; disambiguate with anyString().
        verify(origin, never()).setResponseCharacterEncoding(anyString());
    }
}
