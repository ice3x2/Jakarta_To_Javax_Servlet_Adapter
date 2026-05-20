package com.snoworca;

import adapter.common.AdapterUnwrap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-003 (AdapterUnwrap, double-wrap avoidance)
 * + FR-CONV-004 (equals/hashCode 위임) at the *all-adapter* granularity for
 * the servlet5 module.
 *
 * Scope (T-PH004-03 — RED):
 *   - 모든 어댑터 클래스 (adapter.jakarta.servlet5.*, adapter.javax.servlet5.*)
 *     가 AdapterUnwrap<원본> 를 implements 함을 reflection 으로 검증.
 *   - ServletAdapter.adaptToJavax(adaptToJakarta(javaxObj)) == javaxObj 의
 *     == 동일성 (이중 wrap 회피 / pre-unwrap) 검증.
 *   - instanceof AdapterUnwrap + unwrap() 사용자 경로 검증.
 *   - 동일 원본을 두 번 wrap 한 어댑터 a, b 에 대해 equals 대칭 + hashCode
 *     일치 + delegate 위임 패턴 검증.
 *
 * Red phase 의도:
 *   - 현재 시점 (T-PH004-02 까지 완료) common 모듈에 AdapterUnwrap 인터페이스만
 *     존재하고, 모든 어댑터 클래스는 아직 implements 하지 않는다.
 *   - 어댑터들의 equals/hashCode 는 java.lang.Object 의 identity 기반이므로
 *     동일 원본 두 wrap 은 false / 다른 hash 를 낸다.
 *   - ServletAdapter.adaptToJavax(jakartaObj) 의 jakartaObj 가 본 시점에서는
 *     AdapterUnwrap 이 아니므로 pre-unwrap 분기가 존재하지 않는다.
 *   따라서 본 7 개 케이스는 모두 fail (red) 한다.
 *
 * expected_failure_signature (sidecar):
 *   "expected: AdapterUnwrap on all adapters + equals/hashCode + double-wrap
 *    avoidance, got: missing implementation"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-003-AC1-03 → _FRCONV003_AC1 (AC-1: AdapterUnwrap-가-여전히-존재 + jakarta-side implements 게이트)
 *   TC-REQ-FR-CONV-003-AC2-03 → _FRCONV003_AC2 (AC-2: ALL adapter classes implement)
 *   TC-REQ-FR-CONV-003-AC3-03 → _FRCONV003_AC3 (AC-3: round-trip == identity via pre-unwrap)
 *   TC-REQ-FR-CONV-003-AC4-03 → _FRCONV003_AC4 (AC-4: instanceof AdapterUnwrap + unwrap() user path)
 *   TC-REQ-FR-CONV-004-AC1-01 → _FRCONV004_AC1 (AC-1: equals symmetry)
 *   TC-REQ-FR-CONV-004-AC2-01 → _FRCONV004_AC2 (AC-2: hashCode equality)
 *   TC-REQ-FR-CONV-004-AC3-01 → _FRCONV004_AC3 (AC-3: reflective equals/hashCode delegation contract)
 *
 * Mock 사용 없음 (§0.6).
 *
 * @req FR-CONV-003
 * @req FR-CONV-004
 */
class AdapterUnwrapAllClassesTest {

    private static final String PKG_JAKARTA = "adapter.jakarta.servlet5";
    private static final String PKG_JAVAX = "adapter.javax.servlet5";

    // -----------------------------------------------------------------
    // Reflection helpers (concrete-adapter enumeration)
    // -----------------------------------------------------------------

    /**
     * cwd 의 build/classes 디렉토리를 walk 해 지정 base package 하위의 *adapter*
     * 클래스만 enumerate. Mock / fixture 가 아닌 실제 컴파일된 어댑터를 대상으로
     * 함. nested / anonymous / interface / abstract / enum 클래스 제외.
     */
    private static List<Class<?>> scanAdapterClasses(String basePackage) {
        List<Class<?>> out = new ArrayList<>();
        try {
            ClassLoader cl = AdapterUnwrapAllClassesTest.class.getClassLoader();
            String pathForm = basePackage.replace('.', '/');
            Enumeration<URL> resources = cl.getResources(pathForm);
            while (resources.hasMoreElements()) {
                URL u = resources.nextElement();
                if (!"file".equals(u.getProtocol())) continue;
                File dir = new File(u.toURI());
                walk(dir, basePackage, out);
            }
        } catch (Exception e) {
            fail("scanAdapterClasses(" + basePackage + ") failed: " + e);
        }
        return out;
    }

