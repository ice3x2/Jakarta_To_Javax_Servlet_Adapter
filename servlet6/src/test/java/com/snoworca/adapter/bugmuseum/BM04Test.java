package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * BM04 — servlet6 mirror of servlet5 BugMuseum BM04 (FR-FIX-008).
 *
 * adapter.servletElementConverter6.ServletReqResConverter.convert(jakarta.servlet.ServletResponse)
 * must route a jakarta.servlet.http.HttpServletResponse to the Http adapter
 * (adapter.javax.servlet6.http.HttpServletResponse), not fall through to the generic
 * adapter.javax.servlet6.ServletResponse branch.
 *
 * Mirrors servlet5 BM04 (which exercised the jakarta→javax conversion path); same direction in
 * servlet6 because the same instanceof branch logic is at risk.
 *
 * @req FR-TEST-004
 */
class BM04Test {

    @Test
    @DisplayName("BM04: ServletReqResConverter must route jakarta.HttpServletResponse to Http adapter, not generic ServletResponse (FR-FIX-008)")
    void shouldReproduceBug04() {
        jakarta.servlet.http.HttpServletResponse jakartaHttp =
            mock(jakarta.servlet.http.HttpServletResponse.class);

        javax.servlet.ServletResponse converted =
            adapter.servletElementConverter6.ServletReqResConverter.convert(
                (jakarta.servlet.ServletResponse) jakartaHttp);

        assertNotNull(converted);
        assertTrue(
            converted instanceof adapter.javax.servlet6.http.HttpServletResponse,
            "expected adapter.javax.servlet6.http.HttpServletResponse, actual: "
                + converted.getClass().getName()
                + " (instanceof branch fell through to generic ServletResponse fallback)");
    }
}
