package adapter.jakarta.servlet5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD red-phase defect tests for FR-FIX-007
 * (adapter.jakarta.servlet5.ServletRequest carries an
 * {@code @Override public String getRealPath(String path)} declaration even
 * though the {@code jakarta.servlet.ServletRequest} interface does not
 * declare that method in jakarta 5.0+. The defect: a stray {@code @Override}
 * annotation lies about the contract — readers infer the method is part of
 * the public adapter surface when in fact it cannot be reached through the
 * {@code jakarta.servlet.ServletRequest} cast).
 *
 * Defect surface (jakarta side, servlet5 L124-127):
 * <pre>
 *   &#64;Override public String getRealPath(String path) {
 *       //noinspection deprecation
 *       return this.request.getRealPath(path);
 *   }
 * </pre>
 *
 * Per FR-FIX-007 the fix is to **drop the {@code @Override} annotation**
 * (the method itself may remain as a deprecated alias that delegates to
 * the javax-side deprecated method, or it may be removed entirely — both
 * are acceptable per SRS Implementation Notes).
 *
 * The sibling modules ({@code servlet6}, {@code servlet61}) already carry
 * the fix as a side effect of PH-004 fallback preparation (the
 * {@code @Override} was removed and replaced with an explanatory comment
 * `@Override removed: getRealPath(String) removed from
 * jakarta.servlet.ServletRequest in 6.0`). The defect is therefore
 * isolated to {@code servlet5} as of T-PH003-15.
 *
 * The javax-side counterpart
 * ({@code adapter.javax.servlet5.ServletRequest#getRealPath}) is NOT a
 * verification target — javax 4.0's {@code ServletRequest} declares
 * {@code getRealPath} as a deprecated method, so the {@code @Override}
 * there is correct.
 *
 * Test strategy — source-grep:
 *   {@code @Override} retention is {@link java.lang.annotation.RetentionPolicy#SOURCE},
 *   so the annotation is not available via runtime reflection. The test
 *   reads the SUT's source file directly (gradle test cwd = module root =
 *   {@code servlet5/}) and asserts that the offending line pattern is
 *   absent. This matches the "no compile-time @Override on missing super
 *   method" signature the SRS expects.
 *
 * Expected failure signature (all three tests):
 *   "expected: no @Override on absent method,
 *    got: compile-time @Override on missing super method"
 *
 * Mock policy (§0.6 compliance): no mocks needed — pure source-file
 * inspection. The SUT is never instantiated.
 */
class GetRealPathDefectTest {

    private static final Path SUT_SOURCE = Paths.get(
        "src/main/java/adapter/jakarta/servlet5/ServletRequest.java");

    private static String readSut() throws IOException {
        return Files.readString(SUT_SOURCE);
    }

    // ----------------------------------------------------------------------
    // AC-1 no_override_annotation
    // jakarta servlet5 ServletRequest.getRealPath(String) must not carry an
    // @Override annotation — jakarta.servlet.ServletRequest does not declare
    // the method in 5.0+, so @Override is a false claim of contract
    // membership.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-007 AC-1: jakarta servlet5 ServletRequest must not @Override absent getRealPath")
    void shouldNotOverrideAbsentGetRealPathInJakartaInterface_AC1() throws IOException {
        String src = readSut();
        // Red phase: defective code declares
        //   "@Override public String getRealPath(String path) {"
        // on L124, so the literal pattern is present and assertFalse fails.
        // Expected red failure: "expected: no @Override on absent method,
        // got: compile-time @Override on missing super method".
        assertFalse(src.contains("@Override public String getRealPath"),
            "adapter must not @Override getRealPath — method absent from jakarta 5.0+ interface");
    }

    // ----------------------------------------------------------------------
    // AC-2 method_absent_or_deprecated_only
    // The method either (a) is absent entirely, or (b) remains as a
    // non-overriding deprecated alias. Either way the {@code @Override}
    // declaration must not appear on it. Source-grep verifies condition (b)
    // via the absence of the offending line pattern; absence (a) is also
    // covered because the pattern is then trivially false.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-007 AC-2: jakarta servlet5 ServletRequest.getRealPath must be absent or deprecated-only (no @Override)")
    void shouldNotOverrideAbsentGetRealPathInJakartaInterface_AC2() throws IOException {
        String src = readSut();
        // Same source-grep as AC-1 — the defective compile-time @Override
        // line pattern must not appear regardless of whether the method
        // itself is kept as a deprecated alias or dropped entirely.
        assertFalse(src.contains("@Override public String getRealPath"),
            "getRealPath must remain without @Override (deprecated alias OK) or be removed entirely");
    }

    // ----------------------------------------------------------------------
    // AC-3 cross_module_consistency
    // The sibling servlet6 / servlet61 jakarta-side ServletRequest already
    // dropped the @Override annotation as a PH-004 fallback prep. servlet5
    // must reach the same state — i.e. either (a) source has the explanatory
    // comment marker `@Override removed:` or (b) the offending pattern is
    // simply absent. This test enforces consistency.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-007 AC-3: jakarta servlet5 ServletRequest must match servlet6/61 fix (no @Override on getRealPath, BM06 regression)")
    void shouldNotOverrideAbsentGetRealPathInJakartaInterface_AC3() throws IOException {
        String src = readSut();
        // The offending pattern must be absent (consistent with servlet6/61).
        assertFalse(src.contains("@Override public String getRealPath"),
            "servlet5 must mirror servlet6/61 — no @Override on getRealPath (BM06 regression)");
        // After fix lands, the source should contain either the comment
        // marker that servlet6/61 use, or the method should be absent. We
        // accept either by re-checking the absence of the defect pattern.
        assertTrue(src.contains("getRealPath") == false
                   || src.contains("@Override removed: getRealPath")
                   || src.contains("@SuppressWarnings(\"deprecation\")\n   public String getRealPath")
                   || src.contains("//noinspection deprecation"),
            "post-fix shape must be either: (a) method absent, (b) explanatory comment marker, or (c) deprecated alias without @Override");
    }
}
