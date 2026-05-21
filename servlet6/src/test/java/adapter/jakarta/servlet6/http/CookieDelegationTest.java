package adapter.jakarta.servlet6.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet6.http.Cookie} 위임 단위 테스트.
 *
 * 본 클래스는 어댑터 Cookie 의 슈퍼클래스 ({@code jakarta.servlet.http.Cookie}) 메서드
 * 중에서 어댑터가 명시적으로 {@code @Override} 한 위임 메서드 (setMaxAge, setSecure 등)
 * 가 원본 javax cookie 에 위임되는지 검증한다.
 *
 * jakarta 6.0 슈퍼클래스 신규 attribute API (setAttribute(String,String) /
 * getAttribute(String)) 는 어댑터가 override 하지 않으므로 본 테스트 범위 외 —
 * FR-CONV-013 의 fallback 검증은 javax-side (T-PH005-02) 로 이관됨.
 *
 * @req FR-TEST-001
 */
class CookieDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setMaxAge(int) — 원본 javax cookie 의 setMaxAge 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: Cookie.setMaxAge 는 원본 javax cookie 의 setMaxAge 를 위임 호출한다")
    void shouldDelegateSetMaxAge_AC1() {
        javax.servlet.http.Cookie origin =
            mock(javax.servlet.http.Cookie.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getMaxAge()).thenReturn(3600);

        Cookie adapter = new Cookie(origin);

        adapter.setMaxAge(3600);

        verify(origin).setMaxAge(3600);
        // getMaxAge 도 같은 위임 패턴인지 보조 확인.
        assertEquals(3600, adapter.getMaxAge(), "getMaxAge 는 원본 cookie 의 값을 반환");
        verify(origin).getMaxAge();
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2: setSecure(boolean) — 원본 javax cookie 의 setSecure 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: Cookie.setSecure 는 원본 javax cookie 의 setSecure 를 위임 호출한다")
    void shouldDelegateSetSecure_AC2() {
        javax.servlet.http.Cookie origin =
            mock(javax.servlet.http.Cookie.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getSecure()).thenReturn(true);

        Cookie adapter = new Cookie(origin);

        adapter.setSecure(true);

        verify(origin).setSecure(true);
        // 위임 후 getSecure 도 원본 값을 반환하는지 보조 검증.
        assertEquals(true, adapter.getSecure(), "getSecure 는 원본 cookie 의 값을 반환");
        verify(origin).getSecure();
    }
}
