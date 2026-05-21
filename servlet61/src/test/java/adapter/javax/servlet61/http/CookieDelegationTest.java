package adapter.javax.servlet61.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet61.http.Cookie} 위임 단위 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side Cookie 어댑터 wrap → 위임 메서드 호출 시
 * 원본의 동일 메서드가 호출되는지 검증한다. 어댑터는 {@code javax.servlet.http.Cookie}
 * 를 extends 하므로 super("ignored","ignored") 더미 인자로 초기화하고 모든 상태는
 * 내부 보유 원본 jakarta cookie 에서 위임한다 (Cookie.java:23-26).
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * @req FR-TEST-001
 */
class CookieDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setMaxAge(int) — 원본 jakarta cookie 의 setMaxAge 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: Cookie.setMaxAge 는 원본 jakarta cookie 의 setMaxAge 를 위임 호출한다")
    void shouldDelegateSetMaxAge_AC1() {
        jakarta.servlet.http.Cookie origin =
            mock(jakarta.servlet.http.Cookie.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getMaxAge()).thenReturn(3600);

        Cookie adapter = new Cookie(origin);

        adapter.setMaxAge(3600);

        verify(origin).setMaxAge(3600);
        // getMaxAge 도 같은 위임 패턴인지 보조 확인.
        assertEquals(3600, adapter.getMaxAge(), "getMaxAge 는 원본 cookie 의 값을 반환");
        verify(origin).getMaxAge();
    }
}
