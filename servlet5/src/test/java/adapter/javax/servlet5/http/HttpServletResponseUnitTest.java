package adapter.javax.servlet5.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * G6 (javax-side mirror) — {@link adapter.javax.servlet5.http.HttpServletResponse}
 * 위임 단위 테스트 (T-PH005-07). jakarta mock origin → javax-side 어댑터 wrap.
 * RETURNS_SMART_NULLS 적용 (FR-TEST-001 AC-3).
 *
 * @req FR-TEST-001
 */
class HttpServletResponseUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static jakarta.servlet.http.HttpServletResponse newOriginMock() {
        return mock(jakarta.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side HttpServletResponse.setStatus 위임 (mirror)")
    void shouldDelegateMirrorSetStatus_AC2_76_08() {
        jakarta.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);

        sut.setStatus(202);

        verify(origin).setStatus(202);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side HttpServletResponse.sendRedirect 위임 (mirror)")
    void shouldDelegateMirrorSendRedirect_AC2_76_09() throws Exception {
        jakarta.servlet.http.HttpServletResponse origin = newOriginMock();
        HttpServletResponse sut = new HttpServletResponse(origin);

        sut.sendRedirect("/mirror-destination");

        verify(origin).sendRedirect("/mirror-destination");
    }
}
