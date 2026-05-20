package adapter.jakarta.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * TDD red-phase defect tests for FR-FIX-003
 * (adapter.jakarta.servlet5.ServletResponse#setContentLengthLong(long)
 * narrow-casts its {@code long} argument to {@code int} when forwarding
 * to the wrapped origin response, causing silent overflow for payloads
 * larger than {@link Integer#MAX_VALUE} and producing an incorrect
 * {@code Content-Length} header).
 *
 * Each test exercises FR-FIX-003 under a distinct acceptance-criteria
 * scenario (AC-1 ~ AC-3). They MUST fail against the currently-shipped
 * (defective) jakarta-side code at
 * {@code servlet5/src/main/java/adapter/jakarta/servlet5/ServletResponse.java}
 * L81-83 and turn green only after FR-FIX-003 lands (task T-PH003-08).
 *
 * Defect surface (jakarta side, L81-83):
 * <pre>
 *   &#64;Override public void setContentLengthLong(long arg0) {
 *      this.response.setContentLength((int)arg0);
 *   }
 * </pre>
 * The cast {@code (int)arg0} silently truncates the upper 32 bits of any
 * value greater than {@link Integer#MAX_VALUE}. For {@link Long#MAX_VALUE}
 * the resulting {@code int} is {@code -1}, and the call is dispatched to
 * the wrong origin method ({@code setContentLength(int)} instead of
 * {@code setContentLengthLong(long)}).
 *
 * The javax-side counterpart
 * ({@code adapter.javax.servlet5.ServletResponse#setContentLengthLong(long)}
 * L45-48) already delegates correctly to
 * {@code this.response.setContentLengthLong(len)} and is therefore NOT a
 * verification target of this test class (sidecar.files scopes this task
 * to the jakarta-side test file only).
 *
 * Expected failure signature (all three tests):
 *   "expected: setContentLengthLong(Long.MAX_VALUE) delegated as long,
 *    got: int cast value"
 *
 * Note on Mockito verify ordering: in every test the {@code verify(...)}
 * call is the primary assertion (the SUT method returns {@code void}, so
 * there is no equality check to race against). Placing {@code verify}
 * first follows the convention established in
 * {@link adapter.jakarta.servlet5.http.PartDefectTest} (T-PH003-05 round
 * 2 learning, see also T-PH003-03): Mockito's {@code WantedButNotInvoked}
 * surfaces the precise defect (origin's long-arity method was never
 * called) rather than a misleading downstream symptom. In AC-3 the
 * positive {@code verify} on {@code setContentLengthLong} precedes the
 * negative {@code verify} on {@code setContentLength(anyInt())} for the
 * same reason: the wanted-but-not-invoked failure is the directest
 * pointer to the FR-FIX-003 defect.
 *
 * Package placement: this test lives in {@code adapter.jakarta.servlet5}
 * so that the public constructor of
 * {@link adapter.jakarta.servlet5.ServletResponse} can be invoked
 * directly (matching the BM05 regression test pattern in
 * {@code com.snoworca.regression.BugMuseumTest}).
 *
 * Mock policy (§0.6 compliance): only the external servlet API interface
 * ({@link javax.servlet.ServletResponse}) is mocked. The SUT
 * ({@link adapter.jakarta.servlet5.ServletResponse}) is never mocked.
 */
class ServletResponseDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 long_precision_preserved
    // adapter.jakarta.servlet5.ServletResponse#setContentLengthLong(Long.MAX_VALUE)
    // must delegate to origin.setContentLengthLong(Long.MAX_VALUE) verbatim
    // (no narrowing, no precision loss).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-003 AC-1: jakarta ServletResponse.setContentLengthLong(Long.MAX_VALUE) must delegate to origin.setContentLengthLong with the exact long value")
    void shouldPreserveLongPrecisionForContentLength_AC1() {
        javax.servlet.ServletResponse origin = mock(javax.servlet.ServletResponse.class);
        ServletResponse adapterResp = new ServletResponse(origin);

        adapterResp.setContentLengthLong(Long.MAX_VALUE);

        // Red phase: defective code calls origin.setContentLength((int)Long.MAX_VALUE)
        // and never invokes origin.setContentLengthLong, so this verify fails
        // with Mockito WantedButNotInvoked.
        // Expected red failure: "expected: setContentLengthLong(Long.MAX_VALUE)
        // delegated as long, got: int cast value".
        verify(origin).setContentLengthLong(Long.MAX_VALUE);
    }

    // ----------------------------------------------------------------------
    // AC-2 no_int_cast
    // adapter.jakarta.servlet5.ServletResponse#setContentLengthLong(Long.MAX_VALUE)
    // must NOT invoke origin.setContentLength(int) — the narrow-cast path
    // of the defective code is explicitly forbidden.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-003 AC-2: jakarta ServletResponse.setContentLengthLong must not invoke origin.setContentLength(int) (no narrow cast)")
    void shouldPreserveLongPrecisionForContentLength_AC2() {
        javax.servlet.ServletResponse origin = mock(javax.servlet.ServletResponse.class);
        ServletResponse adapterResp = new ServletResponse(origin);

        adapterResp.setContentLengthLong(Long.MAX_VALUE);

        // Red phase: defective code invokes origin.setContentLength((int)Long.MAX_VALUE)
        // = origin.setContentLength(-1), so this never-verify fails.
        // Expected red failure: "expected: setContentLengthLong(Long.MAX_VALUE)
        // delegated as long, got: int cast value".
        verify(origin, never()).setContentLength(anyInt());
    }

    // ----------------------------------------------------------------------
    // AC-3 BM05 regression / origin_method_invoked
    // SRS literal: "BM05 회귀 테스트 통과." Combines the two BM05 assertions
    // (positive verify on origin.setContentLengthLong + negative verify on
    // origin.setContentLength(anyInt)) into a single test method so this
    // class is a self-contained regression equivalent of BM05.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-003 AC-3: BM05 regression — jakarta ServletResponse.setContentLengthLong must call origin.setContentLengthLong (no int narrowing)")
    void shouldPreserveLongPrecisionForContentLength_AC3() {
        javax.servlet.ServletResponse origin = mock(javax.servlet.ServletResponse.class);
        ServletResponse adapterResp = new ServletResponse(origin);

        adapterResp.setContentLengthLong(Long.MAX_VALUE);

        // verify the wanted (long-arity) delegation first so the red-phase
        // failure surfaces as a Mockito WantedButNotInvoked on the
        // origin.setContentLengthLong method — the most direct pointer to
        // the FR-FIX-003 defect.
        // Expected red failure: "expected: setContentLengthLong(Long.MAX_VALUE)
        // delegated as long, got: int cast value".
        verify(origin).setContentLengthLong(Long.MAX_VALUE);
        verify(origin, never()).setContentLength(anyInt());
    }
}
