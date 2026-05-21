package adapter.javax.servlet61.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet61.http.HttpServletResponse} 단위 위임 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side 어댑터 wrap → 동일 메서드 호출 시
 * 원본의 동일 메서드가 호출되는지 검증한다.
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 * 어댑터 SUT 자체는 절대 mock 하지 않는다.
 *
 * @req FR-TEST-001
 */
class HttpServletResponseDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: deprecated setStatus(int, String) — Servlet 6.1 에서 reason
    // 문자열이 제거되었으므로 어댑터는 reason 을 drop 하고 원본의 setStatus(int) 만
    // 호출한다 (HttpServletResponse.java:146-149 placeholder pending PH-004).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: deprecated setStatus(int,String) 은 reason 을 drop 하고 원본 setStatus(int) 만 위임 호출한다")
    void shouldDelegateDeprecatedSetStatusWithReason_AC1() {
        jakarta.servlet.http.HttpServletResponse origin =
            mock(jakarta.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        HttpServletResponse adapter = new HttpServletResponse(origin);

        adapter.setStatus(418, "I'm a teapot");

        // reason 인자는 drop, int 만 위임.
        verify(origin).setStatus(418);
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2: deprecated encodeRedirectUrl — Servlet 6.1 에서 메서드가
    // 제거되었으므로 어댑터는 encodeRedirectURL(대문자 URL) 로 fallback 위임한다
    // (HttpServletResponse.java:100-103 placeholder pending PH-004).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: deprecated encodeRedirectUrl 은 원본 encodeRedirectURL 로 fallback 위임한다")
    void shouldDelegateEncodeRedirectUrl_AC2() {
        jakarta.servlet.http.HttpServletResponse origin =
            mock(jakarta.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.encodeRedirectURL("/x")).thenReturn("/x;jsessionid=abc");

        HttpServletResponse adapter = new HttpServletResponse(origin);

        String result = adapter.encodeRedirectUrl("/x");

        verify(origin).encodeRedirectURL("/x");
        assertEquals("/x;jsessionid=abc", result, "원본 encodeRedirectURL 반환값 그대로 반환");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #3: addCookie — 어댑터가 받은 javax cookie 를 jakarta cookie
    // 어댑터로 wrap 하여 원본 addCookie 에 전달한다 (HttpServletResponse.java:79-81).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletResponse.addCookie 는 javax cookie 를 jakarta cookie 어댑터로 wrap 하여 위임한다")
    void shouldWrapAddCookieIntoJakartaCookie_AC3() {
        jakarta.servlet.http.HttpServletResponse origin =
            mock(jakarta.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        HttpServletResponse adapter = new HttpServletResponse(origin);

        javax.servlet.http.Cookie javaxCookie = new javax.servlet.http.Cookie("k", "v");
        adapter.addCookie(javaxCookie);

        ArgumentCaptor<jakarta.servlet.http.Cookie> captor =
            ArgumentCaptor.forClass(jakarta.servlet.http.Cookie.class);
        verify(origin).addCookie(captor.capture());
        jakarta.servlet.http.Cookie captured = captor.getValue();
        assertEquals("k", captured.getName(), "wrap 된 jakarta cookie 의 name 은 원본 javax cookie 와 동일");
        assertEquals("v", captured.getValue(), "wrap 된 jakarta cookie 의 value 는 원본 javax cookie 와 동일");
    }
}
