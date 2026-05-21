package adapter.javax.servlet61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet61.ServletRegistration} 단위 위임 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side ServletRegistration 어댑터 wrap →
 * 동일 메서드 호출 시 원본의 동일 메서드가 호출되는지 검증한다.
 *
 * ServletRegistration 은 Registration 의 하위 타입이며, {@code addMapping(String...)}
 * 는 ServletRegistration 고유 API 다 (ServletRegistration.java:14-16).
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * @req FR-TEST-001
 */
class RegistrationDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: ServletRegistration.addMapping(String...) — 원본 위임 +
    // 반환된 Set 동일성 검증.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletRegistration.addMapping 은 원본 jakarta ServletRegistration 의 addMapping 을 위임 호출한다")
    void shouldDelegateAddMapping_AC1() {
        jakarta.servlet.ServletRegistration origin =
            mock(jakarta.servlet.ServletRegistration.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        Set<String> conflicting = Collections.emptySet();
        when(origin.addMapping(eq("/a"), eq("/b"))).thenReturn(conflicting);

        ServletRegistration adapter = new ServletRegistration(origin);

        Set<String> result = adapter.addMapping("/a", "/b");

        verify(origin).addMapping("/a", "/b");
        assertSame(conflicting, result, "원본의 addMapping 반환값이 그대로 전달된다");
        assertEquals(0, result.size(), "충돌이 없는 경우 빈 Set 반환");
    }
}
