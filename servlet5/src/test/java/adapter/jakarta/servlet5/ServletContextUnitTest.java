package adapter.jakarta.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G5 — jakarta-side {@link adapter.jakarta.servlet5.ServletContext} 위임 단위
 * 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API ({@code javax.servlet.ServletContext}) 만 mock 한다. SUT
 * 어댑터는 mock 하지 않는다. RETURNS_SMART_NULLS 적용 (FR-TEST-001 AC-3).
 *
 * <p>위임 메서드 cover (mission-critical 10 개): {@code getContextPath},
 * {@code getMajorVersion}, {@code getMinorVersion}, {@code getMimeType},
 * {@code getInitParameter}, {@code setInitParameter} (boolean 반환),
 * {@code getAttribute}, {@code setAttribute}, {@code log(String)},
 * {@code getRealPath}.
 *
 * @req FR-TEST-001
 */
class ServletContextUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static javax.servlet.ServletContext newOriginMock() {
        return mock(javax.servlet.ServletContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getContextPath 위임")
    void shouldDelegateGetContextPath_AC2_75_01() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.getContextPath()).thenReturn("/myapp");
        ServletContext sut = new ServletContext(origin);

        String result = sut.getContextPath();

        verify(origin).getContextPath();
        assertEquals("/myapp", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getMajorVersion 위임")
    void shouldDelegateGetMajorVersion_AC2_75_02() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.getMajorVersion()).thenReturn(4);
        ServletContext sut = new ServletContext(origin);

        int result = sut.getMajorVersion();

        verify(origin).getMajorVersion();
        assertEquals(4, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getMinorVersion 위임")
    void shouldDelegateGetMinorVersion_AC2_75_03() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.getMinorVersion()).thenReturn(0);
        ServletContext sut = new ServletContext(origin);

        int result = sut.getMinorVersion();

        verify(origin).getMinorVersion();
        assertEquals(0, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getMimeType 위임")
    void shouldDelegateGetMimeType_AC2_75_04() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.getMimeType("a.json")).thenReturn("application/json");
        ServletContext sut = new ServletContext(origin);

        String result = sut.getMimeType("a.json");

        verify(origin).getMimeType("a.json");
        assertEquals("application/json", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getInitParameter 위임")
    void shouldDelegateGetInitParameter_AC2_75_05() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.getInitParameter("k")).thenReturn("v");
        ServletContext sut = new ServletContext(origin);

        String result = sut.getInitParameter("k");

        verify(origin).getInitParameter("k");
        assertEquals("v", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: setInitParameter 위임 + boolean 반환 통과")
    void shouldDelegateSetInitParameter_AC2_75_06() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.setInitParameter("k", "v")).thenReturn(true);
        ServletContext sut = new ServletContext(origin);

        boolean result = sut.setInitParameter("k", "v");

        verify(origin).setInitParameter("k", "v");
        assertTrue(result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getAttribute 위임 (Object 반환)")
    void shouldDelegateGetAttribute_AC2_75_07() {
        javax.servlet.ServletContext origin = newOriginMock();
        Object value = new Object();
        when(origin.getAttribute("attrK")).thenReturn(value);
        ServletContext sut = new ServletContext(origin);

        Object result = sut.getAttribute("attrK");

        verify(origin).getAttribute("attrK");
        assertEquals(value, result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: setAttribute 위임 (name + value 인자 통과)")
    void shouldDelegateSetAttribute_AC2_75_08() {
        javax.servlet.ServletContext origin = newOriginMock();
        ServletContext sut = new ServletContext(origin);
        Object value = new Object();

        sut.setAttribute("attr", value);

        verify(origin).setAttribute("attr", value);
        assertNotNull(sut);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: log(String) 위임")
    void shouldDelegateLogString_AC2_75_09() {
        javax.servlet.ServletContext origin = newOriginMock();
        ServletContext sut = new ServletContext(origin);

        sut.log("info: started");

        verify(origin).log("info: started");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: getRealPath 위임")
    void shouldDelegateGetRealPath_AC2_75_10() {
        javax.servlet.ServletContext origin = newOriginMock();
        when(origin.getRealPath("/index.html")).thenReturn("/var/www/index.html");
        ServletContext sut = new ServletContext(origin);

        String result = sut.getRealPath("/index.html");

        verify(origin).getRealPath("/index.html");
        assertEquals("/var/www/index.html", result);
    }
}
