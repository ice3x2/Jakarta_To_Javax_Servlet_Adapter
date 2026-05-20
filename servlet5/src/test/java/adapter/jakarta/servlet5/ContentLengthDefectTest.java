package adapter.jakarta.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD red-phase defect tests for FR-FIX-006
 * (adapter.jakarta.servlet5.ServletRequest#getContentLengthLong() wraps the
 * origin call in {@code try/catch (Exception)} and falls back to
 * {@code this.request.getContentLength()} (int return, narrow-cast).
 *
 * Defect surface (jakarta side, servlet5 L173-179):
 * <pre>
 *   &#64;Override public long getContentLengthLong() {
 *      try {
 *         return this.request.getContentLengthLong();
 *      } catch (Exception e) {
 *         return this.request.getContentLength();
 *      }
 *   }
 * </pre>
 * Two simultaneous problems:
 *   (1) the catch clause is dead-code defensive programming — javax 4.0 /
 *       jakarta 5.0/6.0/6.1 all have a working {@code getContentLengthLong},
 *       so the swallow path is unreachable in valid containers and only
 *       masks genuine origin-side exceptions during testing or future
 *       container versions;
 *   (2) the fallback returns {@code getContentLength()} (int) which truncates
 *       payloads &gt; 2 GB to the wrong value once it gets implicitly widened
 *       back to {@code long}.
 *
 * The javax-side counterpart
 * ({@code adapter.javax.servlet5.ServletRequest#getContentLengthLong} L49-51)
 * already delegates correctly via a single statement and is NOT a target of
 * this test class.
 *
 * Note on AC ↔ test scenario mapping: SRS FR-FIX-006 AC-1 is phrased as
 * "원본의 동명 메서드만 호출된다 (try/catch 없음)" and AC-2 as "원본이
 * Long.MAX_VALUE 반환하면 어댑터도 그대로 반환한다." A pure happy-path
 * scenario ({@code mock returns Long.MAX_VALUE}) cannot detect the defect
 * because the {@code try} branch succeeds and produces the same observable
 * output as the fixed code. Therefore both AC-1 and AC-2 are exercised via
 * the throw scenario ({@code mock throws RuntimeException}) which forces
 * the catch branch to execute and the fallback path to be observed.
 * The sidecar.test_symbol names are preserved verbatim; the in-body
 * semantics deviate from the literal AC text and this deviation is
 * recorded as an L-OPEN item.
 *
 * Expected failure signature (both tests):
 *   "expected: Long.MAX_VALUE returned, got: int-cast value or fallback"
 *
 * Note on Mockito verify ordering: AC-1 uses {@code assertThrows} as the
 * primary assertion (the SUT method's exception propagation IS the
 * observable behavior). AC-2 uses {@code verify(origin, never())} as the
 * primary assertion. Either way the red-phase failure is the most direct
 * pointer to the FR-FIX-006 defect (catch swallow + fallback path).
 *
 * Mock policy (§0.6 compliance): only the external servlet API interface
 * ({@link javax.servlet.ServletRequest}) is mocked. The SUT
 * ({@link adapter.jakarta.servlet5.ServletRequest}) is never mocked.
 */
class ContentLengthDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 long_precision_preserved (via throw scenario)
    // adapter.jakarta.servlet5.ServletRequest#getContentLengthLong() must
    // propagate any RuntimeException from origin.getContentLengthLong()
    // verbatim — the catch swallow is forbidden.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-006 AC-1: jakarta ServletRequest.getContentLengthLong must propagate origin exceptions (no catch swallow)")
    void shouldReturnLongMaxValueFromOriginGetContentLengthLong_AC1() {
        javax.servlet.ServletRequest origin = mock(javax.servlet.ServletRequest.class);
        RuntimeException simulated = new RuntimeException("origin failure");
        when(origin.getContentLengthLong()).thenThrow(simulated);

        ServletRequest adapterReq = new ServletRequest(origin);

        // Red phase: defective code catches the RuntimeException and silently
        // falls back to origin.getContentLength() (int). assertThrows fails
        // because nothing is thrown.
        // Expected red failure: "expected: Long.MAX_VALUE returned,
        // got: int-cast value or fallback".
        assertThrows(RuntimeException.class, adapterReq::getContentLengthLong,
            "adapter must propagate origin.getContentLengthLong exceptions, not swallow them");
    }

    // ----------------------------------------------------------------------
    // AC-2 no_int_fallback (via throw scenario)
    // adapter.jakarta.servlet5.ServletRequest#getContentLengthLong() must
    // NOT invoke origin.getContentLength() under any circumstance — the
    // narrow-cast fallback path is explicitly forbidden.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-006 AC-2: jakarta ServletRequest.getContentLengthLong must not invoke origin.getContentLength() (no narrow-cast fallback)")
    void shouldReturnLongMaxValueFromOriginGetContentLengthLong_AC2() {
        javax.servlet.ServletRequest origin = mock(javax.servlet.ServletRequest.class);
        RuntimeException simulated = new RuntimeException("origin failure");
        when(origin.getContentLengthLong()).thenThrow(simulated);

        ServletRequest adapterReq = new ServletRequest(origin);

        // Drive the SUT to invoke origin (swallow happens inside the SUT).
        try {
            adapterReq.getContentLengthLong();
        } catch (RuntimeException expected) {
            // After FR-FIX-006 lands the exception propagates here.
            // Red phase: defective code catches internally and never re-throws,
            // so no exception arrives here and the assertions below still fire.
        }

        // Red phase: defective code invokes origin.getContentLength() in the
        // fallback branch, so this never-verify fails with Mockito
        // NeverWantedButInvoked.
        // Expected red failure: "expected: Long.MAX_VALUE returned,
        // got: int-cast value or fallback".
        verify(origin, never()).getContentLength();
        verify(origin).getContentLengthLong();
    }
}
