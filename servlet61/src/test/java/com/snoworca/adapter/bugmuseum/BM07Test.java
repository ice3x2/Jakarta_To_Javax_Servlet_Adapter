package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BM07 — servlet61 mirror of servlet5 BugMuseum BM07 (FR-FIX-009).
 *
 * adapter.jakarta.servlet61.http.HttpServletRequest.getCookies must tolerate the origin returning
 * null without throwing NullPointerException.
 *
 * @req FR-TEST-004
 */
class BM07Test {

    @Test
    @DisplayName("BM07: jakarta HttpServletRequest.getCookies must not throw NullPointerException when origin returns null (FR-FIX-009)")
    void shouldReproduceBug07() {
        javax.servlet.http.HttpServletRequest origin = mock(javax.servlet.http.HttpServletRequest.class);
        when(origin.getCookies()).thenReturn(null);

        adapter.jakarta.servlet61.http.HttpServletRequest adapterReq =
            new adapter.jakarta.servlet61.http.HttpServletRequest(origin);

        jakarta.servlet.http.Cookie[] cookies = assertDoesNotThrow(adapterReq::getCookies,
            "adapter.getCookies must not throw NullPointerException when origin.getCookies() returns null");

        // Either null or an empty array is acceptable; the defect is throwing NPE.
        if (cookies != null) {
            assertEquals(0, cookies.length,
                "when origin returns null, the adapter should return null or an empty array");
        }
    }
}
