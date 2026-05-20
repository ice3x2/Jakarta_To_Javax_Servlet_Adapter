package adapter.jakarta.servlet5.http;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD red-phase defect tests for FR-FIX-002
 * (adapter.jakarta.servlet5.http.Part#getSubmittedFileName() invokes itself,
 * causing infinite recursion / StackOverflowError).
 *
 * Each test exercises FR-FIX-002 under a distinct acceptance-criteria scenario
 * (AC-1 ~ AC-4). They MUST fail against the currently-shipped (defective) code
 * at servlet5/.../adapter/jakarta/servlet5/http/Part.java L50-56 and turn
 * green only after FR-FIX-002 lands (task T-PH003-06).
 *
 * Defect surface (jakarta side, L50-56):
 * <pre>
 *   public String getSubmittedFileName() {
 *      try {
 *         return this.getSubmittedFileName();   // self-recursive -> SOE
 *      } catch (Exception e) {
 *         return this.getName();
 *      }
 *   }
 * </pre>
 * The {@code catch (Exception e)} clause does NOT catch
 * {@link StackOverflowError} (a {@link Throwable} / {@link Error}); therefore
 * the SOE propagates uncaught to the caller. The javax-side counterpart
 * (adapter.javax.servlet5.http.Part L25-26) already delegates correctly to
 * {@code this.part.getSubmittedFileName()} and is the reference behaviour.
 *
 * Expected failure signature (all four tests):
 *   "expected: origin.getSubmittedFileName() called, got: StackOverflowError"
 *
 * Note on Mockito verify ordering: in every test the {@code verify(...)} call
 * is placed BEFORE {@code assertEquals(...)}. Reason: when the SUT delegates
 * to the wrong method (or recurses on self), the Mockito verification surfaces
 * the failure as {@code WantedButNotInvoked} — a precise pointer to the actual
 * defect — rather than a misleading equality mismatch (Mockito returns
 * {@code null} by default for an unstubbed String call, so assertEquals would
 * fail with "expected upload.txt but got null", hiding the recursion symptom).
 * In the red phase the SOE actually short-circuits both calls, but the
 * ordering remains the documented convention so that once SOE is removed the
 * verify-first-then-assertEquals pattern keeps surfacing the correct failure
 * cause for any future regression.
 *
 * Package placement: this test lives in {@code adapter.jakarta.servlet5.http}
 * so that the package-default constructor of
 * {@link adapter.jakarta.servlet5.http.Part} can be invoked directly without
 * reflection. The javax-side counterpart
 * ({@code adapter.javax.servlet5.http.Part}) is in a different package and
 * therefore is instantiated via reflection in AC-4 only.
 *
 * Mock policy (§0.6 compliance): only the external servlet API interfaces
 * ({@link jakarta.servlet.http.Part}, {@link javax.servlet.http.Part}) are
 * mocked. The SUT (adapter Part classes) is never mocked.
 */
class PartDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 no_stack_overflow
    // jakarta adapter Part.getSubmittedFileName() must not raise
    // StackOverflowError (nor any other Throwable) due to self-recursion.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-002 AC-1: jakarta Part.getSubmittedFileName must not throw StackOverflowError (no self-recursion)")
    void shouldDelegateToOriginPartNotSelf_AC1() {
        javax.servlet.http.Part originPart = mock(javax.servlet.http.Part.class);
        when(originPart.getSubmittedFileName()).thenReturn("upload.txt");

        Part adapterPart = new Part(originPart);

        // Red phase: defective code recurses on `this.getSubmittedFileName()`,
        // exhausts the stack, and propagates StackOverflowError uncaught
        // (catch (Exception) does not catch java.lang.Error).
        // Expected red failure: assertDoesNotThrow fails with
        // "expected: origin.getSubmittedFileName() called, got: StackOverflowError".
        assertDoesNotThrow(adapterPart::getSubmittedFileName,
            "adapter must not throw StackOverflowError or other recursion-induced Throwable");
    }

    // ----------------------------------------------------------------------
    // AC-2 origin_delegation_verify
    // jakarta adapter Part.getSubmittedFileName() must invoke
    // origin.getSubmittedFileName() exactly once and return its value verbatim.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-002 AC-2: jakarta Part.getSubmittedFileName must delegate to origin.getSubmittedFileName and return its value verbatim")
    void shouldDelegateToOriginPartNotSelf_AC2() {
        javax.servlet.http.Part originPart = mock(javax.servlet.http.Part.class);
        when(originPart.getSubmittedFileName()).thenReturn("upload.txt");

        Part adapterPart = new Part(originPart);

        String fileName = adapterPart.getSubmittedFileName();

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.getSubmittedFileName wanted but never invoked
        // because the SUT recursed on itself and SOE'd).
        verify(originPart).getSubmittedFileName();
        assertEquals("upload.txt", fileName,
            "adapter must return origin.getSubmittedFileName() value verbatim");
    }

    // ----------------------------------------------------------------------
    // AC-3 BM02 regression equivalent
    // SRS literal: "BM02 regression test passes." Reproduces an equivalent
    // BM02 scenario inline (distinct fileName payload to disambiguate from
    // AC-2 and exercise an independent stub instance).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-002 AC-3: BM02 regression — distinct fileName payload must round-trip through origin.getSubmittedFileName")
    void shouldDelegateToOriginPartNotSelf_AC3() {
        javax.servlet.http.Part originPart = mock(javax.servlet.http.Part.class);
        when(originPart.getSubmittedFileName()).thenReturn("image-2024.png");

        Part adapterPart = new Part(originPart);

        String fileName = adapterPart.getSubmittedFileName();

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch rather than a misleading equality mismatch.
        verify(originPart).getSubmittedFileName();
        assertEquals("image-2024.png", fileName,
            "BM02 regression: adapter must round-trip the origin file name unchanged");
    }

    // ----------------------------------------------------------------------
    // AC-4 cross_module_consistency (servlet5-local proxy for IT08)
    // SRS literal: "IT08 multipart upload integration test passes." IT08 is
    // owned by PH-005 integration tests and cannot be invoked here.
    // Servlet5-local proxy (T-PH003-03 StreamConverterDefectTest AC-4
    // pattern): assert that BOTH directional adapters delegate
    // getSubmittedFileName -> origin.getSubmittedFileName consistently.
    //
    // Direction A (jakarta -> javax adapter): defective today, MUST fail red.
    // Direction B (javax -> jakarta adapter): already correct, expected pass.
    // A single-direction failure fails the test as a whole — red is satisfied.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-002 AC-4: both directional adapters must delegate getSubmittedFileName -> origin consistently (cross-module proxy for IT08)")
    void shouldDelegateToOriginPartNotSelf_AC4() throws Exception {
        // direction A: jakarta adapter wrapping javax origin (defective today)
        javax.servlet.http.Part javaxOrigin = mock(javax.servlet.http.Part.class);
        when(javaxOrigin.getSubmittedFileName()).thenReturn("dirA-upload.txt");

        Part jakartaAdapter = new Part(javaxOrigin);
        String returnedA = jakartaAdapter.getSubmittedFileName();

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.getSubmittedFileName wanted but never invoked).
        verify(javaxOrigin).getSubmittedFileName();
        assertEquals("dirA-upload.txt", returnedA,
            "jakarta->javax adapter must propagate origin.getSubmittedFileName verbatim");

        // direction B: javax adapter wrapping jakarta origin (already correct).
        // Constructor is package-default and lives in a different package
        // (adapter.javax.servlet5.http), so reflection is required.
        jakarta.servlet.http.Part jakartaOrigin = mock(jakarta.servlet.http.Part.class);
        when(jakartaOrigin.getSubmittedFileName()).thenReturn("dirB-upload.txt");

        Class<?> javaxAdapterClass = Class.forName("adapter.javax.servlet5.http.Part");
        Constructor<?> ctor = javaxAdapterClass.getDeclaredConstructor(jakarta.servlet.http.Part.class);
        ctor.setAccessible(true);
        javax.servlet.http.Part javaxAdapter =
            (javax.servlet.http.Part) ctor.newInstance(jakartaOrigin);

        String returnedB = javaxAdapter.getSubmittedFileName();

        verify(jakartaOrigin).getSubmittedFileName();
        assertEquals("dirB-upload.txt", returnedB,
            "javax->jakarta adapter must propagate origin.getSubmittedFileName verbatim");
    }
}
