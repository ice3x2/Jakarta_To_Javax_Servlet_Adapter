package adapter.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-003 — AdapterUnwrap marker interface
 * (common 모듈 신설).
 *
 * Scope (this red task — T-PH004-01):
 *   common 모듈에 신설될 {@code adapter.common.AdapterUnwrap<T>} marker
 *   인터페이스의 존재성 + unwrap() 메서드 시그니처 + generic T 정확성
 *   + common 모듈 컴파일 가능성을 검증한다.
 *
 *   본 red task 는 common 모듈만 다루며, 모든 어댑터 클래스가 implements
 *   하는지 / ServletAdapter 사전 unwrap 체크 / 이중 wrap 회피 / equals
 *   /hashCode 위임은 후속 페어 (T-PH004-03 / T-PH004-04) 의 범위다.
 *
 * AC mapping (sidecar action: interface_exists / unwrap_method_present /
 *             generic_T_correct / common_module_compile):
 *   AC-1 interface_exists      — {@code adapter.common.AdapterUnwrap} 클래스가
 *                                classpath 상에 존재하고 interface 다.
 *   AC-2 unwrap_method_present — public {@code unwrap()} 메서드가 선언되어
 *                                있고 인자 0개를 받는다.
 *   AC-3 generic_T_correct     — 인터페이스에 단일 type parameter {@code <T>}
 *                                가 선언되어 있고 unwrap() 반환 타입이 그
 *                                type parameter 와 동일하다.
 *   AC-4 common_module_compile — 본 테스트 클래스가 동일 패키지
 *                                ({@code adapter.common}) 에서 AdapterUnwrap
 *                                심볼을 사용해 컴파일되는 사실 자체가
 *                                common 모듈 컴파일 통과의 외부 관찰
 *                                증거다. 추가로 인터페이스 modifier 가
 *                                public 임을 확인한다.
 *
 * Red phase 의도:
 *   - {@code adapter.common.AdapterUnwrap} 가 아직 src/main 에 존재하지
 *     않으므로 {@code Class.forName("adapter.common.AdapterUnwrap")} 는
 *     {@code ClassNotFoundException} 을 throw 하고 모든 4개 case 가 fail
 *     한다.
 *   - expected_failure_signature (sidecar): "expected: AdapterUnwrap
 *     interface present, got: NoClassDefFoundError" — 본 테스트의 실패
 *     signature 는 Class.forName 의 {@code ClassNotFoundException} 으로
 *     관찰되며, JVM 클래스 로딩 관점에서 동일한 부재 신호 (class-not-
 *     present) 다.
 *
 * Mock 사용 없음 (§0.6).
 */
class AdapterUnwrapTest {

    private static final String FQN = "adapter.common.AdapterUnwrap";

    // -----------------------------------------------------------------
    // AC-1 interface_exists
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-1: adapter.common.AdapterUnwrap 인터페이스가 common 모듈에 존재한다")
    void shouldDefineAdapterUnwrapInterface_FRCONV003_AC1() {
        Class<?> cls;
        try {
            cls = Class.forName(FQN);
        } catch (ClassNotFoundException e) {
            fail("expected: AdapterUnwrap interface present, got: ClassNotFoundException ("
                + FQN + " not on classpath)");
            return;
        }
        assertTrue(cls.isInterface(),
            "expected: " + FQN + " is interface, got: not an interface (modifiers="
                + cls.getModifiers() + ")");
    }

    // -----------------------------------------------------------------
    // AC-2 unwrap_method_present
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-2: AdapterUnwrap.unwrap() 메서드가 선언되어 있다 (인자 0개)")
    void shouldDefineAdapterUnwrapInterface_FRCONV003_AC2() {
        Class<?> cls;
        try {
            cls = Class.forName(FQN);
        } catch (ClassNotFoundException e) {
            fail("expected: AdapterUnwrap interface present, got: ClassNotFoundException ("
                + FQN + " not on classpath)");
            return;
        }

        Method unwrap;
        try {
            unwrap = cls.getDeclaredMethod("unwrap");
        } catch (NoSuchMethodException e) {
            fail("expected: AdapterUnwrap.unwrap() declared (no-arg), got: NoSuchMethodException");
            return;
        }
        assertEquals(0, unwrap.getParameterCount(),
            "expected: unwrap() takes 0 parameters, got: " + unwrap.getParameterCount());
    }

    // -----------------------------------------------------------------
    // AC-3 generic_T_correct
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-3: AdapterUnwrap 가 단일 type parameter <T> 를 선언하고 unwrap() 반환형이 T 이다")
    void shouldDefineAdapterUnwrapInterface_FRCONV003_AC3() {
        Class<?> cls;
        try {
            cls = Class.forName(FQN);
        } catch (ClassNotFoundException e) {
            fail("expected: AdapterUnwrap interface present, got: ClassNotFoundException ("
                + FQN + " not on classpath)");
            return;
        }

        TypeVariable<?>[] typeParams = cls.getTypeParameters();
        assertEquals(1, typeParams.length,
            "expected: exactly one type parameter <T>, got: " + typeParams.length);

        Method unwrap;
        try {
            unwrap = cls.getDeclaredMethod("unwrap");
        } catch (NoSuchMethodException e) {
            fail("expected: AdapterUnwrap.unwrap() declared, got: NoSuchMethodException");
            return;
        }

        Type returnType = unwrap.getGenericReturnType();
        assertTrue(returnType instanceof TypeVariable,
            "expected: unwrap() generic return type is a TypeVariable (the declared <T>), got: "
                + returnType);
        TypeVariable<?> rt = (TypeVariable<?>) returnType;
        assertEquals(typeParams[0].getName(), rt.getName(),
            "expected: unwrap() returns the declared type parameter (same name), got: "
                + rt.getName() + " vs declared " + typeParams[0].getName());
    }

    // -----------------------------------------------------------------
    // AC-4 common_module_compile (+ public modifier)
    // -----------------------------------------------------------------
    @Test
    @DisplayName("FR-CONV-003 AC-4: AdapterUnwrap 는 public 가시성이며 common 모듈에서 사용 가능하다")
    void shouldDefineAdapterUnwrapInterface_FRCONV003_AC4() {
        Class<?> cls;
        try {
            cls = Class.forName(FQN);
        } catch (ClassNotFoundException e) {
            fail("expected: AdapterUnwrap interface present, got: ClassNotFoundException ("
                + FQN + " not on classpath)");
            return;
        }

        assertAll("AdapterUnwrap public + module accessibility",
            () -> assertTrue(java.lang.reflect.Modifier.isPublic(cls.getModifiers()),
                "expected: public interface, got: non-public (modifiers="
                    + cls.getModifiers() + ")"),
            () -> assertEquals("adapter.common", cls.getPackage().getName(),
                "expected: package adapter.common, got: " + cls.getPackage().getName())
        );
    }
}
