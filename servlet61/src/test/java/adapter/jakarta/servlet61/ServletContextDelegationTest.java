package adapter.jakarta.servlet61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet61.ServletContext} 단위 위임 테스트.
 *
 * @req FR-TEST-001
 */
class ServletContextDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setResponseCharacterEncoding — 원본 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletContext.setResponseCharacterEncoding 은 원본의 동일 메서드를 위임 호출한다")
    void shouldRouteSetResponseCharacterEncodingToOrigin_AC1() {
        javax.servlet.ServletContext origin =
            mock(javax.servlet.ServletContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        ServletContext adapter = new ServletContext(origin);

        adapter.setResponseCharacterEncoding("UTF-8");

        verify(origin).setResponseCharacterEncoding("UTF-8");
    }
}
