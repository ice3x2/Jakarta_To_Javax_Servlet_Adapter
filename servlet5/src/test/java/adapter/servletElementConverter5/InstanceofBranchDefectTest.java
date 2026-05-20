package adapter.servletElementConverter5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * TDD red-phase defect tests for FR-FIX-008
 * ({@code adapter.servletElementConverter5.ServletReqResConverter} has four
 * convert overloads, two of which use the wrong {@code instanceof} namespace
 * — the jakarta-input overloads check {@code instanceof javax.*.HttpServlet*}
 * which is always false because a jakarta object can never be a javax
 * HTTP type. Result: jakarta HTTP requests / responses fall through to the
 * generic {@code ServletRequest} / {@code ServletResponse} wrapper, and
 * HTTP-specific methods (sendRedirect, addCookie, setStatus, ...) are
 * unavailable through the resulting adapter.
 *
 * Defect surface (servlet5 L13-27 — same pattern in servlet6 L13-27 and
 * servlet61 L13-27):
 * <pre>
 *   // L14: javax input → instanceof HttpServletResponse  (javax import L6)  ← OK
 *   // L18: javax input → instanceof HttpServletRequest   (javax import L5)  ← OK
 *   // L22: jakarta input → instanceof HttpServletResponse (javax import L6) ← DEFECT
 *   // L26: jakarta input → instanceof HttpServletRequest  (javax import L5) ← DEFECT
 * </pre>
 *
 * Per FR-FIX-008 the fix is to change L22 / L26 to use the
 * {@code jakarta.servlet.http.*} types so the instanceof check matches the
 * input namespace and the HTTP branch is actually reachable.
 *
 * Test strategy — source-grep + namespace-consistency check:
 *   All four AC are unified into source-file inspection of the SUT. The
 *   {@code @Override} retention is irrelevant here, but the {@code instanceof}
 *   syntactic form is preserved in the .class file's debug info only — not
 *   reachable via runtime reflection. Therefore the test reads the SUT's
 *   source file directly (gradle test cwd = module root = {@code servlet5/})
 *   and asserts that each of the four branch lines uses the namespace that
 *   matches its overload's input parameter type.
 *
 *   This unification is deliberate (sidecar.tdd.test_cases[].test_symbol
 *   names AC-1..AC-4 individually, but all four reduce to the same source
 *   invariant — the namespace-consistency rule applied to four lines). The
 *   in-body semantics deviate from a strict per-AC isolation, and this
 *   deviation is recorded as an L-OPEN item.
 *
 * Expected failure signature (all four tests):
 *   "expected: instanceof branches match input arg namespace,
 *    got: cross-namespace always-false"
 *
 * Mock policy (§0.6 compliance): no mocks needed for source-grep
 * assertions. {@code mock(jakarta.servlet.http.HttpServletResponse.class)}
 * is referenced for documentation of the runtime symptom but not invoked
 * in assertions.
 */
class InstanceofBranchDefectTest {

    private static final Path SUT_SOURCE = Paths.get(
        "src/main/java/adapter/servletElementConverter5/ServletReqResConverter.java");

    private static String readSut() throws IOException {
        return Files.readString(SUT_SOURCE);
    }

    // ----------------------------------------------------------------------
    // AC-1 jakarta_input_jakarta_check (L22 — convert(jakarta.ServletResponse))
    // The jakarta.ServletResponse overload must use
    // `instanceof jakarta.servlet.http.HttpServletResponse` so HTTP responses
    // route to the javax HTTP adapter, not the generic ServletResponse wrapper.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-008 AC-1: jakarta ServletResponse overload must use jakarta-namespace instanceof (not javax)")
    void shouldMatchInputNamespaceForAllFourInstanceofBranches_AC1() throws IOException {
        String src = readSut();
        // Red phase: defective L22 reads
        //   `return response instanceof HttpServletResponse ? new adapter.javax.servlet5.http....`
        // where HttpServletResponse resolves to javax.servlet.http.HttpServletResponse
        // (via import). The jakarta-namespace form is absent → assertTrue fails.
        // Expected red failure: "expected: instanceof branches match input arg
        // namespace, got: cross-namespace always-false".
        assertTrue(src.contains("response instanceof jakarta.servlet.http.HttpServletResponse"),
            "convert(jakarta.ServletResponse) must check instanceof jakarta.servlet.http.HttpServletResponse");
    }

    // ----------------------------------------------------------------------
    // AC-2 javax_input_javax_check (L14 — convert(javax.ServletResponse))
    // The javax.ServletResponse overload must use
    // `instanceof javax.servlet.http.HttpServletResponse`. (This branch is
    // currently correct, but the test enforces the invariant explicitly so
    // a future regression that swaps the namespaces is detected.)
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-008 AC-2: javax ServletResponse overload must use javax-namespace instanceof (regression guard)")
    void shouldMatchInputNamespaceForAllFourInstanceofBranches_AC2() throws IOException {
        String src = readSut();
        // The fix specification (SRS Implementation Notes) is silent on whether
        // L14 should be written with an explicit `javax.servlet.http.` prefix or
        // rely on import + unprefixed HttpServletResponse. The test enforces an
        // unambiguous form — the jakarta-namespace form must NOT appear on a
        // line that handles a javax input. Red-phase: the defective code uses
        // unprefixed `HttpServletResponse` on the javax-input line, which is
        // ambiguous from a pure-string standpoint; we therefore look for the
        // explicit positive marker on the jakarta-input line (AC-1) and the
        // explicit positive marker on the javax-input line here. After fix,
        // L14 must contain the explicit javax prefix to match AC-1's explicit
        // jakarta prefix (symmetry rule).
        assertTrue(src.contains("response instanceof javax.servlet.http.HttpServletResponse"),
            "convert(javax.ServletResponse) must use explicit javax.servlet.http.HttpServletResponse instanceof for symmetry with jakarta-input branch");
    }

    // ----------------------------------------------------------------------
    // AC-3 no_cross_namespace (L26 — convert(jakarta.ServletRequest))
    // The jakarta.ServletRequest overload must use
    // `instanceof jakarta.servlet.http.HttpServletRequest`.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-008 AC-3: jakarta ServletRequest overload must use jakarta-namespace instanceof (BM04 regression)")
    void shouldMatchInputNamespaceForAllFourInstanceofBranches_AC3() throws IOException {
        String src = readSut();
        // Red phase: defective L26 reads
        //   `return request instanceof HttpServletRequest ? new adapter.javax.servlet5.http....`
        // where HttpServletRequest resolves to javax.servlet.http.HttpServletRequest
        // (via import L5). The jakarta-namespace form is absent → assertTrue fails.
        // Expected red failure: "expected: instanceof branches match input arg
        // namespace, got: cross-namespace always-false".
        assertTrue(src.contains("request instanceof jakarta.servlet.http.HttpServletRequest"),
            "convert(jakarta.ServletRequest) must check instanceof jakarta.servlet.http.HttpServletRequest");
    }

    // ----------------------------------------------------------------------
    // AC-4 cross_module_consistency (L18 — convert(javax.ServletRequest))
    // Mirror of AC-2 for the request overload. After fix the L18 line must
    // use the explicit javax-namespace prefix matching the jakarta-input
    // counterpart (AC-3). Reference runtime symptom: jakarta HTTP request
    // mock fed through the converter must produce a real HTTP adapter.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-008 AC-4: javax ServletRequest overload must use javax-namespace instanceof (cross-module consistency)")
    void shouldMatchInputNamespaceForAllFourInstanceofBranches_AC4() throws IOException {
        // Documentation-only: the runtime symptom of L26 (AC-3) is that this
        // mock falls through to the non-HTTP wrapper. We don't assert on the
        // runtime here (covered by BM04 + IT01) — pure source-grep instead.
        @SuppressWarnings("unused")
        jakarta.servlet.http.HttpServletResponse runtimeSymptomReference =
            mock(jakarta.servlet.http.HttpServletResponse.class);

        String src = readSut();
        assertTrue(src.contains("request instanceof javax.servlet.http.HttpServletRequest"),
            "convert(javax.ServletRequest) must use explicit javax.servlet.http.HttpServletRequest instanceof for symmetry with jakarta-input branch (cross-module consistency)");
    }
}
