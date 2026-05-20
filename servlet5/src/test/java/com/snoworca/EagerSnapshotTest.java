package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-006 (eager snapshot, SPEC-11)
 * + FR-CONV-007 (Stream variant fidelity, SPEC-12)
 * + FR-CONV-008 (setContentLengthLong long precision, SPEC-13)
 * + FR-CONV-009 (side-effect propagation, SPEC-16) at the *all-paths*
 * granularity for the servlet5 module.
 *
 * Scope (T-PH004-07 — RED):
 *   - FR-CONV-006 AC-1: getCookies/getParts/getServletRegistrations/
 *     getFilterRegistrations null pass-through (no NPE).
 *   - FR-CONV-006 AC-2: each invocation returns a fresh array (no `==`).
 *   - FR-CONV-006 AC-3: mutation of returned array does not propagate.
 *   - FR-CONV-007 AC-1..AC-9: ServletInputStream/ServletOutputStream
 *     adapter delegates to like-named methods on the original (read,
 *     read(b), read(b,off,len), readLine, isFinished, isReady,
 *     setReadListener, write*, setWriteListener). FR-FIX-001 lock-in.
 *   - FR-CONV-008 AC-1: setContentLengthLong(Long.MAX_VALUE) delegates to
 *     the like-named long method on the original (no int narrow).
 *   - FR-CONV-008 AC-2: setContentLength((int) Long.MAX_VALUE) is NOT
 *     invoked. FR-FIX-003 lock-in.
 *   - FR-CONV-009 AC-1..AC-7: flushBuffer/reset/resetBuffer/complete/
 *     invalidate/sendError/sendRedirect each propagate 1:1.
 *
 * Red phase 의도:
 *   - T-PH004-08 (green) 에서 도입 예정인 production helper class
 *     `adapter.common.ConversionContract` 와 그 정적 메서드 집합 (각 AC 별 1:1
 *     계약 검증자) 이 현재 시점에는 부재한다.
 *   - 본 test 는 `ConversionContract` 를 reflection 으로 lookup 하고, 각 AC 에
 *     대응되는 정적 메서드 (verify_FR_CONV_006_AC1, …, verify_FR_CONV_009_AC7)
 *     를 호출한다. 두 단계 모두 부재로 인해 fail (red).
 *   - 본 패턴은 T-PH004-03 (AdapterUnwrapAllClassesTest, 미존재 implements
 *     gate 검증) / T-PH004-05 (ServletExceptionConversionTest, 미존재
 *     ConverterSupport helper lookup) 와 동일한 missing-helper RED 의도이며,
 *     21 개 test case 가 모두 동일한 expected_failure_signature 로 fail 한다.
 *
 * Green 단계 (T-PH004-08) 에서 충족 방법:
 *   - `common/src/main/java/adapter/common/ConversionContract.java` 신설.
 *   - 21 개 정적 메서드 verify_FR_CONV_006_AC1 … verify_FR_CONV_009_AC7
 *     를 정의하고, 각 메서드 내부에서 실제 어댑터 인스턴스를 직접 생성하여
 *     AC 계약을 단언한다 (mock 없이 직접 fake / spy 구현).
 *   - 본 test 는 ConversionContract.verify_XXX() 호출 결과가 정상 return
 *     (혹은 명시된 AssertionError 가 발생하지 않음) 을 검증한다.
 *
 * expected_failure_signature (sidecar):
 *   "expected: collection snapshot + stream delegation + long preservation
 *    + side-effect propagation, got: at least one defective path"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-006-AC1-01 → _FRCONV006_AC1
 *   TC-REQ-FR-CONV-006-AC2-01 → _FRCONV006_AC2
 *   TC-REQ-FR-CONV-006-AC3-01 → _FRCONV006_AC3
 *   TC-REQ-FR-CONV-007-AC1-01 → _FRCONV007_AC1
 *   TC-REQ-FR-CONV-007-AC2-01 → _FRCONV007_AC2
 *   TC-REQ-FR-CONV-007-AC3-01 → _FRCONV007_AC3
 *   TC-REQ-FR-CONV-007-AC4-01 → _FRCONV007_AC4
 *   TC-REQ-FR-CONV-007-AC5-01 → _FRCONV007_AC5
 *   TC-REQ-FR-CONV-007-AC6-01 → _FRCONV007_AC6
 *   TC-REQ-FR-CONV-007-AC7-01 → _FRCONV007_AC7
 *   TC-REQ-FR-CONV-007-AC8-01 → _FRCONV007_AC8
 *   TC-REQ-FR-CONV-007-AC9-01 → _FRCONV007_AC9
 *   TC-REQ-FR-CONV-008-AC1-01 → _FRCONV008_AC1
 *   TC-REQ-FR-CONV-008-AC2-01 → _FRCONV008_AC2
 *   TC-REQ-FR-CONV-009-AC1-01 → _FRCONV009_AC1
 *   TC-REQ-FR-CONV-009-AC2-01 → _FRCONV009_AC2
 *   TC-REQ-FR-CONV-009-AC3-01 → _FRCONV009_AC3
 *   TC-REQ-FR-CONV-009-AC4-01 → _FRCONV009_AC4
 *   TC-REQ-FR-CONV-009-AC5-01 → _FRCONV009_AC5
 *   TC-REQ-FR-CONV-009-AC6-01 → _FRCONV009_AC6
 *   TC-REQ-FR-CONV-009-AC7-01 → _FRCONV009_AC7
 *
 * Mock 사용 없음 (§0.6) — reflection 으로 production helper 부재를 직접 검출.
 *
 * @req FR-CONV-006
 * @req FR-CONV-007
 * @req FR-CONV-008
 * @req FR-CONV-009
 */
