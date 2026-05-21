package adapter.javax.servlet61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet61.ServletContext} 단위 위임 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side 어댑터 wrap → 동일 메서드 호출 시
 * 원본의 동일 메서드가 호출되는지 검증한다.
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * @req FR-TEST-001
 */
class ServletContextDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setRequestCharacterEncoding — 인자 그대로 원본 jakarta 의
    // 동일 메서드에 라우팅된다 (ServletContext.java:373-375).
    // FR-FIX-005 회귀 lock-in: 정확히 setRequestCharacterEncoding (not Response) 으로
    // 라우팅되어야 한다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletContext.setRequestCharacterEncoding 은 원본 jakarta 의 setRequestCharacterEncoding 으로 라우팅된다")
    void shouldRouteSetRequestCharacterEncodingToOrigin_AC1() {
        jakarta.servlet.ServletContext origin =
            mock(jakarta.servlet.ServletContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        ServletContext adapter = new ServletContext(origin);

        adapter.setRequestCharacterEncoding("UTF-8");

        verify(origin).setRequestCharacterEncoding("UTF-8");
    }
}
