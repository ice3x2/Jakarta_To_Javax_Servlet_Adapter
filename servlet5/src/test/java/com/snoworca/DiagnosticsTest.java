package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-012 (폴백 가시성 — SPEC-8).
 *
 * Scope (T-PH004-11 — RED):
 *   - AC-1: java.util.logging.Logger 가 once-per-method dedup 로 warning 을
 *           emit 한다. 동일 methodKey 의 두 번째 호출은 추가 LogRecord 를
 *           발행하지 않는다.
 *   - AC-2: ServletAdapter.diagnostics() 가 시스템 프로퍼티
 *           com.snoworca.adapter.diagnostics=true 일 때 Map&lt;String, Long&gt;
 *           을 반환하고, opt-in 이 아닐 때는 empty Map 을 반환한다.
 *   - AC-3: incrementDiagnostics 가 thread-safe 하다 (ConcurrentHashMap
 *           기반). 멀티스레드에서 동일 키를 N 회 증가시킨 합계가 정확히 N.
 *
 * Red phase 의도:
 *   - T-PH004-12 (green) 에서 도입 예정인 ConverterSupport 의 로깅/diagnostics
 *     helper (LOGGER_NAME, DIAGNOSTICS_KEY, logFallbackOnce,
 *     incrementDiagnostics, diagnosticsSnapshot,
 *     initDiagnosticsFromSystemProperty, resetLoggingDedupForTesting,
 *     resetDiagnosticsForTesting) 및 ServletAdapter#diagnostics() 정적
 *     메서드가 현재 시점에는 부재한다.
 *   - 본 test 는 위 API 를 reflection 으로 lookup 하여 부재로 fail 시킨다
 *     (T-PH004-09 StrictModeTest 와 동일한 missing-helper RED 패턴).
 *
 * expected_failure_signature (sidecar):
 *   "expected: once-per-method dedup logger + opt-in diagnostics map, got:
 *    missing helpers"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-012-AC1-01 → _FRCONV012_AC1
 *   TC-REQ-FR-CONV-012-AC2-01 → _FRCONV012_AC2
 *   TC-REQ-FR-CONV-012-AC3-01 → _FRCONV012_AC3
 *
 * Mock 사용 없음 (§0.6) — reflection 으로 production helper 부재를 직접
 * 검출. JUL Handler 는 java.util.logging.Handler 의 실제 in-test 구현체로
 * mocking library 없이 LogRecord 수집.
 *
 * @req FR-CONV-012
 */
class DiagnosticsTest {

    private static final String CONVERTER_FQN = "adapter.common.ConverterSupport";
    private static final String SERVLET_ADAPTER_FQN = "com.snoworca.ServletAdapter";
    private static final String FAILURE_SIGNATURE =
            "expected: once-per-method dedup logger + opt-in diagnostics map,"
                    + " got: missing helpers";
    private static final String EXPECTED_LOGGER_NAME = "com.snoworca.servletadapter";
    private static final String DIAGNOSTICS_KEY = "com.snoworca.adapter.diagnostics";

