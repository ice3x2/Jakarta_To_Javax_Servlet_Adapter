package com.snoworca;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import adapter.common.ConverterSupport;

/**
 * TDD red — NFR-CONV-001 (스레드 안전성 + HttpUpgradeHandler ThreadLocal lock-in / BM10).
 *
 * <p>AC-1: 모든 servlet5 어댑터 클래스의 인스턴스 필드는 {@code final} 또는 thread-safe
 * 타입이어야 한다.<br>
 * AC-2: {@link ConverterSupport} 의 LOGGING_DEDUP / DIAGNOSTICS 가 각각
 * {@link ConcurrentHashMap} + {@link AtomicLong} 으로 thread-safe 보장되어야 한다.<br>
 * AC-3: {@code HttpUpgradeHandler.setJakartaUpgradeHandlerClass(...)} 가 호출된 스레드와
 * 다른 스레드에서 {@code HttpUpgradeHandler.<init>()} 가 실행될 경우 ThreadLocal 값이
 * 누설되지 않아야 한다 (현 코드의 단일 스레드 동기 호출 가정을 lock-in).
 */
class ThreadSafetyTest {

    private static final String SERVLET5_ADAPTER_ROOT = "adapter";
    private static final Set<String> ALLOWED_THREAD_SAFE_TYPES = new HashSet<>(Arrays.asList(
            "java.util.concurrent.ConcurrentHashMap",
            "java.util.concurrent.ConcurrentMap",
            "java.util.concurrent.atomic.AtomicLong",
            "java.util.concurrent.atomic.AtomicInteger",
            "java.util.concurrent.atomic.AtomicBoolean",
            "java.util.concurrent.atomic.AtomicReference"
    ));

