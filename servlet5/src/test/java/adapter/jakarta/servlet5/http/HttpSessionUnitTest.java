package adapter.jakarta.servlet5.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G4 — jakarta-side {@link adapter.jakarta.servlet5.http.HttpSession} 위임 단위
 * 테스트 (T-PH005-07).
 *
 * <p>SUT 의 생성자는 package-private 이므로 본 테스트도 동일 패키지
 * ({@code adapter.jakarta.servlet5.http}) 에 배치한다 (servlet5 컨벤션, PartDefectTest
 * 참조). 외부 servlet API ({@code javax.servlet.http.HttpSession}) 만 mock 하고 SUT
 * 어댑터는 mock 하지 않는다. RETURNS_SMART_NULLS 적용 (FR-TEST-001 AC-3).
 *
 * <p>위임 메서드 cover: {@code getId}, {@code getCreationTime},
 * {@code setMaxInactiveInterval}, {@code getAttribute}, {@code setAttribute},
 * {@code invalidate}, {@code getServletContext} (어댑터 wrap 검증).
 *
 * @req FR-TEST-001
 */
class HttpSessionUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static javax.servlet.http.HttpSession newOriginMock() {
        return mock(javax.servlet.http.HttpSession.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getId 위임")
    void shouldDelegateGetId_AC2_74_01() {
        javax.servlet.http.HttpSession origin = newOriginMock();
        when(origin.getId()).thenReturn("SESSION-ABC");
        HttpSession sut = new HttpSession(origin);

        String result = sut.getId();

        verify(origin).getId();
        assertEquals("SESSION-ABC", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getCreationTime 위임 (long 통과)")
    void shouldDelegateGetCreationTime_AC2_74_02() {
        javax.servlet.http.HttpSession origin = newOriginMock();
        when(origin.getCreationTime()).thenReturn(1716000000000L);
        HttpSession sut = new HttpSession(origin);

        long result = sut.getCreationTime();

        verify(origin).getCreationTime();
        assertEquals(1716000000000L, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: setMaxInactiveInterval 위임 (int 인자 통과)")
    void shouldDelegateSetMaxInactiveInterval_AC2_74_03() {
        javax.servlet.http.HttpSession origin = newOriginMock();
        HttpSession sut = new HttpSession(origin);

        sut.setMaxInactiveInterval(1800);

        verify(origin).setMaxInactiveInterval(1800);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: setAttribute 위임 (name + Object value 인자 캡쳐)")
    void shouldDelegateSetAttribute_AC2_74_04() {
        javax.servlet.http.HttpSession origin = newOriginMock();
        HttpSession sut = new HttpSession(origin);
        Object payload = new Object();

        sut.setAttribute("user", payload);

        verify(origin).setAttribute("user", payload);
        assertNotNull(sut);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: invalidate 위임 + isNew false 위임")
    void shouldDelegateInvalidateAndIsNew_AC2_74_05() {
        javax.servlet.http.HttpSession origin = newOriginMock();
        when(origin.isNew()).thenReturn(false);
        HttpSession sut = new HttpSession(origin);

        sut.invalidate();
        boolean isNew = sut.isNew();

        verify(origin).invalidate();
        verify(origin).isNew();
        assertFalse(isNew);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getServletContext — 어댑터로 wrap 된 결과 반환")
    void shouldWrapGetServletContext_AC2_74_06() {
        javax.servlet.http.HttpSession origin = newOriginMock();
        javax.servlet.ServletContext originCtx = mock(javax.servlet.ServletContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getServletContext()).thenReturn(originCtx);
        HttpSession sut = new HttpSession(origin);

        jakarta.servlet.ServletContext result = sut.getServletContext();

        verify(origin).getServletContext();
        assertNotNull(result);
        assertTrue(result instanceof adapter.jakarta.servlet5.ServletContext,
                "getServletContext 결과는 jakarta-side ServletContext 어댑터로 wrap 되어야 한다");
    }
}
