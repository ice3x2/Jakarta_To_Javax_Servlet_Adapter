package com.snoworca.adapter.bugmuseum;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * BM06 — servlet6 mirror of servlet5 BugMuseum BM06 (FR-FIX-007).
 *
 * servlet5 variant asserted that adapter.jakarta.servlet5.ServletRequest.getRealPath is marked
 * {@code @Deprecated} (jakarta 5.0+ removed getRealPath from the interface, so callers must be
 * warned).
 *
 * jakarta 6.0 ALSO removes getRealPath from jakarta.servlet.ServletRequest interface. This mirror
 * verifies the semantically equivalent invariant: the adapter retains a compatibility
 * {@code getRealPath} method (so existing javax callers still compile) AND the underlying
 * jakarta.servlet.ServletRequest interface no longer declares it (proving the adapter is the only
 * surviving path). Annotation-level assertion is omitted because the servlet6 adapter
 * intentionally documents the compatibility status via the source comment / removed
 * {@code @Override} (jakarta interface removed the method) instead of a runtime annotation.
 *
 * @req FR-TEST-004
 */
class BM06Test {

    @Test
    @DisplayName("BM06: jakarta 6.0 ServletRequest no longer declares getRealPath; adapter retains compatibility method (FR-FIX-007)")
    void shouldReproduceBug06() throws NoSuchMethodException {
        // (1) The adapter must still expose getRealPath(String) so existing callers compile.
        Method adapterMethod = adapter.jakarta.servlet6.ServletRequest.class
            .getMethod("getRealPath", String.class);
        assertNotNull(adapterMethod, "adapter.jakarta.servlet6.ServletRequest must retain getRealPath");
        assertEquals(String.class, adapterMethod.getReturnType());

        // (2) The jakarta.servlet.ServletRequest interface in jakarta 6.0 must no longer declare
        // getRealPath — this is the API change that motivated the servlet5 BM06 deprecation
        // marker and is the analogous invariant in servlet6.
        assertThrows(NoSuchMethodException.class,
            () -> jakarta.servlet.ServletRequest.class.getMethod("getRealPath", String.class),
            "jakarta 6.0 ServletRequest interface must no longer declare getRealPath; the adapter "
                + "provides it as a compatibility method only.");
    }
}
