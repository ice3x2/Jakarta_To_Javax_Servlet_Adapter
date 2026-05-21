package adapter.jakarta.servlet5.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G2 — jakarta-side {@link adapter.jakarta.servlet5.http.HttpServletRequest}
 * HTTP-specific 위임 단위 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API ({@code javax.servlet.http.HttpServletRequest}) 만 mock 하고
 * SUT 어댑터는 mock 하지 않는다. mock 생성에는
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 를 적용한다 (FR-TEST-001
 * AC-3 잠금). {@code verify(origin)} 호출은 {@code assertEquals} 보다 앞에 배치.
 *
 * <p>HTTP-specific 위임 메서드 cover:
 * {@code getMethod}, {@code getRequestURI}, {@code getQueryString},
 * {@code getDateHeader}, {@code getIntHeader}, {@code getHeader},
 * {@code isUserInRole}, {@code login} (ServletException 변환 분기 포함),
 * {@code getSession(boolean)}, {@code getContextPath}.
 *
 * @req FR-TEST-001
 */
class HttpServletRequestUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static javax.servlet.http.HttpServletRequest newOriginMock() {
        return mock(javax.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getMethod 위임")
    void shouldDelegateGetMethod_AC2_72_01() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getMethod()).thenReturn("POST");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getMethod();

        verify(origin).getMethod();
        assertEquals("POST", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getRequestURI 위임")
    void shouldDelegateGetRequestURI_AC2_72_02() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getRequestURI()).thenReturn("/api/v1/users");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getRequestURI();

        verify(origin).getRequestURI();
        assertEquals("/api/v1/users", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getQueryString 위임")
    void shouldDelegateGetQueryString_AC2_72_03() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getQueryString()).thenReturn("foo=bar&baz=qux");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getQueryString();

        verify(origin).getQueryString();
        assertEquals("foo=bar&baz=qux", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getDateHeader 위임 (long 인자 통과)")
    void shouldDelegateGetDateHeader_AC2_72_04() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getDateHeader("If-Modified-Since")).thenReturn(1716249600000L);
        HttpServletRequest sut = new HttpServletRequest(origin);

        long result = sut.getDateHeader("If-Modified-Since");

        verify(origin).getDateHeader("If-Modified-Since");
        assertEquals(1716249600000L, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getIntHeader 위임 (int 인자 통과)")
    void shouldDelegateGetIntHeader_AC2_72_05() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getIntHeader("X-Retry-Count")).thenReturn(3);
        HttpServletRequest sut = new HttpServletRequest(origin);

        int result = sut.getIntHeader("X-Retry-Count");

        verify(origin).getIntHeader("X-Retry-Count");
        assertEquals(3, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getHeader 위임")
    void shouldDelegateGetHeader_AC2_72_06() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getHeader("X-Forwarded-For")).thenReturn("198.51.100.7");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getHeader("X-Forwarded-For");

        verify(origin).getHeader("X-Forwarded-For");
        assertEquals("198.51.100.7", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: isUserInRole 위임 (boolean 통과)")
    void shouldDelegateIsUserInRole_AC2_72_07() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.isUserInRole("admin")).thenReturn(true);
        when(origin.isUserInRole("guest")).thenReturn(false);
        HttpServletRequest sut = new HttpServletRequest(origin);

        boolean admin = sut.isUserInRole("admin");
        boolean guest = sut.isUserInRole("guest");

        verify(origin).isUserInRole("admin");
        verify(origin).isUserInRole("guest");
        assertTrue(admin);
        assertFalse(guest);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: login (ServletException 변환 분기 — javax→jakarta)")
    void shouldDelegateLoginAndConvertServletException_AC2_72_08() throws Exception {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        doThrow(new javax.servlet.ServletException("bad credentials"))
                .when(origin).login("alice", "secret");
        HttpServletRequest sut = new HttpServletRequest(origin);

        jakarta.servlet.ServletException thrown = assertThrows(
                jakarta.servlet.ServletException.class,
                () -> sut.login("alice", "secret"));

        verify(origin).login("alice", "secret");
        assertNotNull(thrown.getCause());
        assertTrue(thrown.getCause() instanceof javax.servlet.ServletException,
                "원본 javax.ServletException 은 cause 로 보존되어야 한다");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getSession(boolean) 양쪽 분기 — HttpSession 어댑터 wrap")
    void shouldDelegateGetSessionBooleanBranch_AC2_72_09() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        javax.servlet.http.HttpSession originSession =
                mock(javax.servlet.http.HttpSession.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getSession(true)).thenReturn(originSession);
        when(origin.getSession(false)).thenReturn(originSession);
        HttpServletRequest sut = new HttpServletRequest(origin);

        jakarta.servlet.http.HttpSession sessTrue = sut.getSession(true);
        jakarta.servlet.http.HttpSession sessFalse = sut.getSession(false);

        verify(origin).getSession(true);
        verify(origin).getSession(false);
        // jakarta 어댑터 HttpSession wrapper 인스턴스 (SUT 가 mock 이 아님)
        assertNotNull(sessTrue);
        assertNotNull(sessFalse);
        assertTrue(sessTrue instanceof HttpSession,
                "getSession(true) 결과는 어댑터 HttpSession 으로 wrap 되어야 한다");
        assertTrue(sessFalse instanceof HttpSession,
                "getSession(false) 결과는 어댑터 HttpSession 으로 wrap 되어야 한다");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getContextPath 위임")
    void shouldDelegateGetContextPath_AC2_72_10() {
        javax.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getContextPath()).thenReturn("/app");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getContextPath();

        verify(origin).getContextPath();
        assertEquals("/app", result);
    }
}
