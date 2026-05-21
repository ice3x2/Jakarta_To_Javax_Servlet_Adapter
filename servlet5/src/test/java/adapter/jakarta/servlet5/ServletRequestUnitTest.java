package adapter.jakarta.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G1 — jakarta-side {@link adapter.jakarta.servlet5.ServletRequest} base 위임
 * 단위 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API ({@code javax.servlet.ServletRequest}) 만 mock 하고 SUT 어댑터
 * 는 mock 하지 않는다. mock 생성에는 {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)}
 * 를 적용한다 (FR-TEST-001 AC-3 잠금). {@code verify(origin)} 호출은
 * {@code assertEquals} 보다 앞에 배치 (servlet5 컨벤션, ServletResponseDefectTest 참조).
 *
 * <p>본 클래스는 base {@code ServletRequest} 의 위임 메서드를 covers:
 * {@code getContentType}, {@code getProtocol}, {@code getScheme}, {@code getServerName},
 * {@code getServerPort}, {@code getRemoteAddr}, {@code getLocale}, {@code setAttribute}.
 *
 * @req FR-TEST-001
 */
class ServletRequestUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static javax.servlet.ServletRequest newOriginMock() {
        return mock(javax.servlet.ServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-1/AC-2: getContentType 위임 + 반환값 통과")
    void shouldDelegateGetContentType_AC2_01() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getContentType()).thenReturn("application/json");
        ServletRequest sut = new ServletRequest(origin);

        String result = sut.getContentType();

        verify(origin).getContentType();
        assertEquals("application/json", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getProtocol 위임 + 반환값 통과")
    void shouldDelegateGetProtocol_AC2_02() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getProtocol()).thenReturn("HTTP/1.1");
        ServletRequest sut = new ServletRequest(origin);

        String result = sut.getProtocol();

        verify(origin).getProtocol();
        assertEquals("HTTP/1.1", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getScheme 위임 + 반환값 통과")
    void shouldDelegateGetScheme_AC2_03() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getScheme()).thenReturn("https");
        ServletRequest sut = new ServletRequest(origin);

        String result = sut.getScheme();

        verify(origin).getScheme();
        assertEquals("https", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getServerName 위임 + 반환값 통과")
    void shouldDelegateGetServerName_AC2_04() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getServerName()).thenReturn("example.com");
        ServletRequest sut = new ServletRequest(origin);

        String result = sut.getServerName();

        verify(origin).getServerName();
        assertEquals("example.com", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getServerPort 위임 + 반환값 통과")
    void shouldDelegateGetServerPort_AC2_05() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getServerPort()).thenReturn(8443);
        ServletRequest sut = new ServletRequest(origin);

        int result = sut.getServerPort();

        verify(origin).getServerPort();
        assertEquals(8443, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getRemoteAddr 위임 + 반환값 통과")
    void shouldDelegateGetRemoteAddr_AC2_06() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getRemoteAddr()).thenReturn("203.0.113.42");
        ServletRequest sut = new ServletRequest(origin);

        String result = sut.getRemoteAddr();

        verify(origin).getRemoteAddr();
        assertEquals("203.0.113.42", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getLocale 위임 + 반환값 통과")
    void shouldDelegateGetLocale_AC2_07() {
        javax.servlet.ServletRequest origin = newOriginMock();
        when(origin.getLocale()).thenReturn(Locale.KOREA);
        ServletRequest sut = new ServletRequest(origin);

        Locale result = sut.getLocale();

        verify(origin).getLocale();
        assertEquals(Locale.KOREA, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: setAttribute 위임 (name + value 인자 통과)")
    void shouldDelegateSetAttribute_AC2_71() {
        javax.servlet.ServletRequest origin = newOriginMock();
        ServletRequest sut = new ServletRequest(origin);
        Object value = new Object();

        sut.setAttribute("attrKey", value);

        verify(origin).setAttribute("attrKey", value);
        // smart-null 잠금 — origin 어댑터는 null 아님
        assertNotNull(sut);
    }
}
