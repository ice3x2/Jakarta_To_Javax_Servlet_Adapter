package adapter.jakarta.servlet5.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G3 — jakarta-side {@link adapter.jakarta.servlet5.http.HttpServletResponse}
 * HTTP-specific 위임 단위 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API ({@code javax.servlet.http.HttpServletResponse}) 만 mock 한다.
 * SUT 어댑터는 mock 하지 않는다. mock 생성에는 RETURNS_SMART_NULLS 적용 (FR-TEST-001
 * AC-3 잠금). {@code verify(origin)} 호출은 {@code assertEquals} 보다 앞에 배치.
 *
 * <p>위임 메서드 cover:
 * {@code addCookie} (jakarta→javax Cookie 변환), {@code sendError(int,String)},
 * {@code sendError(int)}, {@code sendRedirect}, {@code setStatus(int)},
 * {@code encodeURL}, {@code containsHeader}.
 *
 * @req FR-TEST-001
 */
class HttpServletResponseUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static javax.servlet.http.HttpServletResponse newOriginMock() {
        return mock(javax.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: addCookie — jakarta Cookie 가 javax Cookie 어댑터로 변환 후 위임")
    void shouldConvertAndDelegateAddCookie_AC2_73_01() {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);
        jakarta.servlet.http.Cookie jakartaCookie =
                new jakarta.servlet.http.Cookie("sid", "abc123");

        sut.addCookie(jakartaCookie);

        ArgumentCaptor<javax.servlet.http.Cookie> cap =
                ArgumentCaptor.forClass(javax.servlet.http.Cookie.class);
        verify(origin).addCookie(cap.capture());
        // 변환된 javax Cookie 어댑터는 name/value 가 보존된다
        // (adapter.javax.servlet5.http.Cookie ctor 은 super("ignored","ignored") 로
        //  설정 후 unwrap 으로 origin 을 반환. name/value getter 는 unwrap 위임)
        javax.servlet.http.Cookie passed = cap.getValue();
        assertNotNull(passed);
        assertEquals("sid", passed.getName());
        assertEquals("abc123", passed.getValue());
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: sendError(int,String) 2-arity 위임")
    void shouldDelegateSendError2Arity_AC2_73_02() throws Exception {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);

        sut.sendError(503, "Service Unavailable");

        verify(origin).sendError(503, "Service Unavailable");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: sendError(int) 1-arity 위임")
    void shouldDelegateSendError1Arity_AC2_73_03() throws Exception {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);

        sut.sendError(404);

        verify(origin).sendError(404);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: sendRedirect 위임 (location 인자 통과)")
    void shouldDelegateSendRedirect_AC2_73_04() throws Exception {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);

        sut.sendRedirect("/login");

        verify(origin).sendRedirect("/login");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: setStatus(int) 위임")
    void shouldDelegateSetStatusInt_AC2_73_05() {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);

        sut.setStatus(201);

        verify(origin).setStatus(201);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: encodeURL 위임")
    void shouldDelegateEncodeURL_AC2_73_06() {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        when(origin.encodeURL("/raw")).thenReturn("/raw;jsessionid=X");
        HttpServletResponse sut = new HttpServletResponse(origin);

        String result = sut.encodeURL("/raw");

        verify(origin).encodeURL("/raw");
        assertEquals("/raw;jsessionid=X", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: containsHeader 위임")
    void shouldDelegateContainsHeader_AC2_73_07() {
        javax.servlet.http.HttpServletResponse origin = newOriginMock();
        when(origin.containsHeader("ETag")).thenReturn(true);
        HttpServletResponse sut = new HttpServletResponse(origin);

        boolean result = sut.containsHeader("ETag");

        verify(origin).containsHeader("ETag");
        assertEquals(true, result);
    }
}
