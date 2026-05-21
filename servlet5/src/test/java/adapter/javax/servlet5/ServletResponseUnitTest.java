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
 * G6 (javax-side mirror) — {@link adapter.javax.servlet5.ServletResponse} 위임 단위
 * 테스트 (T-PH005-07). jakarta mock origin → javax-side 어댑터 wrap. RETURNS_SMART_NULLS
 * 적용 (FR-TEST-001 AC-3).
 *
 * @req FR-TEST-001
 */
class ServletResponseUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static jakarta.servlet.ServletResponse newOriginMock() {
        return mock(jakarta.servlet.ServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side ServletResponse.getContentType 위임 (mirror)")
    void shouldDelegateMirrorGetContentType_AC2_76_03() {
        jakarta.servlet.ServletResponse origin = newOriginMock();
        when(origin.getContentType()).thenReturn("application/xml");
        ServletResponse sut = new ServletResponse(origin);

        String result = sut.getContentType();

        verify(origin).getContentType();
        assertEquals("application/xml", result);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side ServletResponse.setBufferSize 위임 (mirror)")
    void shouldDelegateMirrorSetBufferSize_AC2_76_04() {
        jakarta.servlet.ServletResponse origin = newOriginMock();
        ServletResponse sut = new ServletResponse(origin);

        sut.setBufferSize(4096);

        verify(origin).setBufferSize(4096);
    }
}
