package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-005 (ServletException 양방향 변환 — try/catch
 * + cause chain).
 *
 * Scope (T-PH004-05 — RED):
 *   - AC-1: jakarta-side 위임 메서드 호출 시 발생한 javax.servlet.ServletException
 *           이 jakarta.servlet.ServletException 으로 변환되고 cause 가 원본을
 *           가리켜야 한다 (bidirectional, 정방향).
 *   - AC-2: javax-side 위임 메서드 호출 시 발생한 jakarta.servlet.ServletException
 *           이 javax.servlet.ServletException 으로 변환되고 cause 가 원본을
 *           가리켜야 한다 (bidirectional, 역방향).
 *   - AC-3: IOException / IllegalStateException 등 표준 예외는 어댑터를 통해
 *           그대로 propagate 된다 (cause chain 없이 동일 인스턴스).
 *
 * Red phase 의도:
 *   - T-PH004-06 (impl) 에서 도입 예정인 common 모듈의 ConverterSupport helper
 *     (javaxToJakartaServletException + jakartaToJavaxServletException) 가 현재
 *     시점에는 부재한다.
 *   - test 는 ConverterSupport.javaxToJakartaServletException(...) /
 *     jakartaToJavaxServletException(...) 두 정적 헬퍼를 reflection 으로 lookup
 *     하여 호출하고, 그 결과 (반환 예외 타입 + cause identity) 를 검증한다.
 *   - 현재 코드에는 ConverterSupport 클래스가 존재하지 않으므로 ClassNotFound
 *     단계에서 fail.
 *   - AC-3 도 동일하게 ConverterSupport 의 standard-exception passthrough
 *     계약을 검증하는 helper (예: rethrowOrConvert) 를 lookup 하여 부재로 fail.
 *
 * expected_failure_signature (sidecar):
 *   "expected: bidirectional ServletException with initCause, got: missing
 *    converter helper"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-005-AC1-01 → _FRCONV005_AC1
 *   TC-REQ-FR-CONV-005-AC2-01 → _FRCONV005_AC2
 *   TC-REQ-FR-CONV-005-AC3-01 → _FRCONV005_AC3
 *
 * Mock 사용 없음 (§0.6) — reflection 으로 production helper 부재를 직접 검출.
 *
 * @req FR-CONV-005
 */
class ServletExceptionConversionTest {

    private static final String CONVERTER_FQN = "adapter.common.ConverterSupport";
    private static final String FORWARD_METHOD = "javaxToJakartaServletException";
    private static final String REVERSE_METHOD = "jakartaToJavaxServletException";
    private static final String PASSTHROUGH_METHOD = "rethrowOrConvert";

    // -----------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-005
     */
    private static Class<?> lookupConverterSupport() {
        try {
            return Class.forName(CONVERTER_FQN, true,
                    ServletExceptionConversionTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail("expected: bidirectional ServletException with initCause, got: missing converter helper — "
                    + CONVERTER_FQN + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-005
     */
    private static Method lookupStaticMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            Method m = owner.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            fail("expected: bidirectional ServletException with initCause, got: missing converter helper — "
                    + owner.getName() + "#" + name + " not declared");
            throw new AssertionError("unreachable");
        }
    }

    // -----------------------------------------------------------------
    // AC-1 — jakarta-side: javax.ServletException → jakarta.ServletException
    //         with cause chain preserved
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-005
     */
    @Test
    @DisplayName("FR-CONV-005 AC-1: javax→jakarta ServletException bidirectional with cause chain")
    void shouldConvertServletExceptionBidirectionalWithCauseChain_FRCONV005_AC1() {
        Class<?> support = lookupConverterSupport();
        Method forward = lookupStaticMethod(support, FORWARD_METHOD, javax.servlet.ServletException.class);

        javax.servlet.ServletException original =
                new javax.servlet.ServletException("javax-side root cause");

        Object converted;
        try {
            converted = forward.invoke(null, original);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("expected: bidirectional ServletException with initCause, got: helper invocation failed — " + e);
            return;
        }

        assertNotNull(converted,
                "expected: bidirectional ServletException with initCause, got: null return");
        assertTrue(converted instanceof jakarta.servlet.ServletException,
                "expected: bidirectional ServletException with initCause, got: "
                        + converted.getClass().getName());

        jakarta.servlet.ServletException cast = (jakarta.servlet.ServletException) converted;
        assertSame(original, cast.getCause(),
                "expected: bidirectional ServletException with initCause, got: cause="
                        + cast.getCause());
    }

    // -----------------------------------------------------------------
    // AC-2 — javax-side: jakarta.ServletException → javax.ServletException
    //         with cause chain preserved
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-005
     */
    @Test
    @DisplayName("FR-CONV-005 AC-2: jakarta→javax ServletException bidirectional with cause chain")
    void shouldConvertServletExceptionBidirectionalWithCauseChain_FRCONV005_AC2() {
        Class<?> support = lookupConverterSupport();
        Method reverse = lookupStaticMethod(support, REVERSE_METHOD, jakarta.servlet.ServletException.class);

        jakarta.servlet.ServletException original =
                new jakarta.servlet.ServletException("jakarta-side root cause");

        Object converted;
        try {
            converted = reverse.invoke(null, original);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("expected: bidirectional ServletException with initCause, got: helper invocation failed — " + e);
            return;
        }

        assertNotNull(converted,
                "expected: bidirectional ServletException with initCause, got: null return");
        assertTrue(converted instanceof javax.servlet.ServletException,
                "expected: bidirectional ServletException with initCause, got: "
                        + converted.getClass().getName());

        javax.servlet.ServletException cast = (javax.servlet.ServletException) converted;
        assertSame(original, cast.getCause(),
                "expected: bidirectional ServletException with initCause, got: cause="
                        + cast.getCause());
    }

    // -----------------------------------------------------------------
    // AC-3 — standard exceptions (IOException 등) passthrough — 동일 인스턴스
    //         로 propagate, cause chain 없음
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-005
     */
    @Test
    @DisplayName("FR-CONV-005 AC-3: IOException 등 표준 예외는 동일 인스턴스 그대로 propagate (cause chain 없이)")
    void shouldConvertServletExceptionBidirectionalWithCauseChain_FRCONV005_AC3() {
        Class<?> support = lookupConverterSupport();
        Method passthrough = lookupStaticMethod(support, PASSTHROUGH_METHOD, Throwable.class);

        IOException standard = new IOException("standard io failure");

        Throwable propagated;
        try {
            propagated = (Throwable) passthrough.invoke(null, standard);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("expected: bidirectional ServletException with initCause, got: helper invocation failed — " + e);
            return;
        }

        assertSame(standard, propagated,
                "expected: bidirectional ServletException with initCause, got: not-same-instance propagation");
    }
}
