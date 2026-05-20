package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-001 (인터페이스 완전 구현 — SPEC-1)
 * for the servlet6 module (jakarta 6.0 신규 추상 메서드).
 *
 * Scope (T-PH004-17 — RED):
 *   FR-CONV-001 AC-1: ServletRequest.getRequestId() 가 lazy UUID 를 반환한다.
 *                     동일 인스턴스에서 두 번 호출 시 같은 UUID 가 반환된다
 *                     (lazy caching — 매 호출마다 새 UUID 생성 금지).
 *   FR-CONV-001 AC-2: ServletRequest.getProtocolRequestId() 가 빈 문자열 ""
 *                     을 반환하며, ServletRequest.getServletConnection() 이
 *                     DummyServletConnection (합성 fake 객체) 를 반환한다
 *                     (javax 측 원본은 본 메서드 자체가 없으므로 어댑터가
 *                      합성).
 *
 * Red phase 의도:
 *   - 현재 시점 (T-PH004-16 까지 완료) servlet6 ServletRequest 의
 *     getRequestId() 는 placeholder 로 null 을 반환하고,
 *     getProtocolRequestId() 도 null 을 반환하며,
 *     getServletConnection() 은 UnsupportedOperationException 을 throw
 *     한다. 따라서 두 test 모두 fail (red) 한다.
 *
 * expected_failure_signature (sidecar 2 case 공통):
 *   "expected: getRequestId UUID + getProtocolRequestId empty +
 *    getServletConnection synth, got: AbstractMethodError"
 *   — 실제로는 placeholder return null / throw UOE 이므로 본 의도는
 *   "placeholder 동작이 SRS 요구를 충족하지 않음" 으로 매핑.
 *
 * Mock 사용 없음 (§0.6) — dynamic Proxy 로 javax.servlet.http.HttpServletRequest
 * 원본을 합성 (sibling AdapterUnwrapAllClassesTest 와 동일 패턴).
 *
 * @req FR-CONV-001
 */
class RequestIdTest {

    /**
     * Create a no-op javax.servlet.http.HttpServletRequest dynamic proxy.
     * All methods return null / 0 / false default (Mock 아님 — JDK Proxy).
     */
    private static javax.servlet.http.HttpServletRequest newFakeJavaxRequest() {
        return (javax.servlet.http.HttpServletRequest) Proxy.newProxyInstance(
            RequestIdTest.class.getClassLoader(),
            new Class<?>[]{ javax.servlet.http.HttpServletRequest.class },
            (proxy, method, args) -> null);
    }

    // FR-CONV-001 AC-1 — getRequestId() lazy UUID
    @Test
    @DisplayName("FR-CONV-001 AC-1: ServletRequest.getRequestId() 가 lazy UUID 를 반환한다 (servlet6)")
    void shouldImplementJakarta6NewAbstractMethods_FRCONV001_AC1() {
        javax.servlet.http.HttpServletRequest origin = newFakeJavaxRequest();
        adapter.jakarta.servlet6.http.HttpServletRequest wrapper =
            new adapter.jakarta.servlet6.http.HttpServletRequest(origin);

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
    @DisplayName("FR-CONV-001 AC-2: getProtocolRequestId() 빈 문자열 + getServletConnection() 합성 (servlet6)")
    void shouldImplementJakarta6NewAbstractMethods_FRCONV001_AC2() {
        javax.servlet.http.HttpServletRequest origin = newFakeJavaxRequest();
        adapter.jakarta.servlet6.http.HttpServletRequest wrapper =
            new adapter.jakarta.servlet6.http.HttpServletRequest(origin);

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

        // DummyServletConnection 합성 검증 — 두 번 호출 시 동일 인스턴스 (또는 동일 값) 의무 없음,
        // 단 non-null + sensible field 값 (id 비어있지 않음) 정도 검증.
        assertNotNull(conn.getConnectionId(),
            "expected: synthesized ServletConnection.getConnectionId() non-null"
                + " (FR-CONV-001 AC-2 — fake fallback), got: null");
    }
}
