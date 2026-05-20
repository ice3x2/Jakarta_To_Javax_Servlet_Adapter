package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-001 (인터페이스 완전 구현 — SPEC-1)
 * for the servlet61 module (jakarta 6.0 신규 추상 메서드).
 *
 * See servlet6 sibling for detailed contract — only package targets differ.
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1) — servlet61 mirrors servlet6
 * by symbol name. Single sidecar test_case set, file path differs per module.
 *
 * Mock 사용 없음 (§0.6).
 *
 * @req FR-CONV-001
 */
class RequestIdTest {

    private static javax.servlet.http.HttpServletRequest newFakeJavaxRequest() {
        return (javax.servlet.http.HttpServletRequest) Proxy.newProxyInstance(
            RequestIdTest.class.getClassLoader(),
            new Class<?>[]{ javax.servlet.http.HttpServletRequest.class },
            (proxy, method, args) -> null);
    }

    // FR-CONV-001 AC-1 — getRequestId() lazy UUID
    @Test
    @DisplayName("FR-CONV-001 AC-1: ServletRequest.getRequestId() 가 lazy UUID 를 반환한다 (servlet61)")
    void shouldImplementJakarta6NewAbstractMethods_FRCONV001_AC1() {
        javax.servlet.http.HttpServletRequest origin = newFakeJavaxRequest();
        adapter.jakarta.servlet61.http.HttpServletRequest wrapper =
            new adapter.jakarta.servlet61.http.HttpServletRequest(origin);

        String first = wrapper.getRequestId();
        assertNotNull(first,
            "expected: getRequestId() returns non-null UUID string (FR-CONV-001 AC-1),"
                + " got: null (placeholder)");

        try {
            UUID.fromString(first);
        } catch (IllegalArgumentException e) {
            fail("expected: getRequestId() returns parseable UUID (FR-CONV-001 AC-1),"
                + " got: not a UUID — value=" + first);
        }

        String second = wrapper.getRequestId();
        assertEquals(first, second,
            "expected: getRequestId() returns the same lazy UUID on repeated calls"
                + " (FR-CONV-001 AC-1 — lazy caching), got: first=" + first
                + " second=" + second);
    }

    // FR-CONV-001 AC-2 — getProtocolRequestId() empty + getServletConnection() synth
    @Test
    @DisplayName("FR-CONV-001 AC-2: getProtocolRequestId() 빈 문자열 + getServletConnection() 합성 (servlet61)")
    void shouldImplementJakarta6NewAbstractMethods_FRCONV001_AC2() {
        javax.servlet.http.HttpServletRequest origin = newFakeJavaxRequest();
        adapter.jakarta.servlet61.http.HttpServletRequest wrapper =
            new adapter.jakarta.servlet61.http.HttpServletRequest(origin);

        String protocolRequestId = wrapper.getProtocolRequestId();
        assertNotNull(protocolRequestId,
            "expected: getProtocolRequestId() returns non-null (FR-CONV-001 AC-2),"
                + " got: null (placeholder)");
        assertEquals("", protocolRequestId,
            "expected: getProtocolRequestId() returns empty string \"\" (FR-CONV-001 AC-2),"
                + " got: " + protocolRequestId);

        jakarta.servlet.ServletConnection conn;
        try {
            conn = wrapper.getServletConnection();
        } catch (UnsupportedOperationException e) {
            fail("expected: getServletConnection() returns synthesized DummyServletConnection"
                + " (FR-CONV-001 AC-2), got: UnsupportedOperationException (placeholder)");
            return;
        }
        assertNotNull(conn,
            "expected: getServletConnection() returns non-null synthesized object"
                + " (FR-CONV-001 AC-2), got: null");

        assertNotNull(conn.getConnectionId(),
            "expected: synthesized ServletConnection.getConnectionId() non-null"
                + " (FR-CONV-001 AC-2 — fake fallback), got: null");
    }
}
