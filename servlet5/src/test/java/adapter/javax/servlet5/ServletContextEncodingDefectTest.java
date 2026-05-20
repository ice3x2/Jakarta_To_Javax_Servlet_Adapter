package adapter.javax.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * TDD red-phase defect tests for FR-FIX-005
 * (adapter.javax.servlet5.ServletContext#setRequestCharacterEncoding(String)
 * invokes this.setResponseCharacterEncoding(encoding) — i.e. it routes the
 * request-encoding call to the response-encoding adapter method, which in
 * turn correctly delegates to origin.setResponseCharacterEncoding. The net
 * effect: origin.setRequestCharacterEncoding is NEVER called, and the
 * request encoding is silently dropped while the response encoding is
 * overwritten).
 *
 * This is the mirror defect of FR-FIX-004 (jakarta-side
 * setResponseCharacterEncoding routing to setRequestCharacterEncoding).
 * Together the two defects form a symmetric mis-routing in opposite
 * directions across the bidirectional adapter pair.
 *
 * Defect surface (javax side, servlet5 L353-355):
 * <pre>
 *   &#64;Override public void setRequestCharacterEncoding(String encoding) {
 *      this.setResponseCharacterEncoding(encoding);   // wrong: routes to response side
 *   }
 * </pre>
 * The companion `setResponseCharacterEncoding` (L361-363) is correct — it
 * delegates to `this.servletContext.setResponseCharacterEncoding(encoding)`.
 *
 * Expected failure signature (all three tests):
 *   "expected: origin.setRequestCharacterEncoding called,
 *    got: origin.setResponseCharacterEncoding"
 *
 * Note on Mockito verify ordering: in every test the {@code verify(...)}
 * call is the primary assertion (the SUT method returns {@code void}, so
 * there is no equality check to race against). Placing the positive
 * verify (`origin.setRequestCharacterEncoding`) first surfaces the
 * red-phase failure as Mockito's {@code WantedButNotInvoked} — the most
 * direct pointer to the FR-FIX-005 defect. In AC-3 the positive verify
 * precedes the negative verify on `origin.setResponseCharacterEncoding`
 * for the same reason: the wanted-but-not-invoked failure is more
 * informative than the never-wanted-but-invoked variant for diagnosing
 * the mis-routing.
 *
 * Package placement: this test lives in {@code adapter.javax.servlet5}
 * so that the public constructor of
 * {@link adapter.javax.servlet5.ServletContext} can be invoked directly.
 *
 * Mock policy (§0.6 compliance): only the external servlet API interface
 * ({@link jakarta.servlet.ServletContext}) is mocked. The SUT
 * ({@link adapter.javax.servlet5.ServletContext}) is never mocked.
 */
class ServletContextEncodingDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 direction_correct
    // adapter.javax.servlet5.ServletContext#setRequestCharacterEncoding("UTF-8")
    // must delegate to origin.setRequestCharacterEncoding("UTF-8") verbatim
    // (no mis-routing to the response side).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-005 AC-1: javax ServletContext.setRequestCharacterEncoding(\"UTF-8\") must delegate to origin.setRequestCharacterEncoding(\"UTF-8\")")
    void shouldDelegateSetRequestCharEncodingToOriginSameMethod_AC1() {
        jakarta.servlet.ServletContext origin = mock(jakarta.servlet.ServletContext.class);
        ServletContext adapterCtx = new ServletContext(origin);

        adapterCtx.setRequestCharacterEncoding("UTF-8");

        // Red phase: defective code routes to this.setResponseCharacterEncoding,
        // which ends up calling origin.setResponseCharacterEncoding instead.
        // origin.setRequestCharacterEncoding is never invoked, so this verify
        // fails with Mockito WantedButNotInvoked.
        // Expected red failure: "expected: origin.setRequestCharacterEncoding
        // called, got: origin.setResponseCharacterEncoding".
        verify(origin).setRequestCharacterEncoding("UTF-8");
    }

    // ----------------------------------------------------------------------
    // AC-2 no_response_method_invoked
    // adapter.javax.servlet5.ServletContext#setRequestCharacterEncoding must
    // NOT invoke origin.setResponseCharacterEncoding (the mis-routed path of
    // the defective code is explicitly forbidden).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-005 AC-2: javax ServletContext.setRequestCharacterEncoding must not invoke origin.setResponseCharacterEncoding (no mis-route)")
    void shouldDelegateSetRequestCharEncodingToOriginSameMethod_AC2() {
        jakarta.servlet.ServletContext origin = mock(jakarta.servlet.ServletContext.class);
        ServletContext adapterCtx = new ServletContext(origin);

        adapterCtx.setRequestCharacterEncoding("UTF-8");

        // Red phase: defective code transitively invokes
        // origin.setResponseCharacterEncoding via the self-delegation chain,
        // so this never-verify fails with Mockito NeverWantedButInvoked.
        // Expected red failure: "expected: origin.setRequestCharacterEncoding
        // called, got: origin.setResponseCharacterEncoding".
        verify(origin, never()).setResponseCharacterEncoding(anyString());
    }

    // ----------------------------------------------------------------------
    // AC-3 BM08 regression / origin_request_method_called
    // SRS literal: "BM08 회귀 테스트 통과." Combines the two BM08 assertions
    // (positive verify on origin.setRequestCharacterEncoding + negative verify
    // on origin.setResponseCharacterEncoding) into a single test method so
    // this class is a self-contained regression equivalent of BM08.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-005 AC-3: BM08 regression — javax ServletContext.setRequestCharacterEncoding must call origin.setRequestCharacterEncoding (no mis-route to response)")
    void shouldDelegateSetRequestCharEncodingToOriginSameMethod_AC3() {
        jakarta.servlet.ServletContext origin = mock(jakarta.servlet.ServletContext.class);
        ServletContext adapterCtx = new ServletContext(origin);

        adapterCtx.setRequestCharacterEncoding("UTF-8");

        // verify the wanted (request-side) delegation first so the red-phase
        // failure surfaces as Mockito WantedButNotInvoked on the
        // origin.setRequestCharacterEncoding method — the most direct
        // pointer to the FR-FIX-005 defect.
        // Expected red failure: "expected: origin.setRequestCharacterEncoding
        // called, got: origin.setResponseCharacterEncoding".
        verify(origin).setRequestCharacterEncoding("UTF-8");
        verify(origin, never()).setResponseCharacterEncoding(anyString());
    }
}
