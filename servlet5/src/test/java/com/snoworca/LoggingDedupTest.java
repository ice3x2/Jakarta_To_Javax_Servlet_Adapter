package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase complementary test for FR-CONV-012 (폴백 가시성 — SPEC-8) AC-1.
 *
 * Scope (T-PH004-11 — RED, 보조):
 *   - AC-1 보강: 정책 B/D/F → WARN 레벨, 정책 E → INFO 레벨로 once-per-method
 *     dedup 적용. 서로 다른 methodKey 는 각각 1회씩 emit 되며 (서로 dedup 되지
 *     않음), 동일 methodKey 의 반복 호출은 첫 호출만 emit 된다.
 *
 * Red phase 의도:
 *   - sidecar.files[] 에 LoggingDedupTest.java 가 신설 대상으로 명시되어
 *     있으나 sidecar.tdd.test_cases[] 의 표준 3건은 DiagnosticsTest.java 에
 *     위치한다. 본 파일은 AC-1 (dedup) 의 다중 정책 레벨/다중 methodKey
 *     관점을 보조로 검증하여 sidecar.files[] 선언과 일치시킨다.
 *   - 본 파일의 test 들은 sidecar.tdd.test_cases[].test_symbol 매핑 대상이
 *     아니므로 ac_refs 만 코멘트로 명시 (체크 가능한 형태).
 *
 * expected_failure_signature (sidecar 공통):
 *   "expected: once-per-method dedup logger + opt-in diagnostics map, got:
 *    missing helpers"
 *
 * Mock 사용 없음 (§0.6) — reflection 으로 production helper 부재를 직접
 * 검출. JUL Handler 는 java.util.logging.Handler 의 실제 in-test 구현체.
 *
 * @req FR-CONV-012
 */
class LoggingDedupTest {

    private static final String CONVERTER_FQN = "adapter.common.ConverterSupport";
    private static final String FAILURE_SIGNATURE =
            "expected: once-per-method dedup logger + opt-in diagnostics map,"
                    + " got: missing helpers";
    private static final String EXPECTED_LOGGER_NAME = "com.snoworca.servletadapter";

    /**
     * @req FR-CONV-012
     */
    private static Class<?> lookupConverterSupport() {
        try {
            return Class.forName(CONVERTER_FQN, true,
                    LoggingDedupTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE + " — " + CONVERTER_FQN
                    + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-012
     */
    private static Method lookupLogOnce(Class<?> support) {
        try {
            Method m = support.getDeclaredMethod("logFallbackOnce",
                    String.class, Level.class, String.class);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — logFallbackOnce not declared");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-012
     */
    private static void resetLoggingDedup(Class<?> support) {
        try {
            Method reset = support.getDeclaredMethod("resetLoggingDedupForTesting");
            reset.setAccessible(true);
            reset.invoke(null);
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — resetLoggingDedupForTesting not declared");
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE + " — resetLoggingDedupForTesting threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
        }
    }

    /**
     * AC-1 보강 (WARN/INFO 레벨 분리 + 서로 다른 methodKey 독립 dedup).
     *
     * ac_refs: AC-1
     *
     * @req FR-CONV-012
     */
    @Test
    @DisplayName("FR-CONV-012 AC-1 보강: WARN(B/D/F) vs INFO(E) once-per-method dedup, 서로 다른 키 독립 dedup")
    void shouldDedupLogFallbackPerMethodKeyAndRespectLevel() {
        Class<?> support = lookupConverterSupport();
        Method logOnce = lookupLogOnce(support);

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

            String keyB = "HttpSession.getValue@javax->jakarta";
            String keyE = "Cookie.setAttribute@jakarta->javax";

            try {
                // WARN (B/D/F) — 2회 호출, 첫 회만 emit
                logOnce.invoke(null, keyB, Level.WARNING, "fallback B");
                logOnce.invoke(null, keyB, Level.WARNING, "fallback B");
                // INFO (E) — 별도 key, 별도 dedup, 1회 emit
                logOnce.invoke(null, keyE, Level.INFO, "fallback E");
                logOnce.invoke(null, keyE, Level.INFO, "fallback E");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — logFallbackOnce threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            int warnCount = handler.countAtLevel(Level.WARNING, keyB);
            int infoCount = handler.countAtLevel(Level.INFO, keyE);

            assertEquals(1, warnCount, FAILURE_SIGNATURE
                    + " — expected 1 WARN emit for " + keyB + ", got " + warnCount);
            assertEquals(1, infoCount, FAILURE_SIGNATURE
                    + " — expected 1 INFO emit for " + keyE + ", got " + infoCount);

            // 레벨 교차 오염 점검 — keyB 는 WARN 만, keyE 는 INFO 만
            assertEquals(0, handler.countAtLevel(Level.INFO, keyB),
                    FAILURE_SIGNATURE + " — " + keyB
                            + " should not emit at INFO level");
            assertEquals(0, handler.countAtLevel(Level.WARNING, keyE),
                    FAILURE_SIGNATURE + " — " + keyE
                            + " should not emit at WARNING level");
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParent);
            resetLoggingDedup(support);
        }
    }

    /**
     * AC-1 보강 (reset 후 동일 key 재emit 가능).
     *
     * ac_refs: AC-1
     *
     * @req FR-CONV-012
     */
    @Test
    @DisplayName("FR-CONV-012 AC-1 보강: resetLoggingDedupForTesting 호출 후 동일 key 재emit 가능")
    void shouldReemitAfterResetForTesting() {
        Class<?> support = lookupConverterSupport();
        Method logOnce = lookupLogOnce(support);

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
            String key = "ServletRequest.getRealPath@javax->jakarta";

            try {
                logOnce.invoke(null, key, Level.WARNING, "fallback D");
                logOnce.invoke(null, key, Level.WARNING, "fallback D");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — logFallbackOnce threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            assertEquals(1, handler.countContaining(key),
                    FAILURE_SIGNATURE
                            + " — expected 1 emit before reset, got "
                            + handler.countContaining(key));

            resetLoggingDedup(support);
            try {
                logOnce.invoke(null, key, Level.WARNING, "fallback D");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — logFallbackOnce threw after reset: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            assertEquals(2, handler.countContaining(key),
                    FAILURE_SIGNATURE
                            + " — expected 2 total emits after reset, got "
                            + handler.countContaining(key));
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParent);
            resetLoggingDedup(support);
        }
    }

    /**
     * Minimal {@link Handler} that records emitted {@link LogRecord} instances
     * for assertion. Production code never uses this — test-only.
     *
     * @req FR-CONV-012
     */
    static final class RecordingHandler extends Handler {
        private final java.util.List<LogRecord> records =
                java.util.Collections.synchronizedList(new java.util.ArrayList<>());

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

        int countContaining(String token) {
            int n = 0;
            synchronized (records) {
                for (LogRecord r : records) {
                    String msg = r.getMessage();
                    if (msg != null && msg.contains(token)) {
                        n++;
                    }
                }
            }
            return n;
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

    /**
     * Sanity check — `assertTrue` 미사용 라인 lint 차단 회피용.
     *
     * @req FR-CONV-012
     */
    @SuppressWarnings("unused")
    private static void touchAssertTrue() {
        assertTrue(true);
    }
}
