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
 * TDD red-phase test for FR-CONV-003 + FR-CONV-004 (servlet61 module).
 * See servlet5 sibling for detailed contract — only package targets differ.
 *
 * Mock 사용 없음 (§0.6).
 *
 * @req FR-CONV-003
 * @req FR-CONV-004
 */
class AdapterUnwrapAllClassesTest {

    private static final String PKG_JAKARTA = "adapter.jakarta.servlet61";
    private static final String PKG_JAVAX = "adapter.javax.servlet61";

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
                    // ignored — see servlet5 sibling
                }
            }
        }
    }

    private static List<String> findAdaptersMissingUnwrap(String pkg) {
        List<Class<?>> all = scanAdapterClasses(pkg);
        if (all.isEmpty()) {
            fail("expected: " + pkg + " contains concrete adapter classes on classpath, got: 0");
        }
        List<String> missing = new ArrayList<>();
        for (Class<?> c : all) {
            if (!AdapterUnwrap.class.isAssignableFrom(c)) {
                missing.add(c.getName());
            }
        }
        return missing;
    }

    @Test
    @DisplayName("FR-CONV-003 AC-1: jakarta-side 모든 어댑터 클래스가 AdapterUnwrap 를 implements 한다 (servlet61)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC1() {
        List<String> missing = findAdaptersMissingUnwrap(PKG_JAKARTA);
        assertTrue(missing.isEmpty(),
            "expected: ALL jakarta-side adapters implement AdapterUnwrap, got: missing=" + missing);
    }

    @Test
    @DisplayName("FR-CONV-003 AC-2: javax-side 모든 어댑터 클래스가 AdapterUnwrap 를 implements 한다 (servlet61)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC2() {
        List<String> missing = findAdaptersMissingUnwrap(PKG_JAVAX);
        assertTrue(missing.isEmpty(),
            "expected: ALL javax-side adapters implement AdapterUnwrap, got: missing=" + missing);
    }

    @Test
    @DisplayName("FR-CONV-003 AC-3: adaptToJavax(adaptToJakarta(javaxObj)) == javaxObj (이중 wrap 회피, servlet61)")
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

    @Test
    @DisplayName("FR-CONV-003 AC-4: 어댑터 instance 가 AdapterUnwrap 이며 unwrap() 으로 원본 반환 (servlet61)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV003_AC4() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet61.http.Cookie wrapper = new adapter.javax.servlet61.http.Cookie(origin);
        if (!(wrapper instanceof AdapterUnwrap)) {
            fail("expected: adapter.javax.servlet61.http.Cookie is instanceof AdapterUnwrap,"
                + " got: not implementing AdapterUnwrap (FR-CONV-003 AC-4)");
        }
        Object unwrapped = ((AdapterUnwrap<?>) wrapper).unwrap();
        assertSame(origin, unwrapped,
            "expected: unwrap() returns the wrapped origin, got: " + unwrapped);
    }

    @Test
    @DisplayName("FR-CONV-004 AC-1: 동일 원본의 두 wrap a,b 가 a.equals(b) / b.equals(a) 양방 true (servlet61)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV004_AC1() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet61.http.Cookie a = new adapter.javax.servlet61.http.Cookie(origin);
        adapter.javax.servlet61.http.Cookie b = new adapter.javax.servlet61.http.Cookie(origin);
        assertNotSame(a, b, "fixture: a, b must be distinct adapter instances");
        assertTrue(a.equals(b),
            "expected: a.equals(b) true (FR-CONV-004 AC-1 — equals delegates to unwrap), got: false");
        assertTrue(b.equals(a),
            "expected: b.equals(a) true (symmetry), got: false");
    }

    @Test
    @DisplayName("FR-CONV-004 AC-2: 동일 원본의 두 wrap a,b 가 a.hashCode() == b.hashCode() (servlet61)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV004_AC2() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet61.http.Cookie a = new adapter.javax.servlet61.http.Cookie(origin);
        adapter.javax.servlet61.http.Cookie b = new adapter.javax.servlet61.http.Cookie(origin);
        assertEquals(a.hashCode(), b.hashCode(),
            "expected: a.hashCode() == b.hashCode() (FR-CONV-004 AC-2 — hashCode delegates to unwrap),"
                + " got: a=" + a.hashCode() + " b=" + b.hashCode());
    }

    @Test
    @DisplayName("FR-CONV-004 AC-3: equals/hashCode 위임 패턴 reflective 검증 (servlet61)")
    void shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance_FRCONV004_AC3() {
        jakarta.servlet.http.Cookie origin = new jakarta.servlet.http.Cookie("k", "v");
        adapter.javax.servlet61.http.Cookie a = new adapter.javax.servlet61.http.Cookie(origin);

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

        assertTrue(a.equals(origin),
            "expected: a.equals(origin) true (this.unwrap().equals(origin) — FR-CONV-004 AC-3),"
                + " got: false (identity equals path — 위임 미구현)");

        assertEquals(origin.hashCode(), a.hashCode(),
            "expected: a.hashCode() == origin.hashCode() (SPEC-6 위임 패턴),"
                + " got: a=" + a.hashCode() + " origin=" + origin.hashCode());
    }
}
