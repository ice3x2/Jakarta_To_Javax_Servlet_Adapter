package com.snoworca;

import adapter.common.testfakes.AbstractFakeServletContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-011 (strict 모드 — SPEC-9, opt-in).
 *
 * Scope (T-PH004-09 — RED):
 *   - AC-1: 시스템 프로퍼티 `com.snoworca.adapter.strict=true` 설정 시
 *           strict 가 활성화되어 ConverterSupport.throwIfStrict(msg) 가
 *           UnsupportedOperationException 을 throw 한다.
 *   - AC-2: ServletContext init-param `com.snoworca.adapter.strict=true`
 *           만 설정해도 동일 효과가 발생한다 (시스템 프로퍼티 미설정 시).
 *   - AC-3: OFF default (시스템 프로퍼티·init-param 모두 미설정) 시
 *           throwIfStrict 는 예외 없이 정상 return 한다 (lenient 폴백).
 *   - AC-4: strict 활성 시에도 정책표상 "F 동일" 인 메서드
 *           (getRequestId / getServletConnection) 와 "D 동일" 인
 *           getProtocolRequestId 는 default 동작 유지를 위해 strict
 *           검사에서 면제된다 — shouldBypassStrictFor("getRequestId") /
 *           ("getServletConnection") / ("getProtocolRequestId") 가 true.
 *
 * Red phase 의도:
 *   - T-PH004-10 (green) 에서 도입 예정인 ConverterSupport 의 strict-mode
 *     helper 메서드 집합 (initStrictModeFromSystemProperty,
 *     initStrictModeFromServletContext, throwIfStrict,
 *     shouldBypassStrictFor, resetStrictModeForTesting) 이 현재
 *     시점에는 부재한다.
 *   - 본 test 는 ConverterSupport 의 위 메서드들을 reflection 으로 lookup
 *     하여 부재로 fail 시킨다 (T-PH004-05, T-PH004-07 와 동일한
 *     missing-helper RED 패턴).
 *
 * expected_failure_signature (sidecar):
 *   "expected: opt-in strict mode + throwIfStrict + lenient default, got:
 *    missing flag or fallback bypass"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-011-AC1-01 → _FRCONV011_AC1
 *   TC-REQ-FR-CONV-011-AC2-01 → _FRCONV011_AC2
 *   TC-REQ-FR-CONV-011-AC3-01 → _FRCONV011_AC3
 *   TC-REQ-FR-CONV-011-AC4-01 → _FRCONV011_AC4
 *
 * Mock 사용 없음 (§0.6) — reflection 으로 production helper 부재를 직접
 * 검출. ServletContext 도 javax.servlet.ServletContext 의 minimal in-test
 * fake 구현체 (실제 인터페이스 직접 구현, 라이브러리 mock 없음) 로 대체.
 *
 * @req FR-CONV-011
 */
class StrictModeTest {

    private static final String CONVERTER_FQN = "adapter.common.ConverterSupport";
    private static final String FAILURE_SIGNATURE =
            "expected: opt-in strict mode + throwIfStrict + lenient default,"
                    + " got: missing flag or fallback bypass";
    private static final String SYSTEM_PROPERTY_KEY = "com.snoworca.adapter.strict";
    private static final String INIT_PARAM_KEY = "com.snoworca.adapter.strict";