class EagerSnapshotTest {

    private static final String CONTRACT_FQN = "adapter.common.ConversionContract";

    private static final String FAILURE_SIGNATURE =
            "expected: collection snapshot + stream delegation + long preservation"
                    + " + side-effect propagation, got: at least one defective path";

    // -----------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-006
     * @req FR-CONV-007
     * @req FR-CONV-008
     * @req FR-CONV-009
     */
    private static Class<?> lookupContract() {
        try {
            return Class.forName(CONTRACT_FQN, true,
                    EagerSnapshotTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            fail(FAILURE_SIGNATURE
                    + " — " + CONTRACT_FQN + " not present on classpath");
            throw new AssertionError("unreachable");
        }
    }

    /**
     * @req FR-CONV-006
     * @req FR-CONV-007
     * @req FR-CONV-008
     * @req FR-CONV-009
     */
    private static void invokeContract(String methodName) {
        Class<?> clazz = lookupContract();
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE
                    + " — " + CONTRACT_FQN + "." + methodName + "() not declared");
            throw new AssertionError("unreachable");
        }
        assertNotNull(method, FAILURE_SIGNATURE
                + " — " + CONTRACT_FQN + "." + methodName + " resolved to null");
        try {
            method.setAccessible(true);
            method.invoke(null);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail(FAILURE_SIGNATURE
                    + " — " + CONTRACT_FQN + "." + methodName + "() threw: "
                    + cause.getClass().getName() + ": " + cause.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-006 — eager snapshot (3 AC)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-006
     */
    @Test
    @DisplayName("FR-CONV-006 AC-1: null pass-through (no NPE) on getCookies/getParts/getServletRegistrations/getFilterRegistrations")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV006_AC1() {
        invokeContract("verify_FR_CONV_006_AC1");
    }

    /**
     * @req FR-CONV-006
     */
    @Test
    @DisplayName("FR-CONV-006 AC-2: each invocation returns a fresh array (no == identity)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV006_AC2() {
        invokeContract("verify_FR_CONV_006_AC2");
    }

    /**
     * @req FR-CONV-006
     */
    @Test
    @DisplayName("FR-CONV-006 AC-3: mutation of returned array is isolated from source")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV006_AC3() {
        invokeContract("verify_FR_CONV_006_AC3");
    }

    // -----------------------------------------------------------------
    // FR-CONV-007 — Stream variant fidelity (9 AC)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-1: ServletInputStream.read() delegates to origin.read() (no readLine redirect)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC1() {
        invokeContract("verify_FR_CONV_007_AC1");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-2: ServletInputStream.read(byte[]) delegates to origin.read(byte[]) (no readLine redirect)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC2() {
        invokeContract("verify_FR_CONV_007_AC2");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-3: ServletInputStream.read(byte[],int,int) delegates to origin.read(byte[],int,int) (FR-FIX-001)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC3() {
        invokeContract("verify_FR_CONV_007_AC3");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-4: ServletInputStream.readLine(byte[],int,int) delegates to origin.readLine(...)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC4() {
        invokeContract("verify_FR_CONV_007_AC4");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-5: ServletInputStream.isFinished() delegates to origin.isFinished()")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC5() {
        invokeContract("verify_FR_CONV_007_AC5");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-6: ServletInputStream.isReady() delegates to origin.isReady()")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC6() {
        invokeContract("verify_FR_CONV_007_AC6");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-7: ServletInputStream.setReadListener(listener) delegates with adapted argument")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC7() {
        invokeContract("verify_FR_CONV_007_AC7");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-8: ServletOutputStream.write(...)+isReady()+setWriteListener(listener) same delegate pattern")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC8() {
        invokeContract("verify_FR_CONV_007_AC8");
    }

    /**
     * @req FR-CONV-007
     */
    @Test
    @DisplayName("FR-CONV-007 AC-9: binary body without line terminator is forwarded verbatim through the stream adapter")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV007_AC9() {
        invokeContract("verify_FR_CONV_007_AC9");
    }

    // -----------------------------------------------------------------
    // FR-CONV-008 — setContentLengthLong precision (2 AC)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-008
     */
    @Test
    @DisplayName("FR-CONV-008 AC-1: setContentLengthLong(Long.MAX_VALUE) delegates to origin.setContentLengthLong(Long.MAX_VALUE)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV008_AC1() {
        invokeContract("verify_FR_CONV_008_AC1");
    }

    /**
     * @req FR-CONV-008
     */
    @Test
    @DisplayName("FR-CONV-008 AC-2: origin.setContentLength((int) Long.MAX_VALUE) is NOT invoked (no silent narrow)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV008_AC2() {
        invokeContract("verify_FR_CONV_008_AC2");
    }

    // -----------------------------------------------------------------
    // FR-CONV-009 — side-effect 1:1 propagation (7 AC)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-1: flushBuffer() propagates 1:1 immediately")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC1() {
        invokeContract("verify_FR_CONV_009_AC1");
    }

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-2: reset() propagates 1:1 immediately")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC2() {
        invokeContract("verify_FR_CONV_009_AC2");
    }

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-3: resetBuffer() propagates 1:1 immediately")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC3() {
        invokeContract("verify_FR_CONV_009_AC3");
    }

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-4: AsyncContext.complete() propagates 1:1 immediately")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC4() {
        invokeContract("verify_FR_CONV_009_AC4");
    }

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-5: HttpSession.invalidate() propagates 1:1 immediately")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC5() {
        invokeContract("verify_FR_CONV_009_AC5");
    }

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-6: sendError(int) and sendError(int,String) each propagate to like-named origin")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC6() {
        invokeContract("verify_FR_CONV_009_AC6");
    }

    /**
     * @req FR-CONV-009
     */
    @Test
    @DisplayName("FR-CONV-009 AC-7: sendRedirect(String) propagates to origin.sendRedirect(String)")
    void shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths_FRCONV009_AC7() {
        invokeContract("verify_FR_CONV_009_AC7");
    }
}
