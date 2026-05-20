package adapter.jakarta.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * TDD red-phase defect tests for FR-FIX-004
 * (adapter.jakarta.servlet5.ServletContext#setResponseCharacterEncoding(String)
 * mis-routes the call to {@code this.setRequestCharacterEncoding(arg0)},
 * causing a dual defect: (1) the response encoding stays at its default
 * value and (2) the request encoding is silently overwritten by the
 * caller's response-encoding intent).
 *
 * Each test exercises FR-FIX-004 under a distinct acceptance-criteria
 * scenario (AC-1 ~ AC-4). They MUST fail against the currently-shipped
 * (defective) jakarta-side code at
 * {@code servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java}
 * L337-338 and turn green only after FR-FIX-004 lands (task T-PH003-10).
 *
 * Defect surface (jakarta side, L337-338):
 * <pre>
 *   &#64;Override public void setResponseCharacterEncoding(String arg0) {
 *      this.setRequestCharacterEncoding(arg0);
 *   }
 * </pre>
 * The body invokes the adapter's OWN {@code setRequestCharacterEncoding}
 * (a self-call), which in turn forwards to
 * {@code origin.setRequestCharacterEncoding(arg0)} (L333-335). The net
 * effect is that the caller's response-encoding intent is dispatched to
 * the wrong origin method ({@code setRequestCharacterEncoding} instead of
 * {@code setResponseCharacterEncoding}), so the response Content-Type
 * charset is never updated and the request charset is corrupted.
 *
 * The javax-side counterpart
 * ({@code adapter.javax.servlet5.ServletContext#setResponseCharacterEncoding(String)}
 * L370-371) already delegates correctly to
 * {@code this.servletContext.setResponseCharacterEncoding(encoding)} and
 * is therefore the reference behaviour for direction B of AC-4. (The
 * symmetric {@code setRequestCharacterEncoding} defect on the javax side
 * at L362-363 is the subject of FR-FIX-005 / T-PH003-11~12 and is OUT OF
 * SCOPE for this test class.)
 *
 * Expected failure signature (all four tests):
 *   "expected: origin.setResponseCharacterEncoding called,
 *    got: origin.setRequestCharacterEncoding"
 *
 * Note on Mockito verify ordering: in every test the wanted positive
 * {@code verify(origin).setResponseCharacterEncoding(...)} is placed
 * BEFORE the negative {@code verify(origin, never()).setRequestCharacterEncoding(...)}.
 * Reason (T-PH003-05/07 learning): when the SUT delegates to the wrong
 * origin method, Mockito's {@code WantedButNotInvoked} on the positive
 * verify is the most direct pointer to the FR-FIX-004 defect (the
 * response-side method was wanted but never invoked). The negative
 * verify is a corroborating assertion — putting it first would surface a
 * {@code NeverWantedButInvoked} on the request-side method, which is the
 * downstream symptom rather than the root cause. All SUT methods exercised
 * here return {@code void}, so the verifications are the sole assertions
 * (no assertEquals races with them).
 *
 * Package placement: this test lives in {@code adapter.jakarta.servlet5}
 * so the public constructor of
 * {@link adapter.jakarta.servlet5.ServletContext} can be invoked directly
 * without reflection. The javax-side counterpart
 * {@code adapter.javax.servlet5.ServletContext} also exposes a public
 * constructor and is invoked via its fully-qualified name in AC-4's
 * direction B; reflection is therefore unnecessary in this class.
 *
 * Mock policy (§0.6 compliance): only the external servlet API interfaces
 * ({@link jakarta.servlet.ServletContext}, {@link javax.servlet.ServletContext})
 * are mocked. The SUTs (the adapter {@code ServletContext} classes) are
 * never mocked.
 */
class ServletContextEncodingDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 direction_correct
    // adapter.jakarta.servlet5.ServletContext#setResponseCharacterEncoding("UTF-8")
    // must delegate to origin.setResponseCharacterEncoding("UTF-8") verbatim.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-004 AC-1: jakarta ServletContext.setResponseCharacterEncoding(\"UTF-8\") must delegate to origin.setResponseCharacterEncoding(\"UTF-8\")")
    void shouldDelegateSetResponseCharEncodingToOriginSameMethod_AC1() {
        javax.servlet.ServletContext origin = mock(javax.servlet.ServletContext.class);
        ServletContext adapterCtx = new ServletContext(origin);

        adapterCtx.setResponseCharacterEncoding("UTF-8");

        // Red phase: defective code self-calls this.setRequestCharacterEncoding
        // which forwards to origin.setRequestCharacterEncoding, so
        // origin.setResponseCharacterEncoding is never invoked.
        // Expected red failure: Mockito WantedButNotInvoked, signature
        // "expected: origin.setResponseCharacterEncoding called,
        //  got: origin.setRequestCharacterEncoding".
        verify(origin).setResponseCharacterEncoding("UTF-8");
    }

    // ----------------------------------------------------------------------
    // AC-2 no_request_method_invoked
    // adapter.jakarta.servlet5.ServletContext#setResponseCharacterEncoding("UTF-8")
    // must NOT invoke origin.setRequestCharacterEncoding(...) — the
    // self-call mis-route of the defective code is explicitly forbidden.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-004 AC-2: jakarta ServletContext.setResponseCharacterEncoding must not invoke origin.setRequestCharacterEncoding (no mis-route)")
    void shouldDelegateSetResponseCharEncodingToOriginSameMethod_AC2() {
        javax.servlet.ServletContext origin = mock(javax.servlet.ServletContext.class);
        ServletContext adapterCtx = new ServletContext(origin);

        adapterCtx.setResponseCharacterEncoding("UTF-8");

        // Red phase: defective code invokes origin.setRequestCharacterEncoding("UTF-8")
        // via the internal self-call, so this never-verify fails with
        // Mockito NeverWantedButInvoked.
        // Expected red failure signature: "expected: origin.setResponseCharacterEncoding
        // called, got: origin.setRequestCharacterEncoding".
        verify(origin, never()).setRequestCharacterEncoding(anyString());
    }

    // ----------------------------------------------------------------------
    // AC-3 origin_response_method_called (BM03 regression equivalent)
    // SRS literal: "BM03 회귀 테스트 통과." Combines the two BM03 assertions
    // (positive verify on origin.setResponseCharacterEncoding + negative
    // verify on origin.setRequestCharacterEncoding) into a single,
    // self-contained regression scenario so this class does not depend on
    // BM03 itself.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-004 AC-3: BM03 regression — jakarta ServletContext.setResponseCharacterEncoding must call origin.setResponseCharacterEncoding (no mis-route to request)")
    void shouldDelegateSetResponseCharEncodingToOriginSameMethod_AC3() {
        javax.servlet.ServletContext origin = mock(javax.servlet.ServletContext.class);
        ServletContext adapterCtx = new ServletContext(origin);

        adapterCtx.setResponseCharacterEncoding("UTF-8");

        // verify the wanted (response-side) delegation first so the
        // red-phase failure surfaces as a Mockito WantedButNotInvoked on
        // origin.setResponseCharacterEncoding — the most direct pointer
        // to the FR-FIX-004 defect.
        // Expected red failure signature: "expected: origin.setResponseCharacterEncoding
        // called, got: origin.setRequestCharacterEncoding".
        verify(origin).setResponseCharacterEncoding("UTF-8");
        verify(origin, never()).setRequestCharacterEncoding(anyString());
    }

    // ----------------------------------------------------------------------
    // AC-4 cross_module_consistency (servlet5-local proxy for IT10)
    // SRS literal: "IT10 (Charset) 통합 테스트에서 응답 Content-Type 의 charset
    // 이 정확히 UTF-8 으로 전달된다." IT10 is owned by PH-005 integration
    // tests and cannot be invoked here. Servlet5-local proxy: assert that
    // BOTH directional adapters delegate setResponseCharacterEncoding ->
    // origin.setResponseCharacterEncoding consistently.
    //
    // Direction A (jakarta -> javax adapter): defective today, MUST fail red.
    // Direction B (javax -> jakarta adapter): already correct (L370-371),
    //   expected pass.
    // A single-direction failure fails the test as a whole — red is satisfied.
    //
    // Direction B uses the FQN adapter.javax.servlet5.ServletContext via
    // its public constructor; no reflection required.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-004 AC-4: both directional adapters must delegate setResponseCharacterEncoding -> origin consistently (cross-module proxy for IT10)")
    void shouldDelegateSetResponseCharEncodingToOriginSameMethod_AC4() {
        // direction A: jakarta adapter wrapping javax origin (defective today).
        javax.servlet.ServletContext javaxOrigin = mock(javax.servlet.ServletContext.class);
        ServletContext jakartaAdapter = new ServletContext(javaxOrigin);

        jakartaAdapter.setResponseCharacterEncoding("UTF-8");

        // verify-first: the wanted response-side delegation. In the red
        // phase the defective jakarta adapter calls
        // javaxOrigin.setRequestCharacterEncoding instead, so this fails
        // with Mockito WantedButNotInvoked.
        // Expected red failure: "expected: origin.setResponseCharacterEncoding
        // called, got: origin.setRequestCharacterEncoding".
        verify(javaxOrigin).setResponseCharacterEncoding("UTF-8");
        verify(javaxOrigin, never()).setRequestCharacterEncoding(anyString());

        // direction B: javax adapter wrapping jakarta origin (already correct).
        // Public constructor — instantiate via FQN, no reflection needed.
        jakarta.servlet.ServletContext jakartaOrigin = mock(jakarta.servlet.ServletContext.class);
        adapter.javax.servlet5.ServletContext javaxAdapter =
            new adapter.javax.servlet5.ServletContext(jakartaOrigin);

        javaxAdapter.setResponseCharacterEncoding("UTF-8");

        verify(jakartaOrigin).setResponseCharacterEncoding("UTF-8");
        verify(jakartaOrigin, never()).setRequestCharacterEncoding(anyString());
    }
}
