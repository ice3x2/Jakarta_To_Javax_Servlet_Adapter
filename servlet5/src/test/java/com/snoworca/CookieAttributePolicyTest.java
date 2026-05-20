package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-013 (Cookie attribute API 정책 D — SPEC-15).
 *
 * Scope (T-PH004-15 — RED):
 *   - AC-1: jakarta→javax 어댑터의 {@code setAttribute(String, String)}
 *           호출은 no-op + ConverterSupport.logFallbackOnce(WARN) 1회 emit
 *           ("Cookie.setAttribute@jakarta-&gt;javax" methodKey 기반).
 *   - AC-2: 동일 어댑터의 {@code getAttribute(String)} 가 항상 {@code null}
 *           을 반환한다 (보관소 없음).
 *   - AC-3: {@code getAttributes()} 가 빈 unmodifiable Map 을 반환한다
 *           ({@link Collections#emptyMap()} 와 의미 동일, mutation 시
 *           {@link UnsupportedOperationException}).
 *   - AC-4: 어댑터에 자체 attribute 보관용 instance Map 필드가 존재하지
 *           않는다 (자체 Map 보관 금지 — SPEC-15 정책 D 핵심).
 *
 * Red phase 의도:
 *   - T-PH004-16 (green) 에서 도입 예정인 adapter.javax.servletN.http.Cookie
 *     의 신규 메서드 {@code setAttribute(String, String)},
 *     {@code getAttribute(String)}, {@code getAttributes()} 가 현재
 *     시점에는 부재한다.
 *   - javax 4.x {@code javax.servlet.http.Cookie} 에는 attribute API 자체가
 *     없으므로, 본 메서드들은 어댑터 클래스에 직접 선언되어야 한다
 *     ({@link Class#getDeclaredMethod} 로 검출).
 *   - 본 test 는 위 API 를 reflection 으로 lookup 하여 부재로 fail 시킨다
 *     (T-PH004-09 StrictModeTest, T-PH004-11 DiagnosticsTest 와 동일한
 *     missing-helper RED 패턴).
 *
 * expected_failure_signature (sidecar):
 *   "expected: setAttribute no-op + getAttribute null + empty map + WARN
 *    once, got: internal map or NPE"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-013-AC1-01 → _FRCONV013_AC1
 *   TC-REQ-FR-CONV-013-AC2-01 → _FRCONV013_AC2
 *   TC-REQ-FR-CONV-013-AC3-01 → _FRCONV013_AC3
 *   TC-REQ-FR-CONV-013-AC4-01 → _FRCONV013_AC4
 *
 * Mock 사용 없음 (§0.6) — reflection 으로 production helper / 어댑터
 * 멤버 부재를 직접 검출. JUL Handler 는 java.util.logging.Handler 의
 * 실제 in-test 구현체로 LogRecord 수집 (라이브러리 mock 없음).
 *
 * 본 테스트는 servlet5 모듈에서 실행되어 RED 를 잠그고, T-PH004-16 의
 * green 단계는 동일 패키지 적용을 servlet5/servlet6/servlet61 3 모듈에
 * 모두 수행한다 (sidecar T-PH004-16.files[] SSOT).
 *
 * @req FR-CONV-013
 */
class CookieAttributePolicyTest {

    private static final String COOKIE_ADAPTER_FQN =
            "adapter.javax.servlet5.http.Cookie";
    private static final String CONVERTER_FQN =
            "adapter.common.ConverterSupport";
    private static final String EXPECTED_LOGGER_NAME =
            "com.snoworca.servletadapter";
    private static final String EXPECTED_METHOD_KEY =
            "Cookie.setAttribute@jakarta->javax";
    private static final String FAILURE_SIGNATURE =
            "expected: setAttribute no-op + getAttribute null + empty map"
                    + " + WARN once, got: internal map or NPE";

    // -----------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-013
     */
    private static Class<?> lookupCookieAdapter() {
        try {
            return Class.forName(COOKIE_ADAPTER_FQN, true,
                    CookieAttributePolicyTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE + " — " + COOKIE_ADAPTER_FQN
                    + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-013
     */
    private static Class<?> lookupConverterSupport() {
        try {
            return Class.forName(CONVERTER_FQN, true,
                    CookieAttributePolicyTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE + " — " + CONVERTER_FQN
                    + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * Adapter Cookie 의 직접 선언 메서드 (super 상속분 제외) 만 검출.
     * javax.servlet.http.Cookie (javax 4.x) 에는 attribute API 가 없으므로,
     * setAttribute/getAttribute/getAttributes 는 어댑터 클래스가 직접
     * 선언해야 한다.
     *
     * @req FR-CONV-013
     */
    private static Method lookupDeclaredMethod(Class<?> owner, String name,
                                               Class<?>... params) {
        try {
            Method m = owner.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — " + owner.getName() + "#" + name
                    + "(" + paramsToString(params) + ")"
                    + " not declared on adapter class");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-013
     */
    private static String paramsToString(Class<?>[] params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(params[i].getSimpleName());
        }
        return sb.toString();
    }

    /**
     * @req FR-CONV-013
     */
    private static void resetLoggingDedup(Class<?> support) {
        try {
            Method reset =
                    support.getDeclaredMethod("resetLoggingDedupForTesting");
            reset.setAccessible(true);
            reset.invoke(null);
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — resetLoggingDedupForTesting"
                    + " not declared on ConverterSupport");
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE + " — resetLoggingDedupForTesting threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
        }
    }

    /**
     * jakarta.servlet.http.Cookie 의 minimal instance — adapter 생성자
     * 인자용. mock 라이브러리 없이 실제 jakarta API 인스턴스 사용 (§0.6).
     *
     * @req FR-CONV-013
     */
    private static jakarta.servlet.http.Cookie newJakartaCookie() {
        return new jakarta.servlet.http.Cookie("sid", "abc");
    }

    /**
     * 어댑터 인스턴스 생성. 생성자가 부재하면 fail.
     *
     * @req FR-CONV-013
     */
    private static Object newAdapterCookie(Class<?> adapter) {
        try {
            return adapter
                    .getDeclaredConstructor(jakarta.servlet.http.Cookie.class)
                    .newInstance(newJakartaCookie());
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE + " — adapter Cookie constructor threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
            throw new AssertionError("unreachable");
        }
    }

    // -----------------------------------------------------------------
    // AC-1 — setAttribute no-op + WARN once (methodKey dedup)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-013
     */
    @Test
    @DisplayName("FR-CONV-013 AC-1: setAttribute no-op + WARN once via ConverterSupport.logFallbackOnce")
    void shouldApplyNoOpWithWarnOnceForCookieAttributeApi_FRCONV013_AC1() {
        Class<?> adapter = lookupCookieAdapter();
        Class<?> support = lookupConverterSupport();

        Method setAttr = lookupDeclaredMethod(adapter, "setAttribute",
                String.class, String.class);
        Object cookie = newAdapterCookie(adapter);

        Logger logger = Logger.getLogger(EXPECTED_LOGGER_NAME);
        RecordingHandler handler = new RecordingHandler();
        handler.setLevel(Level.ALL);
        Level previousLevel = logger.getLevel();
        boolean previousUseParent = logger.getUseParentHandlers();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        try {
            resetLoggingDedup(support);

            try {
                // 동일 어댑터에 attribute 2회 set — dedup 으로 첫 호출만 WARN
                setAttr.invoke(cookie, "SameSite", "Lax");
                setAttr.invoke(cookie, "Partitioned", "true");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — setAttribute threw: "
                        + cause.getClass().getName() + ": "
                        + cause.getMessage());
                return;
            }

            int warnCount = handler.countAtLevel(Level.WARNING,
                    EXPECTED_METHOD_KEY);
            assertEquals(1, warnCount, FAILURE_SIGNATURE
                    + " — expected exactly 1 WARN for methodKey "
                    + EXPECTED_METHOD_KEY + ", got " + warnCount);
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParent);
            resetLoggingDedup(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-2 — getAttribute(String) returns null (no storage)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-013
     */
    @Test
    @DisplayName("FR-CONV-013 AC-2: getAttribute(String) returns null even after setAttribute")
    void shouldApplyNoOpWithWarnOnceForCookieAttributeApi_FRCONV013_AC2() {
        Class<?> adapter = lookupCookieAdapter();
        Class<?> support = lookupConverterSupport();

        Method setAttr = lookupDeclaredMethod(adapter, "setAttribute",
                String.class, String.class);
        Method getAttr = lookupDeclaredMethod(adapter, "getAttribute",
                String.class);
        Object cookie = newAdapterCookie(adapter);

        // 로그 dedup 와 무관 — JUL 핸들러 격리만 위해 reset 후 진행
        resetLoggingDedup(support);

        try {
            try {
                setAttr.invoke(cookie, "SameSite", "Lax");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — setAttribute threw: "
                        + cause.getClass().getName() + ": "
                        + cause.getMessage());
                return;
            }

            Object value;
            try {
                value = getAttr.invoke(cookie, "SameSite");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — getAttribute threw: "
                        + cause.getClass().getName() + ": "
                        + cause.getMessage());
                return;
            }
            assertNull(value, FAILURE_SIGNATURE
                    + " — getAttribute(SameSite) expected null after"
                    + " setAttribute (no storage), got " + value);

            // unknown key 도 null
            Object unknown;
            try {
                unknown = getAttr.invoke(cookie, "no-such-attr");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — getAttribute(unknown) threw: "
                        + cause.getClass().getName() + ": "
                        + cause.getMessage());
                return;
            }
            assertNull(unknown, FAILURE_SIGNATURE
                    + " — getAttribute(unknown) expected null, got "
                    + unknown);
        } finally {
            resetLoggingDedup(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-3 — getAttributes() returns empty unmodifiable Map
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-013
     */
    @Test
    @DisplayName("FR-CONV-013 AC-3: getAttributes() returns empty unmodifiable Map")
    void shouldApplyNoOpWithWarnOnceForCookieAttributeApi_FRCONV013_AC3() {
        Class<?> adapter = lookupCookieAdapter();
        Class<?> support = lookupConverterSupport();

        Method setAttr = lookupDeclaredMethod(adapter, "setAttribute",
                String.class, String.class);
        Method getAll = lookupDeclaredMethod(adapter, "getAttributes");
        Object cookie = newAdapterCookie(adapter);

        resetLoggingDedup(support);
        try {
            try {
                setAttr.invoke(cookie, "SameSite", "Lax");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — setAttribute threw: "
                        + cause.getClass().getName() + ": "
                        + cause.getMessage());
                return;
            }

            Object result;
            try {
                result = getAll.invoke(cookie);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — getAttributes threw: "
                        + cause.getClass().getName() + ": "
                        + cause.getMessage());
                return;
            }

            assertNotNull(result, FAILURE_SIGNATURE
                    + " — getAttributes() returned null");
            assertTrue(result instanceof Map, FAILURE_SIGNATURE
                    + " — getAttributes() return type "
                    + result.getClass().getName());
            Map<?, ?> map = (Map<?, ?>) result;
            assertTrue(map.isEmpty(), FAILURE_SIGNATURE
                    + " — getAttributes() expected empty map, got " + map);

            // unmodifiable — put 호출 시 UnsupportedOperationException
            boolean unmodifiable = false;
            try {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Map raw = map;
                raw.put("k", "v");
            } catch (UnsupportedOperationException uoe) {
                unmodifiable = true;
            } catch (RuntimeException other) {
                fail(FAILURE_SIGNATURE + " — getAttributes() put threw "
                        + other.getClass().getName() + " instead of"
                        + " UnsupportedOperationException");
                return;
            }
            assertTrue(unmodifiable, FAILURE_SIGNATURE
                    + " — getAttributes() Map must be unmodifiable but put"
                    + " succeeded");
        } finally {
            resetLoggingDedup(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-4 — no internal Map storage (자체 attribute Map 미보관)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-013
     */
    @Test
    @DisplayName("FR-CONV-013 AC-4: adapter Cookie holds no internal attribute Map field")
    void shouldApplyNoOpWithWarnOnceForCookieAttributeApi_FRCONV013_AC4() {
        Class<?> adapter = lookupCookieAdapter();

        for (Field f : adapter.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                // static 상수/sentinel 은 인스턴스 상태 아님 — 제외
                continue;
            }
            Class<?> type = f.getType();
            assertFalse(Map.class.isAssignableFrom(type), FAILURE_SIGNATURE
                    + " — adapter Cookie must not hold a Map instance field"
                    + " (SPEC-15 정책 D 위반): "
                    + f.getName() + " : " + type.getName());
        }

        // getAttributes() 가 매 호출마다 동일 sentinel (또는 emptyMap) 을
        // 반환하는지 — 인스턴스별 새 Map 생성 시에도 mutation 차단되어야 하나,
        // 자체 보관 금지 = 동일 sentinel 반환 강제 (Collections.emptyMap()).
        Method getAll = lookupDeclaredMethod(adapter, "getAttributes");
        Object cookie = newAdapterCookie(adapter);
        Object r1;
        Object r2;
        try {
            r1 = getAll.invoke(cookie);
            r2 = getAll.invoke(cookie);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE + " — getAttributes threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
            return;
        }
        assertSame(r1, r2, FAILURE_SIGNATURE
                + " — getAttributes() should return same shared empty Map"
                + " (no per-call allocation = no internal storage)");
        assertSame(Collections.emptyMap(), r1, FAILURE_SIGNATURE
                + " — getAttributes() expected Collections.emptyMap()"
                + " (no internal Map storage), got "
                + r1.getClass().getName());
    }

    // -----------------------------------------------------------------
    // JUL Handler — in-test LogRecord collector (mocking 없음, §0.6)
    // -----------------------------------------------------------------

    /**
     * Minimal {@link Handler} that records emitted {@link LogRecord}
     * instances for assertion. Production code never uses this — test-only.
     *
     * @req FR-CONV-013
     */
    static final class RecordingHandler extends Handler {
        private final java.util.List<LogRecord> records =
                java.util.Collections.synchronizedList(
                        new java.util.ArrayList<>());

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() throws SecurityException {
            // no-op
        }

        int countAtLevel(Level level, String token) {
            assertNotNull(level, "level must not be null");
            int n = 0;
            synchronized (records) {
                for (LogRecord r : records) {
                    if (!level.equals(r.getLevel())) {
                        continue;
                    }
                    String msg = r.getMessage();
                    if (msg != null && msg.contains(token)) {
                        n++;
                    }
                }
            }
            return n;
        }
    }
}
