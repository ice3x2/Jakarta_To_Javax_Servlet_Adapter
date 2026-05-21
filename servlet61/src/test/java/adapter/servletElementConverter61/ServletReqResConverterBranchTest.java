package adapter.servletElementConverter61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * {@link adapter.servletElementConverter61.ServletReqResConverter} 의 4 instanceof
 * 분기 라우팅 단위 테스트.
 *
 * <p>FR-FIX-008 (servlet5 에서 fix 된 cross-namespace instanceof 결함) 의 servlet61
 * 회귀 lock-in 테스트. 즉 jakarta 입력 분기는 jakarta-namespace 의 HTTP 타입을
 * instanceof 로 체크하고, javax 입력 분기는 javax-namespace 의 HTTP 타입을 체크함을
 * 보장한다. 잘못된 cross-namespace instanceof 가 재도입되면 HTTP mock 이
 * 비-HTTP wrapper 로 떨어져 HTTP-specific 메서드가 사라지는 BM04-급 결함이
 * 발생하므로, 본 테스트는 두 방향의 HTTP 분기 도달성을 동시에 가드한다.
 *
 * <p>검증 전략: jakarta HTTP mock 을 jakarta-input convert 에 통과시키면 javax HTTP
 * adapter 타입의 결과를 반환해야 한다. 마찬가지로 javax HTTP mock 을 javax-input
 * convert 에 통과시키면 jakarta HTTP adapter 타입의 결과를 반환해야 한다.
 * 만약 instanceof 가 cross-namespace 로 잘못 작성되면 결과는 항상 ServletResponse /
 * ServletRequest 일반 wrapper 가 되어 HTTP adapter 의 instanceof 검증이 실패한다.
 *
 * <p>Mock 정책 (FR-TEST-001 AC-3): jakarta / javax HTTP 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * @req FR-TEST-001
 * @req FR-FIX-008 (servlet5 fix 의 servlet61 회귀 lock-in)
 */
class ServletReqResConverterBranchTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1 (FR-FIX-008 회귀 lock-in)
    // jakarta HTTP request 입력 → javax HTTP request adapter (HttpServletRequest)
    // 로 라우팅된다. 만약 instanceof 가 cross-namespace 로 변경되면 일반
    // javax.servlet.ServletRequest 일반 wrapper 로 떨어져 본 assert 가 실패한다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2 / FR-FIX-008 lock-in: jakarta HTTP request 입력은 javax HTTP request adapter 로 라우팅된다")
    void shouldRouteJakartaInputToJakartaHttpBranch_AC1() {
        jakarta.servlet.http.HttpServletRequest jakartaHttpRequest =
            mock(jakarta.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        jakarta.servlet.http.HttpServletResponse jakartaHttpResponse =
            mock(jakarta.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        javax.servlet.ServletRequest convertedRequest =
            ServletReqResConverter.convert((jakarta.servlet.ServletRequest) jakartaHttpRequest);
        javax.servlet.ServletResponse convertedResponse =
            ServletReqResConverter.convert((jakarta.servlet.ServletResponse) jakartaHttpResponse);

        assertNotNull(convertedRequest, "변환 결과는 null 이 아니다");
        assertNotNull(convertedResponse, "변환 결과는 null 이 아니다");
        assertTrue(convertedRequest instanceof javax.servlet.http.HttpServletRequest,
            "jakarta HTTP request 입력은 javax HTTP request adapter 로 라우팅되어야 한다 "
            + "(actual=" + convertedRequest.getClass().getName() + ")");
        assertTrue(convertedResponse instanceof javax.servlet.http.HttpServletResponse,
            "jakarta HTTP response 입력은 javax HTTP response adapter 로 라우팅되어야 한다 "
            + "(actual=" + convertedResponse.getClass().getName() + ")");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2 (FR-FIX-008 mirror direction 회귀 lock-in)
    // javax HTTP request 입력 → jakarta HTTP request adapter (HttpServletRequest)
    // 로 라우팅된다. 양방향 대칭성 보장.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2 / FR-FIX-008 lock-in: javax HTTP request 입력은 jakarta HTTP request adapter 로 라우팅된다")
    void shouldRouteJavaxInputToJavaxHttpBranch_AC2() {
        javax.servlet.http.HttpServletRequest javaxHttpRequest =
            mock(javax.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.http.HttpServletResponse javaxHttpResponse =
            mock(javax.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.ServletRequest convertedRequest =
            ServletReqResConverter.convert((javax.servlet.ServletRequest) javaxHttpRequest);
        jakarta.servlet.ServletResponse convertedResponse =
            ServletReqResConverter.convert((javax.servlet.ServletResponse) javaxHttpResponse);

        assertNotNull(convertedRequest, "변환 결과는 null 이 아니다");
        assertNotNull(convertedResponse, "변환 결과는 null 이 아니다");
        assertTrue(convertedRequest instanceof jakarta.servlet.http.HttpServletRequest,
            "javax HTTP request 입력은 jakarta HTTP request adapter 로 라우팅되어야 한다 "
            + "(actual=" + convertedRequest.getClass().getName() + ")");
        assertTrue(convertedResponse instanceof jakarta.servlet.http.HttpServletResponse,
            "javax HTTP response 입력은 jakarta HTTP response adapter 로 라우팅되어야 한다 "
            + "(actual=" + convertedResponse.getClass().getName() + ")");
    }
}
