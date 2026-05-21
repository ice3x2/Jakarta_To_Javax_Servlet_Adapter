package adapter.servletElementConverter61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * {@link adapter.servletElementConverter61.ServletConverter} 의
 * {@code convert(javax.servlet.Registration)} polymorphic dispatch 단위 테스트.
 *
 * <p>{@code ServletConverter.convert(javax.servlet.Registration)} 는 6 개의
 * instanceof 분기를 가진다 (코드 L87-99):
 * <ol>
 *   <li>{@code instanceof javax.servlet.ServletRegistration.Dynamic} &rarr;
 *       {@code adapter.jakarta.servlet61.ServletRegistrationDynamic}</li>
 *   <li>{@code instanceof javax.servlet.ServletRegistration} &rarr;
 *       {@code adapter.jakarta.servlet61.ServletRegistration}</li>
 *   <li>{@code instanceof javax.servlet.FilterRegistration.Dynamic} &rarr;
 *       {@code adapter.jakarta.servlet61.FilterRegistrationDynamic}</li>
 *   <li>{@code instanceof javax.servlet.FilterRegistration} &rarr;
 *       {@code adapter.jakarta.servlet61.FilterRegistration}</li>
 *   <li>{@code instanceof javax.servlet.Registration.Dynamic} &rarr;
 *       {@code adapter.jakarta.servlet61.RegistrationDynamic}</li>
 *   <li>else (base Registration) &rarr;
 *       {@code adapter.jakarta.servlet61.Registration}</li>
 * </ol>
 *
 * <p>본 테스트는 6 variant 모두를 단일 메서드 안에서 동시 검증한다 — 어떤 분기가
 * 잘못된 어댑터 클래스로 떨어지면 곧바로 instanceof assertion 실패로 위치 식별이
 * 가능하다. 6 분기 모두 정상 dispatch 됨이 곧 ServletConverter Registration↔
 * polymorphic SSOT 가드이다 (계획 §3.PH-005.T-PH005-06 action).
 *
 * <p><b>FR-CONV-010 default 분기 페어 가드 (계획 §3.PH-005.T-PH005-06 action)</b>:
 * 본 테스트는 ConverterSupport 폴백 정책의 <em>default</em> 분기 시나리오를 검증한다
 * (strict 분기 페어는 T-PH005-07 G9 ServletConverterUnitTest 에서 수행). 어댑터
 * 인스턴스 생성 자체가 정상 통과 (no exception) 함을 보임으로써 default 모드의
 * 폴백 정책 도달성을 가드한다.
 *
 * <p>Mock 정책 (FR-TEST-001 AC-3): javax {@code Registration} 계열 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다. SUT
 * (ServletConverter 가 반환하는 어댑터 인스턴스) 자체는 mock 하지 않는다.
 *
 * <p>분기 5 ({@code Registration.Dynamic}) 검증은 javax {@code Registration.Dynamic} 의
 * 직접 mock 으로 가능하다. 분기 1~4 의 sub-interface 들이 모두 {@code Registration}
 * 을 상속하므로 mock 우선순위는 분기 1 (가장 좁은 ServletRegistration.Dynamic) 이
 * 첫 매칭되는 instanceof 분기로 안전하게 dispatch 된다.
 *
 * @req FR-TEST-001
 */
class ServletConverterTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1 (Registration polymorphic dispatch 6-variant)
    // 6 instanceof 분기 모두에서 javax→jakarta 어댑터 변환이 올바른 jakarta 어댑터
    // 클래스로 dispatch 됨을 검증한다 (FR-CONV-010 default 분기 페어 가드 포함).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletConverter.convert(javax.Registration) 의 6 instanceof 분기가 올바른 jakarta 어댑터 클래스로 dispatch 된다 (FR-CONV-010 default 분기 페어 가드)")
    void shouldConvertServletRegistrationDynamic_AC1() {
        // ---- 분기 1: javax.servlet.ServletRegistration.Dynamic → adapter.jakarta.servlet61.ServletRegistrationDynamic
        javax.servlet.ServletRegistration.Dynamic javaxServletRegDynamic =
            mock(javax.servlet.ServletRegistration.Dynamic.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.Registration result1 =
            ServletConverter.convert((javax.servlet.Registration) javaxServletRegDynamic);
        assertNotNull(result1, "분기 1: 변환 결과는 null 이 아니다");
        assertTrue(result1 instanceof adapter.jakarta.servlet61.ServletRegistrationDynamic,
            "분기 1 (ServletRegistration.Dynamic) 은 jakarta ServletRegistrationDynamic 어댑터로 dispatch 되어야 한다 "
            + "(actual=" + result1.getClass().getName() + ")");

        // ---- 분기 2: javax.servlet.ServletRegistration (non-Dynamic) → adapter.jakarta.servlet61.ServletRegistration
        javax.servlet.ServletRegistration javaxServletReg =
            mock(javax.servlet.ServletRegistration.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.Registration result2 =
            ServletConverter.convert((javax.servlet.Registration) javaxServletReg);
        assertNotNull(result2, "분기 2: 변환 결과는 null 이 아니다");
        assertTrue(result2 instanceof adapter.jakarta.servlet61.ServletRegistration,
            "분기 2 (ServletRegistration) 는 jakarta ServletRegistration 어댑터로 dispatch 되어야 한다 "
            + "(actual=" + result2.getClass().getName() + ")");
        assertTrue(!(result2 instanceof adapter.jakarta.servlet61.ServletRegistrationDynamic),
            "분기 2 는 non-Dynamic 분기여야 한다 (Dynamic 으로 잘못 dispatch 되면 분기 1 이 선점된 것)");

        // ---- 분기 3: javax.servlet.FilterRegistration.Dynamic → adapter.jakarta.servlet61.FilterRegistrationDynamic
        javax.servlet.FilterRegistration.Dynamic javaxFilterRegDynamic =
            mock(javax.servlet.FilterRegistration.Dynamic.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.Registration result3 =
            ServletConverter.convert((javax.servlet.Registration) javaxFilterRegDynamic);
        assertNotNull(result3, "분기 3: 변환 결과는 null 이 아니다");
        assertTrue(result3 instanceof adapter.jakarta.servlet61.FilterRegistrationDynamic,
            "분기 3 (FilterRegistration.Dynamic) 은 jakarta FilterRegistrationDynamic 어댑터로 dispatch 되어야 한다 "
            + "(actual=" + result3.getClass().getName() + ")");

        // ---- 분기 4: javax.servlet.FilterRegistration (non-Dynamic) → adapter.jakarta.servlet61.FilterRegistration
        javax.servlet.FilterRegistration javaxFilterReg =
            mock(javax.servlet.FilterRegistration.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.Registration result4 =
            ServletConverter.convert((javax.servlet.Registration) javaxFilterReg);
        assertNotNull(result4, "분기 4: 변환 결과는 null 이 아니다");
        assertTrue(result4 instanceof adapter.jakarta.servlet61.FilterRegistration,
            "분기 4 (FilterRegistration) 는 jakarta FilterRegistration 어댑터로 dispatch 되어야 한다 "
            + "(actual=" + result4.getClass().getName() + ")");
        assertTrue(!(result4 instanceof adapter.jakarta.servlet61.FilterRegistrationDynamic),
            "분기 4 는 non-Dynamic 분기여야 한다 (Dynamic 으로 잘못 dispatch 되면 분기 3 이 선점된 것)");

        // ---- 분기 5: javax.servlet.Registration.Dynamic (base Dynamic) → adapter.jakarta.servlet61.RegistrationDynamic
        javax.servlet.Registration.Dynamic javaxBaseRegDynamic =
            mock(javax.servlet.Registration.Dynamic.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.Registration result5 =
            ServletConverter.convert((javax.servlet.Registration) javaxBaseRegDynamic);
        assertNotNull(result5, "분기 5: 변환 결과는 null 이 아니다");
        assertTrue(result5 instanceof adapter.jakarta.servlet61.RegistrationDynamic,
            "분기 5 (Registration.Dynamic base) 는 jakarta RegistrationDynamic 어댑터로 dispatch 되어야 한다 "
            + "(actual=" + result5.getClass().getName() + ")");

        // ---- 분기 6 (else / FR-CONV-010 default 분기): javax.servlet.Registration base → adapter.jakarta.servlet61.Registration
        javax.servlet.Registration javaxBaseReg =
            mock(javax.servlet.Registration.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.Registration result6 =
            ServletConverter.convert(javaxBaseReg);
        assertNotNull(result6, "분기 6 (default): 변환 결과는 null 이 아니다");
        assertTrue(result6 instanceof adapter.jakarta.servlet61.Registration,
            "분기 6 (else / FR-CONV-010 default) 은 jakarta Registration base 어댑터로 dispatch 되어야 한다 "
            + "(actual=" + result6.getClass().getName() + ")");
        // base Registration dispatch 는 그 어떤 sub-Dynamic / sub-Filter / sub-Servlet 분기도 매칭되지 않는다.
        assertTrue(!(result6 instanceof adapter.jakarta.servlet61.RegistrationDynamic),
            "분기 6 (default) 은 어떤 sub-Dynamic 도 아니어야 한다");
        assertTrue(!(result6 instanceof adapter.jakarta.servlet61.ServletRegistration),
            "분기 6 (default) 은 ServletRegistration 으로 잘못 dispatch 되어선 안 된다");
        assertTrue(!(result6 instanceof adapter.jakarta.servlet61.FilterRegistration),
            "분기 6 (default) 은 FilterRegistration 으로 잘못 dispatch 되어선 안 된다");

        // 결과 6 개 모두 서로 다른 어댑터 인스턴스로 dispatch 되었는지 sanity check
        // (sub-interface 가 super-interface 분기로 fall-through 되는 회귀 발견 보조)
        assertSame(result1.getClass(), adapter.jakarta.servlet61.ServletRegistrationDynamic.class,
            "분기 1 결과 클래스 SSOT");
        assertSame(result2.getClass(), adapter.jakarta.servlet61.ServletRegistration.class,
            "분기 2 결과 클래스 SSOT");
        assertSame(result3.getClass(), adapter.jakarta.servlet61.FilterRegistrationDynamic.class,
            "분기 3 결과 클래스 SSOT");
        assertSame(result4.getClass(), adapter.jakarta.servlet61.FilterRegistration.class,
            "분기 4 결과 클래스 SSOT");
        assertSame(result5.getClass(), adapter.jakarta.servlet61.RegistrationDynamic.class,
            "분기 5 결과 클래스 SSOT");
        assertSame(result6.getClass(), adapter.jakarta.servlet61.Registration.class,
            "분기 6 (FR-CONV-010 default) 결과 클래스 SSOT");
    }
}
