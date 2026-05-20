package adapter.jakarta.servlet5.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TDD red-phase defect tests for FR-FIX-009
 * ({@code adapter.jakarta.servlet5.http.HttpServletRequest#getCookies()} (and
 * its javax-side mirror) iterates the origin cookie array directly without
 * a null guard. When the origin returns {@code null}, the for-each loop
 * triggers {@code NullPointerException} (and the subsequent
 * {@code cookies.length} on the javax side fails the same way).
 *
 * Defect surface (jakarta side, servlet5 L166-175):
 * <pre>
 *   &#64;Override public jakarta.servlet.http.Cookie[] getCookies() {
 *      &#64;SuppressWarnings("DuplicatedCode")
 *      javax.servlet.http.Cookie[] cookies = this.httpRequest.getCookies();
 *      ArrayList&lt;jakarta.servlet.http.Cookie&gt; newArrayList = new ArrayList&lt;&gt;();
 *      for (javax.servlet.http.Cookie cookie : cookies) {          // ← NPE here
 *         newArrayList.add(new Cookie(cookie));
 *      }
 *      return newArrayList.toArray(new jakarta.servlet.http.Cookie[0]);
 *   }
 * </pre>
 *
 * Per FR-FIX-009 the fix is to add an early-return {@code if (cookies == null)
 * return null;} (or equivalent empty-array path). BM07 accepts either null or
 * an empty array as the response — the defect is throwing NPE.
 *
 * The javax-side counterpart
 * ({@code adapter.javax.servlet5.http.HttpServletRequest#getCookies}) has the
 * mirror defect (L182-191). Both directions must be fixed; BM07 covers the
 * jakarta direction, and this test class adds a cross-module proxy (AC-2)
 * that exercises the javax direction symmetrically.
 *
 * Expected failure signature (both tests):
 *   "expected: null returned when origin getCookies is null,
 *    got: NullPointerException"
 *
 * Note on Mockito verify ordering: AC-1 uses {@code assertDoesNotThrow} as
 * the primary assertion (the defect IS the unchecked NPE). AC-2 uses the
 * same pattern on the mirror direction.
 *
 * Mock policy (§0.6 compliance): only the external servlet API interface
 * ({@link javax.servlet.http.HttpServletRequest}, {@link jakarta.servlet.http.HttpServletRequest})
 * is mocked. The SUT adapter classes are never mocked.
 */
class GetCookiesNullDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 null_passthrough (jakarta direction)
    // adapter.jakarta.servlet5.http.HttpServletRequest#getCookies() must not
    // throw NullPointerException when the origin returns null. Either null or
    // an empty array is acceptable as the returned value.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-009 AC-1: jakarta HttpServletRequest.getCookies must not throw NPE when origin returns null (BM07 regression)")
    void shouldReturnNullWhenOriginCookiesIsNull_AC1() {
        javax.servlet.http.HttpServletRequest origin = mock(javax.servlet.http.HttpServletRequest.class);
        when(origin.getCookies()).thenReturn(null);

        HttpServletRequest adapterReq = new HttpServletRequest(origin);

        // Red phase: defective code enters the for-each on a null array,
        // throws NullPointerException, and assertDoesNotThrow fails.
        // Expected red failure: "expected: null returned when origin
        // getCookies is null, got: NullPointerException".
        jakarta.servlet.http.Cookie[] cookies = assertDoesNotThrow(adapterReq::getCookies,
            "adapter.getCookies must tolerate origin returning null (no NPE)");

        // null or empty array — both acceptable per FR-FIX-009 / BM07.
        assertTrue(cookies == null || cookies.length == 0,
            "result must be null or empty array, got: length=" + (cookies == null ? "null" : cookies.length));
    }

    // ----------------------------------------------------------------------
    // AC-2 empty_array_preserved + cross_module_consistency (javax direction)
    // Cross-module proxy: adapter.javax.servlet5.http.HttpServletRequest also
    // has the mirror defect at L182-191. This AC exercises the javax-side
    // adapter via reflection (different package).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-009 AC-2: javax HttpServletRequest.getCookies must not throw NPE when origin returns null (cross-module mirror)")
    void shouldReturnNullWhenOriginCookiesIsNull_AC2() throws Exception {
        jakarta.servlet.http.HttpServletRequest origin = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(origin.getCookies()).thenReturn(null);

        Class<?> javaxAdapterClass = Class.forName("adapter.javax.servlet5.http.HttpServletRequest");
        java.lang.reflect.Constructor<?> ctor =
            javaxAdapterClass.getDeclaredConstructor(jakarta.servlet.http.HttpServletRequest.class);
        ctor.setAccessible(true);
        Object adapterReq = ctor.newInstance(origin);

        java.lang.reflect.Method getCookies = javaxAdapterClass.getMethod("getCookies");

        // Red phase: defective code throws NPE inside reflective invocation
        // (wrapped as InvocationTargetException); assertDoesNotThrow fails.
        // Expected red failure: "expected: null returned when origin
        // getCookies is null, got: NullPointerException".
        javax.servlet.http.Cookie[] cookies = assertDoesNotThrow(() -> {
            try {
                return (javax.servlet.http.Cookie[]) getCookies.invoke(adapterReq);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                // Unwrap so assertDoesNotThrow sees the original NPE, not the
                // reflection wrapper.
                if (ite.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) ite.getCause();
                }
                throw new RuntimeException(ite.getCause());
            }
        }, "javax HttpServletRequest.getCookies must tolerate origin returning null (mirror of jakarta direction)");

        assertTrue(cookies == null || cookies.length == 0,
            "javax-side result must be null or empty array, got: length=" + (cookies == null ? "null" : cookies.length));
    }
}