    // ------------------------------------------------------------------------
    // AC-1
    // ------------------------------------------------------------------------
    /**
     * @req NFR-CONV-001
     */
    @Test
    @DisplayName("AC-1: 모든 servlet5 어댑터 인스턴스 필드는 final 또는 thread-safe 타입 (NFR-CONV-001)")
    void shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn_NFRCONV001_AC1() throws Exception {
        List<Class<?>> adapterClasses = loadServlet5AdapterClasses();
        assertTrue(adapterClasses.size() >= 10,
                "expected: >=10 adapter classes discovered under '" + SERVLET5_ADAPTER_ROOT
                        + "', found: " + adapterClasses.size());

        List<String> violations = new ArrayList<>();
        for (Class<?> cls : adapterClasses) {
            for (Field f : cls.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods)) {
                    continue; // 인스턴스 필드만 검사
                }
                if (Modifier.isFinal(mods)) {
                    continue; // final → OK
                }
                if (Modifier.isVolatile(mods)) {
                    continue; // volatile → thread-safe 보장
                }
                String typeName = f.getType().getName();
                if (ALLOWED_THREAD_SAFE_TYPES.contains(typeName)) {
                    continue; // ConcurrentMap/Atomic* 타입 → thread-safe
                }
                violations.add(cls.getName() + "." + f.getName()
                        + " (type=" + typeName + ", mods=" + Modifier.toString(mods) + ")");
            }
        }

        if (!violations.isEmpty()) {
            fail("expected: every servlet5 adapter instance field is `final` or thread-safe, "
                    + "got: mutable field or non-thread-safe map. violations="
                    + violations);
        }
    }

    // ------------------------------------------------------------------------
    // AC-2
    // ------------------------------------------------------------------------
    /**
     * @req NFR-CONV-001
     */
    @Test
    @DisplayName("AC-2: ConverterSupport dedup/diagnostics 는 ConcurrentHashMap + AtomicLong (NFR-CONV-001)")
    void shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn_NFRCONV001_AC2() throws Exception {
        // 1) LOGGING_DEDUP 필드 검증
        Field dedup = ConverterSupport.class.getDeclaredField("LOGGING_DEDUP");
        dedup.setAccessible(true);
        Object dedupVal = dedup.get(null);
        assertNotNull(dedupVal, "expected: ConverterSupport.LOGGING_DEDUP non-null");
        assertTrue(dedupVal instanceof ConcurrentMap,
                "expected: ConverterSupport.LOGGING_DEDUP instanceof ConcurrentMap, "
                        + "got: " + dedupVal.getClass().getName());
        assertTrue(Modifier.isStatic(dedup.getModifiers())
                        && Modifier.isFinal(dedup.getModifiers()),
                "expected: LOGGING_DEDUP static final, got: "
                        + Modifier.toString(dedup.getModifiers()));

        // 2) DIAGNOSTICS 필드 검증
        Field diag = ConverterSupport.class.getDeclaredField("DIAGNOSTICS");
        diag.setAccessible(true);
        Object diagVal = diag.get(null);
        assertNotNull(diagVal, "expected: ConverterSupport.DIAGNOSTICS non-null");
        assertTrue(diagVal instanceof ConcurrentMap,
                "expected: ConverterSupport.DIAGNOSTICS instanceof ConcurrentMap, "
                        + "got: " + diagVal.getClass().getName());

        // 3) 동시 increment → AtomicLong 누적 정확성 (thread-safe 보장 회귀 잠금)
        ConverterSupport.resetDiagnosticsForTesting();
        System.setProperty(ConverterSupport.DIAGNOSTICS_KEY, "true");
        try {
            ConverterSupport.initDiagnosticsFromSystemProperty();
            final String key = "ThreadSafetyTest.AC2.bumpKey";
            final int threads = 8;
            final int iterations = 1000;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    for (int j = 0; j < iterations; j++) {
                        ConverterSupport.incrementDiagnostics(key);
                    }
                }));
            }
            start.countDown();
            for (Future<?> f : futures) {
                f.get(30, TimeUnit.SECONDS);
            }
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS),
                    "expected: diagnostics workers terminate within 10s");
            Map<String, Long> snapshot = ConverterSupport.diagnosticsSnapshot();
            Long actual = snapshot.get(key);
            assertNotNull(actual, "expected: diagnostics snapshot contains key '" + key + "'");
            assertEquals((long) threads * iterations, actual.longValue(),
                    "expected: AtomicLong increments preserve all "
                            + (threads * iterations)
                            + " concurrent increments, got: " + actual
                            + " — counter type is not thread-safe AtomicLong");
        } finally {
            System.clearProperty(ConverterSupport.DIAGNOSTICS_KEY);
            ConverterSupport.resetDiagnosticsForTesting();
        }

        // 4) DIAGNOSTICS value 타입 entry 검증 — AtomicLong 만 사용
        ConverterSupport.resetDiagnosticsForTesting();
        System.setProperty(ConverterSupport.DIAGNOSTICS_KEY, "true");
        try {
            ConverterSupport.initDiagnosticsFromSystemProperty();
            ConverterSupport.incrementDiagnostics("ThreadSafetyTest.AC2.typeProbe");
            @SuppressWarnings("unchecked")
            ConcurrentMap<String, Object> raw = (ConcurrentMap<String, Object>) diagVal;
            Object entry = raw.get("ThreadSafetyTest.AC2.typeProbe");
            assertNotNull(entry, "expected: DIAGNOSTICS contains probed key");
            assertTrue(entry instanceof AtomicLong,
                    "expected: DIAGNOSTICS values are AtomicLong, "
                            + "got: " + entry.getClass().getName());
        } finally {
            System.clearProperty(ConverterSupport.DIAGNOSTICS_KEY);
            ConverterSupport.resetDiagnosticsForTesting();
        }
    }

    // ------------------------------------------------------------------------
    // AC-3
    // ------------------------------------------------------------------------
    /**
     * @req NFR-CONV-001
     */
    @Test
    @DisplayName("AC-3: HttpUpgradeHandler ThreadLocal cross-thread 누설 차단 (BM10) (NFR-CONV-001)")
    void shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn_NFRCONV001_AC3() throws Exception {
        // Step 1: 메인 스레드에서 ThreadLocal seam 설정.
        adapter.javax.servlet5.http.HttpUpgradeHandler.setJakartaUpgradeHandlerClass(
                DummyJakartaUpgradeHandler.class);

        // Step 2: 다른 스레드에서 <init>() 호출 시 ThreadLocal 이 누설되지 않아야 함.
        //         메인 스레드에서 설정한 ThreadLocal 값은 다른 스레드에서 보이지 않으므로
        //         IllegalStateException 이 발생해야 한다.
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<?> task = pool.submit(() -> {
                try {
                    new adapter.javax.servlet5.http.HttpUpgradeHandler();
                    fail("expected: cross-thread <init>() throws IllegalStateException "
                            + "(ThreadLocal must NOT leak across threads), got: success");
                } catch (IllegalStateException expected) {
                    // OK — ThreadLocal 누설 없음
                } catch (Exception e) {
                    throw new RuntimeException(
                            "expected: IllegalStateException on cross-thread instantiation, "
                                    + "got: " + e.getClass().getName() + " — " + e.getMessage(), e);
                }
            });
            task.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        // Step 3: 메인 스레드에서는 여전히 같은 ThreadLocal 값으로 정상 instantiation 가능.
        adapter.javax.servlet5.http.HttpUpgradeHandler instance =
                new adapter.javax.servlet5.http.HttpUpgradeHandler();
        assertNotNull(instance, "expected: same-thread sync <init>() succeeds with seeded ThreadLocal");
        assertNotNull(instance.getHttpUpgradeHandler(),
                "expected: wrapped jakarta upgrade handler initialized");

        // Step 4: 같은 스레드에서 두 번째 호출은 ThreadLocal 이 비워졌으므로 fail 해야 함
        //         (단일 스레드 동기 1회 가정 lock-in).
        assertThrows(IllegalStateException.class,
                () -> new adapter.javax.servlet5.http.HttpUpgradeHandler(),
                "expected: ThreadLocal cleared after each same-thread sync instantiation "
                        + "(single-thread synchronous invocation lock-in)");
    }

    /**
     * Minimal jakarta.servlet.http.HttpUpgradeHandler implementation used only by AC-3
     * to drive the ThreadLocal-based instantiation flow.
     */
    public static class DummyJakartaUpgradeHandler
            implements jakarta.servlet.http.HttpUpgradeHandler {
        public DummyJakartaUpgradeHandler() {
        }

        @Override
        public void init(jakarta.servlet.http.WebConnection wc) {
        }

        @Override
        public void destroy() {
        }
    }

    // ------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------

    private static List<Class<?>> loadServlet5AdapterClasses() throws Exception {
        List<Class<?>> result = new ArrayList<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        java.util.Enumeration<URL> roots = cl.getResources(SERVLET5_ADAPTER_ROOT);
        while (roots.hasMoreElements()) {
            URL url = roots.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                File rootDir = new File(url.toURI());
                if (rootDir.isDirectory()) {
                    collectClassesFromDir(rootDir, SERVLET5_ADAPTER_ROOT, result);
                }
            } else if ("jar".equals(protocol)) {
                String spec = url.getPath();
                int sep = spec.indexOf('!');
                String jarPath = sep > 0 ? spec.substring(0, sep) : spec;
                if (jarPath.startsWith("file:")) {
                    jarPath = jarPath.substring("file:".length());
                }
                collectClassesFromJar(jarPath, result);
            }
        }
        // 검사 대상은 servlet5 모듈의 adapter.jakarta.servlet5.* / adapter.javax.servlet5.*
        // (common 모듈의 adapter.common.* 은 변환 헬퍼이므로 제외).
        List<Class<?>> filtered = new ArrayList<>();
        for (Class<?> c : result) {
            String n = c.getName();
            if (n.startsWith("adapter.jakarta.servlet5.")
                    || n.startsWith("adapter.javax.servlet5.")) {
                if (c.isInterface()) continue;
                if (c.isAnnotation()) continue;
                if (c.isEnum()) continue;
                if (c.isAnonymousClass()) continue;
                filtered.add(c);
            }
        }
        return filtered;
    }

    private static void collectClassesFromDir(File dir, String pkg, List<Class<?>> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectClassesFromDir(f, pkg + "." + f.getName(), out);
            } else if (f.getName().endsWith(".class")) {
                String simple = f.getName().substring(0, f.getName().length() - ".class".length());
                String className = pkg + "." + simple;
                tryAddClass(className, out);
            }
        }
    }

    private static void collectClassesFromJar(String jarPath, List<Class<?>> out) {
        try (JarFile jar = new JarFile(jarPath)) {
            java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();
                if (!name.endsWith(".class")) continue;
                if (!name.startsWith(SERVLET5_ADAPTER_ROOT + "/")) continue;
                String className = name.substring(0, name.length() - ".class".length())
                        .replace('/', '.');
                tryAddClass(className, out);
            }
        } catch (Exception ignored) {
            // jar 접근 실패는 무시 (file 경로로 이미 수집되었거나 test classpath 외부)
        }
    }

    private static void tryAddClass(String className, List<Class<?>> out) {
        try {
            Class<?> c = Class.forName(className, false,
                    Thread.currentThread().getContextClassLoader());
            out.add(c);
        } catch (Throwable ignored) {
            // load 실패 클래스는 검사 대상에서 제외 (테스트 안정성)
        }
    }
}
