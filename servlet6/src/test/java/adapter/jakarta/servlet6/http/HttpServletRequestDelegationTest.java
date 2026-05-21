package adapter.jakarta.servlet6.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet6.http.HttpServletRequest} 단위 위임 테스트.
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 javax 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 * 어댑터 SUT 자체는 절대 mock 하지 않는다.
 *
 * Verify 순서: 원본 호출 검증 → 반환값 assertEquals.
 *
 * @req FR-TEST-001
 */
class HttpServletRequestDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: getCookies — null 미발생 + 변환 결과 길이/타입 확인
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletRequest.getCookies 가 원본 getCookies 를 위임 호출하고 변환된 배열을 반환한다")
    void shouldDelegateGetCookies_AC1() {
        javax.servlet.http.HttpServletRequest origin =
            mock(javax.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        javax.servlet.http.Cookie originCookie = new javax.servlet.http.Cookie("k", "v");
        when(origin.getCookies()).thenReturn(new javax.servlet.http.Cookie[]{originCookie});

        HttpServletRequest adapter = new HttpServletRequest(origin);

        jakarta.servlet.http.Cookie[] result = adapter.getCookies();

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
        javax.servlet.http.HttpServletRequest origin =
            mock(javax.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        when(origin.getHeader("X-Test")).thenReturn("hello");

        HttpServletRequest adapter = new HttpServletRequest(origin);

        String result = adapter.getHeader("X-Test");

        verify(origin).getHeader("X-Test");
        assertEquals("hello", result, "원본 반환값을 그대로 반환");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #3: getRequestId — wrapper 인스턴스 단위 lazy 캐시.
    // (FR-CONV-001) 동일 wrapper 에서 두 번 호출 시 동일 UUID 반환.
    // 원본 setAttribute 는 첫 호출 시 1회만 호출되어야 한다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpServletRequest.getRequestId 는 wrapper 인스턴스 단위로 lazy 캐시한다")
    void shouldCacheRequestIdLazily_AC3() {
        javax.servlet.http.HttpServletRequest origin =
            mock(javax.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        // 첫 호출 시 attribute 가 없고 setAttribute 가 1회 호출되도록 한다.
        when(origin.getAttribute("com.snoworca.adapter.requestId")).thenReturn(null);

        HttpServletRequest adapter = new HttpServletRequest(origin);

        String first = adapter.getRequestId();
        String second = adapter.getRequestId();

        // 첫 호출 시 attribute 조회 + setAttribute 호출 발생.
        verify(origin).getAttribute("com.snoworca.adapter.requestId");
        verify(origin).setAttribute(org.mockito.ArgumentMatchers.eq("com.snoworca.adapter.requestId"),
                                    org.mockito.ArgumentMatchers.anyString());

        assertNotNull(first, "request id 가 생성되어 반환된다");
        assertSame(first, second, "두 번째 호출은 wrapper 인스턴스 캐시를 사용한다 (동일 객체)");
    }
}
