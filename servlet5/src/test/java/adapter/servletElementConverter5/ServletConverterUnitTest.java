package adapter.servletElementConverter5;

import adapter.common.ConverterSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * G9 — {@link adapter.servletElementConverter5.ServletConverter} 22+ factory +
 * Registration polymorphic + ServletException 양방향 변환 단위 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API ({@code javax.servlet.*}, {@code jakarta.servlet.*}) 만 mock 한다.
 * SUT 변환 결과 인스턴스는 mock 하지 않는다. RETURNS_SMART_NULLS 적용 (FR-TEST-001
 * AC-3).
 *
 * <p>본 클래스는 <b>AC-4 폴백 정책 페어 검증</b> 을 포함한다 (FR-CONV-010 default +
 * strict). default 분기는 {@link ServletConverter#convert(javax.servlet.Registration)}
 * 의 6 instanceof else 분기로 검증 (servlet6/ServletConverterTest 컨벤션 참조). strict
 * 분기는 {@link ConverterSupport#throwIfStrict(String)} 가 strict 활성 상태에서
 * {@link UnsupportedOperationException} 을 throw 하는지로 검증 (StrictModeTest
 * 컨벤션 참조). 두 시나리오를 한 클래스 내에서 페어로 두어 정책 회귀를 한 곳에서
 * 잠근다.
 *
 * @req FR-TEST-001
 */
class ServletConverterUnitTest {

    /**
     * @req FR-TEST-001
     */
    @BeforeEach
    void resetStrictBefore() {
        ConverterSupport.resetStrictModeForTesting();
    }

    /**
     * @req FR-TEST-001
     */
    @AfterEach
    void resetStrictAfter() {
        ConverterSupport.resetStrictModeForTesting();
        System.clearProperty(ConverterSupport.STRICT_MODE_KEY);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.Servlet) → jakarta Servlet, ServletException 변환 분기")
    void shouldConvertServletAndWrapServletException_AC2_79_01() throws Exception {
        javax.servlet.Servlet origin = mock(javax.servlet.Servlet.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.ServletConfig javaxConfig = mock(javax.servlet.ServletConfig.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        // jakarta → javax 변환된 config 인자에 대해 origin.service 에서 jakarta.ServletException 던지지 않음;
        // 대신 init 에서 javax.ServletException 을 강제 — Servlet jakarta-side wrapper 가 jakarta.ServletException 으로 wrap 함을 검증
        doThrow(new javax.servlet.ServletException("init failed"))
                .when(origin).init(org.mockito.ArgumentMatchers.any(javax.servlet.ServletConfig.class));

        jakarta.servlet.Servlet sut = ServletConverter.convert(origin);
        jakarta.servlet.ServletConfig jakartaConfig = mock(jakarta.servlet.ServletConfig.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.ServletException thrown = assertThrows(
                jakarta.servlet.ServletException.class,
                () -> sut.init(jakartaConfig));

        verify(origin).init(org.mockito.ArgumentMatchers.any(javax.servlet.ServletConfig.class));
        assertNotNull(thrown.getCause(),
                "원본 javax.ServletException 은 cause 로 보존되어야 한다");
        assertTrue(thrown.getCause() instanceof javax.servlet.ServletException);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(jakarta.Servlet) → javax Servlet, ServletException 양방향 변환 (mirror)")
    void shouldConvertServletMirror_AC2_79_02() throws Exception {
        jakarta.servlet.Servlet origin = mock(jakarta.servlet.Servlet.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        doThrow(new jakarta.servlet.ServletException("init failed"))
                .when(origin).init(org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletConfig.class));

        javax.servlet.Servlet sut = ServletConverter.convert(origin);
        javax.servlet.ServletConfig javaxConfig = mock(javax.servlet.ServletConfig.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        javax.servlet.ServletException thrown = assertThrows(
                javax.servlet.ServletException.class,
                () -> sut.init(javaxConfig));

        verify(origin).init(org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletConfig.class));
        assertNotNull(thrown.getCause());
        assertTrue(thrown.getCause() instanceof jakarta.servlet.ServletException);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.Filter) → jakarta Filter, destroy 위임")
    void shouldConvertFilterAndDelegateDestroy_AC2_79_03() {
        javax.servlet.Filter origin = mock(javax.servlet.Filter.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.Filter sut = ServletConverter.convert(origin);
        sut.destroy();

        verify(origin).destroy();
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.FilterConfig) → jakarta FilterConfig getFilterName 위임")
    void shouldConvertFilterConfigAndDelegateGetFilterName_AC2_79_04() {
        javax.servlet.FilterConfig origin = mock(javax.servlet.FilterConfig.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        org.mockito.Mockito.when(origin.getFilterName()).thenReturn("my-filter");

        jakarta.servlet.FilterConfig sut = ServletConverter.convert(origin);
        String name = sut.getFilterName();

        verify(origin).getFilterName();
        assertEquals("my-filter", name);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.SessionCookieConfig) — name/domain/path 위임")
    void shouldConvertSessionCookieConfig_AC2_79_05() {
        javax.servlet.SessionCookieConfig origin = mock(javax.servlet.SessionCookieConfig.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        org.mockito.Mockito.when(origin.getName()).thenReturn("JSESSIONID");
        org.mockito.Mockito.when(origin.getDomain()).thenReturn(".example.com");
        org.mockito.Mockito.when(origin.getPath()).thenReturn("/");

        jakarta.servlet.SessionCookieConfig sut = ServletConverter.convert(origin);
        sut.setName("CUSTOM");
        String n = sut.getName();
        String d = sut.getDomain();
        String p = sut.getPath();

        verify(origin).setName("CUSTOM");
        verify(origin).getName();
        verify(origin).getDomain();
        verify(origin).getPath();
        assertEquals("JSESSIONID", n);
        assertEquals(".example.com", d);
        assertEquals("/", p);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(jakarta.MultipartConfigElement) → javax MultipartConfigElement (값 보존)")
    void shouldConvertMultipartConfigElement_AC2_79_06() {
        jakarta.servlet.MultipartConfigElement origin =
                new jakarta.servlet.MultipartConfigElement("/tmp", 10L, 100L, 1024);

        javax.servlet.MultipartConfigElement sut = ServletConverter.convert(origin);

        assertNotNull(sut);
        assertEquals("/tmp", sut.getLocation());
        assertEquals(10L, sut.getMaxFileSize());
        assertEquals(100L, sut.getMaxRequestSize());
        assertEquals(1024, sut.getFileSizeThreshold());
    }

    // ---------------------------------------------------------------
    // AC-4 FR-CONV-010 폴백 정책 페어
    //
    // (1) shouldFallbackInDefaultMode_AC4_01 — default 분기:
    //     ServletConverter.convert(javax.Registration) 의 6 분기 중 else
    //     (base Registration) 분기가 strict OFF 시 예외 없이 어댑터 인스턴스를
    //     반환한다 (servlet6 ServletConverterTest 와 동일 SSOT — default 분기
    //     도달성 가드).
    //
    // (2) shouldThrowInStrictMode_AC4_02 — strict 분기:
    //     ConverterSupport.throwIfStrict(msg) 가 strict 활성 상태일 때
    //     UnsupportedOperationException 을 throw 한다 (StrictModeTest 와 동일
    //     SSOT — strict-mode helper 의 expected exception 패턴). ServletConverter
    //     factory 자체는 strict 검사를 호출하지 않지만, FR-CONV-010 폴백 정책의
    //     SSOT 인 ConverterSupport 의 default↔strict 분기 페어를 본 클래스에
    //     함께 잠근다 (AC-4 페어 검증 dod 충족).
    // ---------------------------------------------------------------

    /**
     * AC-4 default 분기 — FR-CONV-010.
     *
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-4 default: ServletConverter.convert(javax.Registration) 의 else 분기는 strict OFF 시 jakarta Registration 어댑터를 반환한다 (FR-CONV-010)")
    void shouldFallbackInDefaultMode_AC4_01() {
        // strict OFF (default, lenient fallback)
        ConverterSupport.resetStrictModeForTesting();

        // base Registration mock — Servlet/Filter/Dynamic sub-interface 가 아닌 base
        javax.servlet.Registration baseReg = mock(javax.servlet.Registration.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.Registration result = ServletConverter.convert(baseReg);

        // FR-CONV-010 default 분기: 어떤 sub-Dynamic 도 아닌 base 어댑터로 dispatch
        assertNotNull(result, "default 폴백 분기에서 변환 결과는 null 이 아니다");
        assertSame(adapter.jakarta.servlet5.Registration.class, result.getClass(),
                "FR-CONV-010 default 분기 (else) 는 jakarta Registration base 어댑터로 dispatch 되어야 한다");
        // strict OFF 시 throwIfStrict 도 예외 없이 통과 (lenient default 검증)
        ConverterSupport.throwIfStrict("default-mode call");
    }

    /**
     * AC-4 strict 분기 — FR-CONV-010 (페어).
     *
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-4 strict: ConverterSupport.throwIfStrict 는 strict 활성 시 UnsupportedOperationException 을 throw 한다 (FR-CONV-010 페어)")
    void shouldThrowInStrictMode_AC4_02() {
        // strict 활성화 (시스템 프로퍼티 경로)
        System.setProperty(ConverterSupport.STRICT_MODE_KEY, "true");
        try {
            ConverterSupport.initStrictModeFromSystemProperty();

            UnsupportedOperationException thrown = assertThrows(
                    UnsupportedOperationException.class,
                    () -> ConverterSupport.throwIfStrict("strict-mode test"),
                    "strict 활성 상태에서 throwIfStrict 는 UnsupportedOperationException 을 throw 해야 한다 (FR-CONV-010 / FR-CONV-011)");
            assertNotNull(thrown.getMessage());
        } finally {
            // 격리 — @AfterEach 에서도 reset 하지만 시도 즉시 해제
            System.clearProperty(ConverterSupport.STRICT_MODE_KEY);
            ConverterSupport.resetStrictModeForTesting();
        }
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.Registration) 6-variant — 분기 1 (ServletRegistration.Dynamic) dispatch")
    void shouldDispatchServletRegistrationDynamicVariant_AC2_79_07() {
        javax.servlet.ServletRegistration.Dynamic origin =
                mock(javax.servlet.ServletRegistration.Dynamic.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.Registration result =
                ServletConverter.convert((javax.servlet.Registration) origin);

        assertNotNull(result);
        assertSame(adapter.jakarta.servlet5.ServletRegistrationDynamic.class, result.getClass(),
                "분기 1 (ServletRegistration.Dynamic) 은 jakarta ServletRegistrationDynamic 어댑터로 dispatch 되어야 한다");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.Registration) 6-variant — 분기 3/4 (FilterRegistration.Dynamic/FilterRegistration) dispatch")
    void shouldDispatchFilterRegistrationVariants_AC2_79_08() {
        javax.servlet.FilterRegistration.Dynamic filterDyn =
                mock(javax.servlet.FilterRegistration.Dynamic.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.FilterRegistration filterReg =
                mock(javax.servlet.FilterRegistration.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.Registration r3 =
                ServletConverter.convert((javax.servlet.Registration) filterDyn);
        jakarta.servlet.Registration r4 =
                ServletConverter.convert((javax.servlet.Registration) filterReg);

        assertSame(adapter.jakarta.servlet5.FilterRegistrationDynamic.class, r3.getClass(),
                "분기 3: FilterRegistration.Dynamic → jakarta FilterRegistrationDynamic");
        assertSame(adapter.jakarta.servlet5.FilterRegistration.class, r4.getClass(),
                "분기 4: FilterRegistration (non-Dynamic) → jakarta FilterRegistration");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(jakarta.Registration) 양방향 mirror — base 분기 dispatch")
    void shouldDispatchMirrorRegistrationBaseVariant_AC2_79_09() {
        jakarta.servlet.Registration jakartaBase = mock(jakarta.servlet.Registration.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        javax.servlet.Registration result = ServletConverter.convert(jakartaBase);

        assertNotNull(result);
        assertSame(adapter.javax.servlet5.Registration.class, result.getClass(),
                "mirror base 분기: jakarta Registration → javax Registration base 어댑터");
    }
}