    private static void walk(File dir, String pkg, List<Class<?>> out) {
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] entries = dir.listFiles();
        if (entries == null) return;
        for (File f : entries) {
            if (f.isDirectory()) {
                walk(f, pkg + "." + f.getName(), out);
            } else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String simple = f.getName().substring(0, f.getName().length() - ".class".length());
                // production 어댑터 enumeration 만 — test class 디렉토리는 multi-classpath
                // URL 로 함께 노출될 수 있으므로 "*Test" 접미사 클래스는 제외.
                if (simple.endsWith("Test")) continue;
                String fqn = pkg + "." + simple;
                try {
                    Class<?> c = Class.forName(fqn, false, AdapterUnwrapAllClassesTest.class.getClassLoader());
                    if (c.isInterface()) continue;
                    if (c.isEnum()) continue;
                    if (java.lang.reflect.Modifier.isAbstract(c.getModifiers())) continue;
                    out.add(c);
                } catch (Throwable ignore) {
                    // 로딩 실패는 enumerate 단계에서 무시 — 본 테스트는 implements
                    // 여부에 집중하고, 로딩 실패는 별도 인프라 문제다.
                }
            }
        }
    }

    /**
     * jakarta-side / javax-side 양쪽 어댑터를 모아 AdapterUnwrap 미구현 클래스
     * fqn 리스트로 변환한다. 본 helper 는 FR-CONV-003 AC-1, AC-2 양쪽 케이스에서
     * 공용된다.
     */
    private static List<String> findAdaptersMissingUnwrap(String pkg) {
        List<Class<?>> all = scanAdapterClasses(pkg);
        if (all.isEmpty()) {
            fail("expected: " + pkg + " contains concrete adapter classes on classpath, got: 0 — "
                + "build/classes 의 .class 가 enumeration 안 됨 (인프라 문제 의심)");
        }
        List<String> missing = new ArrayList<>();
        for (Class<?> c : all) {
            if (!AdapterUnwrap.class.isAssignableFrom(c)) {
                missing.add(c.getName());
            }
        }
        return missing;
    }

    // -----------------------------------------------------------------
    // FR-CONV-003 AC-1 — jakarta-side adapters implement AdapterUnwrap
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-1: jakarta-side 모든 어댑터 클래스가 AdapterUnwrap 를 implements 한다 (servlet5)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC1() {
        List<String> missing = findAdaptersMissingUnwrap(PKG_JAKARTA);
        assertTrue(missing.isEmpty(),
            "expected: ALL jakarta-side adapters implement AdapterUnwrap, got: missing=" + missing);
    }

    // -----------------------------------------------------------------
    // FR-CONV-003 AC-2 — javax-side adapters implement AdapterUnwrap
    // (이쪽이 SRS AC-2 의 "모든 어댑터 클래스" 범위 — adapter.javax.servletN.*)
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-2: javax-side 모든 어댑터 클래스가 AdapterUnwrap 를 implements 한다 (servlet5)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC2() {
        List<String> missing = findAdaptersMissingUnwrap(PKG_JAVAX);
        assertTrue(missing.isEmpty(),
            "expected: ALL javax-side adapters implement AdapterUnwrap, got: missing=" + missing);
    }

    // -----------------------------------------------------------------
    // FR-CONV-003 AC-3 — ServletAdapter round-trip identity (pre-unwrap)
    //   "ServletAdapter.adaptToJavax(adaptToJakarta(javaxObj)) == javaxObj"
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-3: adaptToJavax(adaptToJakarta(javaxObj)) == javaxObj (이중 wrap 회피, servlet5)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC3() {
        javax.servlet.http.HttpServletRequest origin = (javax.servlet.http.HttpServletRequest)
            java.lang.reflect.Proxy.newProxyInstance(
                AdapterUnwrapAllClassesTest.class.getClassLoader(),
                new Class<?>[]{ javax.servlet.http.HttpServletRequest.class },
                (proxy, method, args) -> null);
        jakarta.servlet.http.HttpServletRequest wrappedJakarta = ServletAdapter.adaptToJakarta(origin);
        javax.servlet.http.HttpServletRequest roundTripped = ServletAdapter.adaptToJavax(wrappedJakarta);
        assertSame(origin, roundTripped,
            "expected: adaptToJavax(adaptToJakarta(origin)) == origin (FR-CONV-003 AC-3 pre-unwrap),"
                + " got: new wrapper instance (double-wrap not avoided)");
    }

    // -----------------------------------------------------------------
    // FR-CONV-003 AC-4 — instanceof AdapterUnwrap + unwrap() user path
    //   "사용자가 어댑터 객체에 대해 instanceof AdapterUnwrap 확인 후 unwrap() 호출"
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-4: 어댑터 instance 가 AdapterUnwrap 이며 unwrap() 으로 원본 반환 (servlet5)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC4() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet5.http.Cookie wrapper = new adapter.javax.servlet5.http.Cookie(origin);
        if (!(wrapper instanceof AdapterUnwrap)) {
            fail("expected: adapter.javax.servlet5.http.Cookie is instanceof AdapterUnwrap,"
                + " got: not implementing AdapterUnwrap (FR-CONV-003 AC-4)");
        }
        Object unwrapped = ((AdapterUnwrap<?>) wrapper).unwrap();
        assertSame(origin, unwrapped,
            "expected: unwrap() returns the wrapped origin, got: " + unwrapped);
    }

    // -----------------------------------------------------------------
    // FR-CONV-004 AC-1 — equals symmetry between two wraps of same origin
    //   "동일 원본을 두 번 wrap 한 a, b 가 a.equals(b) 와 b.equals(a) 모두 true"
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-004 AC-1: 동일 원본의 두 wrap a,b 가 a.equals(b) / b.equals(a) 양방 true (servlet5)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV004_AC1() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet5.http.Cookie a = new adapter.javax.servlet5.http.Cookie(origin);
        adapter.javax.servlet5.http.Cookie b = new adapter.javax.servlet5.http.Cookie(origin);
        assertNotSame(a, b, "fixture: a, b must be distinct adapter instances");
        assertTrue(a.equals(b),
            "expected: a.equals(b) true (FR-CONV-004 AC-1 — equals delegates to unwrap), got: false");
        assertTrue(b.equals(a),
            "expected: b.equals(a) true (symmetry), got: false");
    }

    // -----------------------------------------------------------------
    // FR-CONV-004 AC-2 — hashCode equality between two wraps of same origin
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-004 AC-2: 동일 원본의 두 wrap a,b 가 a.hashCode() == b.hashCode() (servlet5)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV004_AC2() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet5.http.Cookie a = new adapter.javax.servlet5.http.Cookie(origin);
        adapter.javax.servlet5.http.Cookie b = new adapter.javax.servlet5.http.Cookie(origin);
        assertEquals(a.hashCode(), b.hashCode(),
            "expected: a.hashCode() == b.hashCode() (FR-CONV-004 AC-2 — hashCode delegates to unwrap),"
                + " got: a=" + a.hashCode() + " b=" + b.hashCode());
    }

    // -----------------------------------------------------------------
    // FR-CONV-004 AC-3 — Reflective contract: equals/hashCode 위임 패턴
    //   "Reflective contract test 가 equals/hashCode 위임을 검증한다"
    //   adapter 와 원본 사이의 equals/hashCode 위임 패턴을 reflective 로 확인:
    //   a.equals(origin) → this.unwrap().equals(origin) → origin.equals(origin) → true
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-004 AC-3: equals/hashCode 위임 패턴 reflective 검증 (a.equals(origin), a.hashCode()==origin.hashCode())")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV004_AC3() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet5.http.Cookie a = new adapter.javax.servlet5.http.Cookie(origin);

        // unwrap method 존재 (interface contract reflective 점검)
        Method unwrapMethod;
        try {
            unwrapMethod = a.getClass().getMethod("unwrap");
        } catch (NoSuchMethodException e) {
            fail("expected: public unwrap() method via AdapterUnwrap interface,"
                + " got: NoSuchMethodException (FR-CONV-004 AC-3)");
            return;
        }
        assertNotNull(unwrapMethod);
        assertEquals(0, unwrapMethod.getParameterCount(),
            "expected: unwrap() takes 0 args, got: " + unwrapMethod.getParameterCount());

        // equals 위임 패턴: a.equals(origin) — origin 은 AdapterUnwrap 가 아니므로
        // SPEC-6 패턴의 마지막 분기 (this.unwrap().equals(o)) 가 발동되어 true 여야 함.
        assertTrue(a.equals(origin),
            "expected: a.equals(origin) true (this.unwrap().equals(origin) — FR-CONV-004 AC-3),"
                + " got: false (identity equals path — 위임 미구현)");

        // hashCode 위임 패턴: a.hashCode() == origin.hashCode()
        assertEquals(origin.hashCode(), a.hashCode(),
            "expected: a.hashCode() == origin.hashCode() (SPEC-6 위임 패턴),"
                + " got: a=" + a.hashCode() + " origin=" + origin.hashCode());
    }
}