    // -----------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-011
     */
    private static Class<?> lookupConverterSupport() {
        try {
            return Class.forName(CONVERTER_FQN, true,
                    StrictModeTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE + " — " + CONVERTER_FQN
                    + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-011
     */
    private static Method lookupStaticMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            Method m = owner.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — " + owner.getName() + "#" + name
                    + " not declared");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-011
     */
    private static void resetStrictMode(Class<?> support) {
        Method reset;
        try {
            reset = support.getDeclaredMethod("resetStrictModeForTesting");
            reset.setAccessible(true);
            reset.invoke(null);
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — " + support.getName()
                    + "#resetStrictModeForTesting not declared");
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE + " — resetStrictModeForTesting threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // AC-1 — 시스템 프로퍼티 ON → throwIfStrict 가 UOE throw
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-011
     */
    @Test
    @DisplayName("FR-CONV-011 AC-1: 시스템 프로퍼티 ON → throwIfStrict 가 UnsupportedOperationException throw")
    void shouldEnforceStrictModeWithOptInAndDefaultLenient_FRCONV011_AC1() {
        Class<?> support = lookupConverterSupport();
        Method initSysProp = lookupStaticMethod(support,
                "initStrictModeFromSystemProperty");
        Method throwIfStrict = lookupStaticMethod(support,
                "throwIfStrict", String.class);

        String previous = System.getProperty(SYSTEM_PROPERTY_KEY);
        System.setProperty(SYSTEM_PROPERTY_KEY, "true");
        try {
            resetStrictMode(support);
            try {
                initSysProp.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — initStrictModeFromSystemProperty threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            Throwable thrown = null;
            try {
                throwIfStrict.invoke(null, "HttpSession.getValue(name)");
            } catch (InvocationTargetException e) {
                thrown = e.getCause();
            } catch (IllegalAccessException e) {
                fail(FAILURE_SIGNATURE + " — throwIfStrict invocation failed: " + e);
                return;
            }

            assertNotNull(thrown, FAILURE_SIGNATURE
                    + " — throwIfStrict did not throw with system property ON");
            assertTrue(thrown instanceof UnsupportedOperationException,
                    FAILURE_SIGNATURE + " — expected UnsupportedOperationException, got "
                            + thrown.getClass().getName());
        } finally {
            if (previous == null) {
                System.clearProperty(SYSTEM_PROPERTY_KEY);
            } else {
                System.setProperty(SYSTEM_PROPERTY_KEY, previous);
            }
            resetStrictMode(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-2 — init-param 만 ON → throwIfStrict 가 UOE throw
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-011
     */
    @Test
    @DisplayName("FR-CONV-011 AC-2: init-param 만 ON → throwIfStrict 가 UnsupportedOperationException throw")
    void shouldEnforceStrictModeWithOptInAndDefaultLenient_FRCONV011_AC2() {
        Class<?> support = lookupConverterSupport();
        Method initFromCtx = lookupStaticMethod(support,
                "initStrictModeFromServletContext", javax.servlet.ServletContext.class);
        Method throwIfStrict = lookupStaticMethod(support,
                "throwIfStrict", String.class);

        String previous = System.getProperty(SYSTEM_PROPERTY_KEY);
        System.clearProperty(SYSTEM_PROPERTY_KEY);
        try {
            resetStrictMode(support);

            javax.servlet.ServletContext ctxStrict = newServletContextWithInitParam(
                    INIT_PARAM_KEY, "true");
            try {
                initFromCtx.invoke(null, ctxStrict);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — initStrictModeFromServletContext threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            Throwable thrown = null;
            try {
                throwIfStrict.invoke(null, "HttpSession.getValue(name)");
            } catch (InvocationTargetException e) {
                thrown = e.getCause();
            } catch (IllegalAccessException e) {
                fail(FAILURE_SIGNATURE + " — throwIfStrict invocation failed: " + e);
                return;
            }

            assertNotNull(thrown, FAILURE_SIGNATURE
                    + " — throwIfStrict did not throw with init-param ON");
            assertTrue(thrown instanceof UnsupportedOperationException,
                    FAILURE_SIGNATURE + " — expected UnsupportedOperationException, got "
                            + thrown.getClass().getName());
        } finally {
            if (previous == null) {
                System.clearProperty(SYSTEM_PROPERTY_KEY);
            } else {
                System.setProperty(SYSTEM_PROPERTY_KEY, previous);
            }
            resetStrictMode(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-3 — 모두 미설정 → throwIfStrict 가 throw 하지 않음 (lenient default)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-011
     */
    @Test
    @DisplayName("FR-CONV-011 AC-3: OFF default → throwIfStrict 는 예외 없이 return (lenient 폴백)")
    void shouldEnforceStrictModeWithOptInAndDefaultLenient_FRCONV011_AC3() {
        Class<?> support = lookupConverterSupport();
        Method initSysProp = lookupStaticMethod(support,
                "initStrictModeFromSystemProperty");
        Method initFromCtx = lookupStaticMethod(support,
                "initStrictModeFromServletContext", javax.servlet.ServletContext.class);
        Method throwIfStrict = lookupStaticMethod(support,
                "throwIfStrict", String.class);

        String previous = System.getProperty(SYSTEM_PROPERTY_KEY);
        System.clearProperty(SYSTEM_PROPERTY_KEY);
        try {
            resetStrictMode(support);

            try {
                initSysProp.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — initStrictModeFromSystemProperty threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            javax.servlet.ServletContext ctxLenient = new InitParamServletContextFake(
                    INIT_PARAM_KEY, null);
            try {
                initFromCtx.invoke(null, ctxLenient);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — initStrictModeFromServletContext threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            Throwable thrown = null;
            try {
                throwIfStrict.invoke(null, "HttpSession.getValue(name)");
            } catch (InvocationTargetException e) {
                thrown = e.getCause();
            } catch (IllegalAccessException e) {
                fail(FAILURE_SIGNATURE + " — throwIfStrict invocation failed: " + e);
                return;
            }

            if (thrown != null) {
                fail(FAILURE_SIGNATURE
                        + " — throwIfStrict threw despite lenient default: "
                        + thrown.getClass().getName() + ": " + thrown.getMessage());
            }
        } finally {
            if (previous == null) {
                System.clearProperty(SYSTEM_PROPERTY_KEY);
            } else {
                System.setProperty(SYSTEM_PROPERTY_KEY, previous);
            }
            resetStrictMode(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-4 — strict ON 이어도 getRequestId / getServletConnection /
    //         getProtocolRequestId 는 default 유지 (strict 검사 면제)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-011
     */
    @Test
    @DisplayName("FR-CONV-011 AC-4: strict ON 시에도 getRequestId/getServletConnection/getProtocolRequestId 는 default 유지")
    void shouldEnforceStrictModeWithOptInAndDefaultLenient_FRCONV011_AC4() {
        Class<?> support = lookupConverterSupport();
        Method initSysProp = lookupStaticMethod(support,
                "initStrictModeFromSystemProperty");
        Method shouldBypass = lookupStaticMethod(support,
                "shouldBypassStrictFor", String.class);

        String previous = System.getProperty(SYSTEM_PROPERTY_KEY);
        System.setProperty(SYSTEM_PROPERTY_KEY, "true");
        try {
            resetStrictMode(support);
            try {
                initSysProp.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — initStrictModeFromSystemProperty threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            String[] bypassed = {
                    "getRequestId", "getServletConnection", "getProtocolRequestId"
            };
            for (String name : bypassed) {
                Object res;
                try {
                    res = shouldBypass.invoke(null, name);
                } catch (ReflectiveOperationException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    fail(FAILURE_SIGNATURE
                            + " — shouldBypassStrictFor(" + name + ") threw: "
                            + cause.getClass().getName() + ": " + cause.getMessage());
                    return;
                }
                assertNotNull(res, FAILURE_SIGNATURE
                        + " — shouldBypassStrictFor(" + name + ") returned null");
                assertTrue(res instanceof Boolean, FAILURE_SIGNATURE
                        + " — shouldBypassStrictFor(" + name + ") return type "
                        + res.getClass().getName());
                assertTrue((Boolean) res, FAILURE_SIGNATURE
                        + " — shouldBypassStrictFor(" + name + ") returned false");
            }

            Object notBypassed;
            try {
                notBypassed = shouldBypass.invoke(null, "getValue");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — shouldBypassStrictFor(getValue) threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            assertNotNull(notBypassed, FAILURE_SIGNATURE
                    + " — shouldBypassStrictFor(getValue) returned null");
            assertFalse((Boolean) notBypassed, FAILURE_SIGNATURE
                    + " — shouldBypassStrictFor(getValue) should be false");
        } finally {
            if (previous == null) {
                System.clearProperty(SYSTEM_PROPERTY_KEY);
            } else {
                System.setProperty(SYSTEM_PROPERTY_KEY, previous);
            }
            resetStrictMode(support);
        }
    }

    // -----------------------------------------------------------------
    // ServletContext fake (AC-2 / AC-3 helpers)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-011
     */
    private static javax.servlet.ServletContext newServletContextWithInitParam(
            String name, String value) {
        return new InitParamServletContextFake(name, value);
    }

    /**
     * Minimal in-test fake exposing only the init-param mapping required by
     * AC-2 / AC-3. Mock 사용 없음 (§0.6) — {@link AbstractFakeServletContext}
     * 의 기본 구현을 그대로 상속하고 getInitParameter 만 override.
     *
     * @req FR-CONV-011
     */
    static final class InitParamServletContextFake extends AbstractFakeServletContext {
        private final String paramName;
        private final String paramValue;

        InitParamServletContextFake(String paramName, String paramValue) {
            this.paramName = paramName;
            this.paramValue = paramValue;
        }

        @Override
        public String getInitParameter(String name) {
            if (paramName != null && paramName.equals(name)) {
                return paramValue;
            }
            return null;
        }
    }
}