    // -----------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-012
     */
    private static Class<?> lookupConverterSupport() {
        try {
            return Class.forName(CONVERTER_FQN, true,
                    DiagnosticsTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE + " — " + CONVERTER_FQN
                    + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-012
     */
    private static Class<?> lookupServletAdapter() {
        try {
            return Class.forName(SERVLET_ADAPTER_FQN, true,
                    DiagnosticsTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE + " — " + SERVLET_ADAPTER_FQN
                    + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-012
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
     * @req FR-CONV-012
     */
    private static String lookupLoggerName(Class<?> support) {
        try {
            Object value = support.getDeclaredField("LOGGER_NAME").get(null);
            assertNotNull(value, FAILURE_SIGNATURE
                    + " — ConverterSupport.LOGGER_NAME is null");
            return value.toString();
        } catch (NoSuchFieldException e) {
            fail(FAILURE_SIGNATURE + " — ConverterSupport.LOGGER_NAME"
                    + " field not declared");
        } catch (IllegalAccessException e) {
            fail(FAILURE_SIGNATURE + " — LOGGER_NAME inaccessible: " + e);
        }
        throw new AssertionError("unreachable");
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
     * @req FR-CONV-012
     */
    private static void resetDiagnostics(Class<?> support) {
        try {
            Method reset = support.getDeclaredMethod("resetDiagnosticsForTesting");
            reset.setAccessible(true);
            reset.invoke(null);
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE + " — resetDiagnosticsForTesting not declared");
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE + " — resetDiagnosticsForTesting threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // AC-1 — once-per-method dedup logger (WARN 첫 호출만 emit)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-012
     */
    @Test
    @DisplayName("FR-CONV-012 AC-1: java.util.logging once-per-method dedup — 두번째 호출은 emit 안 함")
    void shouldProvideFallbackVisibilityWithLoggingAndDiagnostics_FRCONV012_AC1() {
        Class<?> support = lookupConverterSupport();

        String loggerName = lookupLoggerName(support);
        assertEquals(EXPECTED_LOGGER_NAME, loggerName,
                FAILURE_SIGNATURE + " — LOGGER_NAME mismatch");

        Method logOnce = lookupStaticMethod(support, "logFallbackOnce",
                String.class, Level.class, String.class);

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

            String methodKey = "HttpSession.getValue@javax->jakarta";
            try {
                logOnce.invoke(null, methodKey, Level.WARNING,
                        "fallback B applied");
                logOnce.invoke(null, methodKey, Level.WARNING,
                        "fallback B applied");
                logOnce.invoke(null, methodKey, Level.WARNING,
                        "fallback B applied");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — logFallbackOnce threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            int matching = handler.countContaining(methodKey);
            assertEquals(1, matching, FAILURE_SIGNATURE
                    + " — expected exactly 1 LogRecord for methodKey, got "
                    + matching);
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParent);
            resetLoggingDedup(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-2 — ServletAdapter.diagnostics() opt-in 시스템 프로퍼티
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-012
     */
    @Test
    @DisplayName("FR-CONV-012 AC-2: ServletAdapter.diagnostics() opt-in 활성 시 Map 반환, 비활성 시 empty Map")
    void shouldProvideFallbackVisibilityWithLoggingAndDiagnostics_FRCONV012_AC2() {
        Class<?> support = lookupConverterSupport();
        Class<?> adapter = lookupServletAdapter();

        Method diagnostics = lookupStaticMethod(adapter, "diagnostics");
        Method initDiag = lookupStaticMethod(support,
                "initDiagnosticsFromSystemProperty");
        Method increment = lookupStaticMethod(support,
                "incrementDiagnostics", String.class);

        String previous = System.getProperty(DIAGNOSTICS_KEY);

        try {
            // -- opt-in ON 시: Map 반환 + 호출 카운트 노출
            System.setProperty(DIAGNOSTICS_KEY, "true");
            resetDiagnostics(support);
            try {
                initDiag.invoke(null);
                increment.invoke(null, "HttpSession.getValue@javax->jakarta");
                increment.invoke(null, "HttpSession.getValue@javax->jakarta");
                increment.invoke(null, "Cookie.setAttribute@jakarta->javax");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — opt-in setup threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            Object snapshotOn;
            try {
                snapshotOn = diagnostics.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — ServletAdapter.diagnostics() threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            assertNotNull(snapshotOn, FAILURE_SIGNATURE
                    + " — diagnostics() returned null while opt-in ON");
            assertTrue(snapshotOn instanceof Map, FAILURE_SIGNATURE
                    + " — diagnostics() return type "
                    + snapshotOn.getClass().getName());
            @SuppressWarnings("unchecked")
            Map<String, Long> mapOn = (Map<String, Long>) snapshotOn;
            assertEquals(Long.valueOf(2L),
                    mapOn.get("HttpSession.getValue@javax->jakarta"),
                    FAILURE_SIGNATURE
                            + " — expected HttpSession.getValue count=2, got "
                            + mapOn.get("HttpSession.getValue@javax->jakarta"));
            assertEquals(Long.valueOf(1L),
                    mapOn.get("Cookie.setAttribute@jakarta->javax"),
                    FAILURE_SIGNATURE
                            + " — expected Cookie.setAttribute count=1, got "
                            + mapOn.get("Cookie.setAttribute@jakarta->javax"));

            // -- opt-in OFF 시: empty Map 반환
            System.clearProperty(DIAGNOSTICS_KEY);
            resetDiagnostics(support);
            try {
                initDiag.invoke(null);
                increment.invoke(null, "HttpSession.getValue@javax->jakarta");
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — opt-out setup threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            Object snapshotOff;
            try {
                snapshotOff = diagnostics.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — diagnostics() threw on opt-out: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            assertNotNull(snapshotOff, FAILURE_SIGNATURE
                    + " — diagnostics() returned null while opt-in OFF");
            assertTrue(snapshotOff instanceof Map, FAILURE_SIGNATURE
                    + " — diagnostics() return type "
                    + snapshotOff.getClass().getName());
            assertTrue(((Map<?, ?>) snapshotOff).isEmpty(),
                    FAILURE_SIGNATURE
                            + " — diagnostics() should be empty when opt-in OFF, got "
                            + snapshotOff);
        } finally {
            if (previous == null) {
                System.clearProperty(DIAGNOSTICS_KEY);
            } else {
                System.setProperty(DIAGNOSTICS_KEY, previous);
            }
            resetDiagnostics(support);
        }
    }

    // -----------------------------------------------------------------
    // AC-3 — incrementDiagnostics thread-safe (concurrent increment 정확성)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-012
     */
    @Test
    @DisplayName("FR-CONV-012 AC-3: incrementDiagnostics thread-safe — N 스레드 × M 증가 = 합계 정확")
    void shouldProvideFallbackVisibilityWithLoggingAndDiagnostics_FRCONV012_AC3() {
        final int threadCount = 8;
        final int incrementsPerThread = 2_000;
        final long expectedTotal = (long) threadCount * incrementsPerThread;

        Class<?> support = lookupConverterSupport();
        final Method initDiag = lookupStaticMethod(support,
                "initDiagnosticsFromSystemProperty");
        final Method increment = lookupStaticMethod(support,
                "incrementDiagnostics", String.class);
        final Method snapshot = lookupStaticMethod(support, "diagnosticsSnapshot");

        String previous = System.getProperty(DIAGNOSTICS_KEY);
        System.setProperty(DIAGNOSTICS_KEY, "true");
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            resetDiagnostics(support);
            try {
                initDiag.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE
                        + " — initDiagnosticsFromSystemProperty threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }

            final String key = "HttpSession.getValueNames@javax->jakarta";
            final CountDownLatch start = new CountDownLatch(1);
            final CountDownLatch done = new CountDownLatch(threadCount);
            final AtomicInteger failures = new AtomicInteger(0);

            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < incrementsPerThread; i++) {
                            try {
                                increment.invoke(null, key);
                            } catch (InvocationTargetException ite) {
                                failures.incrementAndGet();
                                return;
                            }
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        failures.incrementAndGet();
                    } catch (IllegalAccessException iae) {
                        failures.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            try {
                boolean finished = done.await(30, TimeUnit.SECONDS);
                assertTrue(finished, FAILURE_SIGNATURE
                        + " — concurrent increments did not finish within 30s");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                fail(FAILURE_SIGNATURE + " — main thread interrupted: " + ie);
                return;
            }
            assertEquals(0, failures.get(), FAILURE_SIGNATURE
                    + " — increment threw on " + failures.get() + " calls");

            Object snap;
            try {
                snap = snapshot.invoke(null);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                fail(FAILURE_SIGNATURE + " — diagnosticsSnapshot threw: "
                        + cause.getClass().getName() + ": " + cause.getMessage());
                return;
            }
            assertNotNull(snap, FAILURE_SIGNATURE
                    + " — diagnosticsSnapshot returned null");
            assertTrue(snap instanceof Map, FAILURE_SIGNATURE
                    + " — diagnosticsSnapshot return type "
                    + snap.getClass().getName());
            @SuppressWarnings("unchecked")
            Map<String, Long> map = (Map<String, Long>) snap;
            Long actual = map.get(key);
            assertNotNull(actual, FAILURE_SIGNATURE
                    + " — snapshot missing key " + key);
            assertEquals(Long.valueOf(expectedTotal), actual, FAILURE_SIGNATURE
                    + " — expected " + expectedTotal + " total increments, got "
                    + actual + " (lost updates indicate non-thread-safe impl)");
        } finally {
            pool.shutdownNow();
            try {
                pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            if (previous == null) {
                System.clearProperty(DIAGNOSTICS_KEY);
            } else {
                System.setProperty(DIAGNOSTICS_KEY, previous);
            }
            resetDiagnostics(support);
        }
    }

    // -----------------------------------------------------------------
    // JUL Handler — in-test LogRecord collector (mocking 없음, §0.6)
    // -----------------------------------------------------------------

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
    }
}
