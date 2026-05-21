package adapter.javax.servlet5.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G6 (javax-side mirror) — {@link adapter.javax.servlet5.http.HttpServletRequest}
 * 위임 단위 테스트 (T-PH005-07). jakarta mock origin → javax-side 어댑터 wrap.
 * RETURNS_SMART_NULLS 적용 (FR-TEST-001 AC-3).
 *
 * @req FR-TEST-001
 */
class HttpServletRequestUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static jakarta.servlet.http.HttpServletRequest newOriginMock() {
        return mock(jakarta.servlet.http.HttpServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side HttpServletRequest.getMethod 위임 (mirror)")
    void shouldDelegateMirrorGetMethod_AC2_76_06() {
        jakarta.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getMethod()).thenReturn("PUT");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getMethod();

        verify(origin).getMethod();
        assertEquals("PUT", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side HttpServletRequest.getRequestURI 위임 (mirror)")
    void shouldDelegateMirrorGetRequestURI_AC2_76_07() {
        jakarta.servlet.http.HttpServletRequest origin = newOriginMock();
        when(origin.getRequestURI()).thenReturn("/v1/mirror");
        HttpServletRequest sut = new HttpServletRequest(origin);

        String result = sut.getRequestURI();

        verify(origin).getRequestURI();
        assertEquals("/v1/mirror", result);
    }
}
