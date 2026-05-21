package adapter.jakarta.servlet6.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet6.http.HttpServletResponse} 단위 위임 테스트.
 *
 * @req FR-TEST-001
 */
class HttpServletResponseDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setStatus(int) — 원본 setStatus 위임
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletResponse.setStatus 가 원본 setStatus 를 위임 호출한다")
    void shouldDelegateSetStatus_AC1() {
        javax.servlet.http.HttpServletResponse origin =
            mock(javax.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        HttpServletResponse adapter = new HttpServletResponse(origin);

        adapter.setStatus(418);

        verify(origin).setStatus(418);
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2: addCookie — 어댑터가 받은 jakarta cookie 를
    // javax cookie 로 wrap 하여 원본 addCookie 에 전달한다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletResponse.addCookie 는 jakarta cookie 를 javax cookie 로 wrap 하여 위임한다")
    void shouldWrapAddCookieIntoJavaxCookie_AC2() {
        javax.servlet.http.HttpServletResponse origin =
            mock(javax.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        HttpServletResponse adapter = new HttpServletResponse(origin);

        jakarta.servlet.http.Cookie jakartaCookie = new jakarta.servlet.http.Cookie("k", "v");
        adapter.addCookie(jakartaCookie);

        ArgumentCaptor<javax.servlet.http.Cookie> captor =
            ArgumentCaptor.forClass(javax.servlet.http.Cookie.class);
        verify(origin).addCookie(captor.capture());
        javax.servlet.http.Cookie captured = captor.getValue();
        assertEquals("k", captured.getName(), "wrap 된 javax cookie 의 name 은 원본과 동일");
        assertEquals("v", captured.getValue(), "wrap 된 javax cookie 의 value 는 원본과 동일");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #3: sendRedirect(String) — 인자 전달 위임
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletResponse.sendRedirect(String) 는 원본 sendRedirect 를 그대로 위임한다")
    void shouldDelegateSendRedirectSingleArg_AC3() throws IOException {
        javax.servlet.http.HttpServletResponse origin =
            mock(javax.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        HttpServletResponse adapter = new HttpServletResponse(origin);

        adapter.sendRedirect("/redirect-target");

        verify(origin).sendRedirect("/redirect-target");
    }
}
