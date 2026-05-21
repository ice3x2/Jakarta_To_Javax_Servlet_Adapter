package adapter.javax.servlet5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G6 (javax-side mirror) — {@link adapter.javax.servlet5.ServletRequest} 위임 단위
 * 테스트 (T-PH005-07).
 *
 * <p>jakarta-side ({@link adapter.jakarta.servlet5.ServletRequestUnitTest}) 의 미러로,
 * jakarta mock 을 origin 으로 하고 javax-side 어댑터를 SUT 로 wrap 한다.
 * RETURNS_SMART_NULLS 적용 (FR-TEST-001 AC-3).
 *
 * @req FR-TEST-001
 */
class ServletRequestUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static jakarta.servlet.ServletRequest newOriginMock() {
        return mock(jakarta.servlet.ServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side ServletRequest.getContentType 위임 (mirror)")
    void shouldDelegateMirrorGetContentType_AC2_76_01() {
        jakarta.servlet.ServletRequest origin = newOriginMock();
        when(origin.getContentType()).thenReturn("text/plain");
        ServletRequest sut = new ServletRequest(origin);

        String result = sut.getContentType();

        verify(origin).getContentType();
        assertEquals("text/plain", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side ServletRequest.getServerPort 위임 (mirror)")
    void shouldDelegateMirrorGetServerPort_AC2_76_02() {
        jakarta.servlet.ServletRequest origin = newOriginMock();
        when(origin.getServerPort()).thenReturn(9090);
        ServletRequest sut = new ServletRequest(origin);

        int result = sut.getServerPort();

        verify(origin).getServerPort();
        assertEquals(9090, result);
    }
}
