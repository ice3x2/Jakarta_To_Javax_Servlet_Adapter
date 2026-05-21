package adapter.javax.servlet61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet61.ServletRequest} 단위 위임 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side 어댑터 wrap → javax 4.x API 호출 시
 * 원본 jakarta 6.1 의 동일 메서드가 호출되는지 검증한다.
 *
 * 본 테스트는 javax-side {@code ServletRequest} 가 request character encoding 을
 * 원본 jakarta {@code setCharacterEncoding} 으로 위임하는지 검증한다 (ServletRequest.java:60-62).
 * jakarta {@code ServletContext.setRequestCharacterEncoding} 은 ServletContext 어댑터의
 * 책임이며 별도 테스트(ServletContextDelegationTest)에서 cover 한다.
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * @req FR-TEST-001
 */
class ServletRequestDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setCharacterEncoding — 인자 그대로 원본 jakarta 의 동일
    // 메서드에 라우팅된다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletRequest.setCharacterEncoding 은 원본 jakarta 의 동일 메서드로 인자를 위임한다")
    void shouldRouteSetRequestCharacterEncodingToOrigin_AC1() throws UnsupportedEncodingException {
        jakarta.servlet.ServletRequest origin =
            mock(jakarta.servlet.ServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        ServletRequest adapter = new ServletRequest(origin);

        adapter.setCharacterEncoding("UTF-8");

        verify(origin).setCharacterEncoding("UTF-8");
    }
}
