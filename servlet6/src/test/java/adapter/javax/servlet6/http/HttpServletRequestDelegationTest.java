package adapter.javax.servlet6.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet6.http.HttpServletRequest} 단위 위임 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side 어댑터 wrap → 동일 메서드 호출 시
 * 원본의 동일 메서드가 호출되는지 검증한다.
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 * 어댑터 SUT 자체는 절대 mock 하지 않는다.
 *
 * Verify 순서: 원본 호출 검증 → 반환값 assertEquals.
 *
 * @req FR-TEST-001
 */
class HttpServletRequestDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: getCookies — null 미발생 + jakarta → javax wrap 결과 확인
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletRequest.getCookies 는 원본 getCookies 를 위임 호출하고 변환된 배열을 반환한다")
    void shouldDelegateGetCookies_AC1() {
        jakarta.servlet.http.HttpServletRequest origin =
            mock(jakarta.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.http.Cookie originCookie = new jakarta.servlet.http.Cookie("k", "v");
        when(origin.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{originCookie});

        HttpServletRequest adapter = new HttpServletRequest(origin);

        javax.servlet.http.Cookie[] result = adapter.getCookies();

        verify(origin).getCookies();
        assertNotNull(result, "변환된 cookie 배열은 null 이 아니다");
        assertEquals(1, result.length, "1건 원본을 변환해 1건 반환");
        assertEquals("k", result[0].getName(), "변환된 cookie 의 이름은 원본을 위임한다");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2: getHeader — 단순 위임
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletRequest.getHeader 는 원본 getHeader 에 인자 그대로 위임한다")
    void shouldDelegateGetHeader_AC2() {
        jakarta.servlet.http.HttpServletRequest origin =
            mock(jakarta.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        when(origin.getHeader("X-Test")).thenReturn("hello");

        HttpServletRequest adapter = new HttpServletRequest(origin);

        String result = adapter.getHeader("X-Test");

        verify(origin).getHeader("X-Test");
        assertEquals("hello", result, "원본 반환값을 그대로 반환");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #3: getMethod — 단순 위임
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletRequest.getMethod 는 원본 getMethod 를 그대로 위임한다")
    void shouldDelegateGetMethod_AC3() {
        jakarta.servlet.http.HttpServletRequest origin =
            mock(jakarta.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        when(origin.getMethod()).thenReturn("POST");

        HttpServletRequest adapter = new HttpServletRequest(origin);

        String result = adapter.getMethod();

        verify(origin).getMethod();
        assertEquals("POST", result, "원본 반환값을 그대로 반환");
    }
}
