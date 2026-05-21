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
 * G6 (javax-side mirror) — {@link adapter.javax.servlet5.ServletContext} 위임 단위
 * 테스트 (T-PH005-07). jakarta mock origin → javax-side 어댑터 wrap.
 * RETURNS_SMART_NULLS 적용 (FR-TEST-001 AC-3).
 *
 * @req FR-TEST-001
 */
class ServletContextUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static jakarta.servlet.ServletContext newOriginMock() {
        return mock(jakarta.servlet.ServletContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side ServletContext.getContextPath 위임 (mirror)")
    void shouldDelegateMirrorGetContextPath_AC2_76_05() {
        jakarta.servlet.ServletContext origin = newOriginMock();
        when(origin.getContextPath()).thenReturn("/mirror");
        ServletContext sut = new ServletContext(origin);

        String result = sut.getContextPath();

        verify(origin).getContextPath();
        assertEquals("/mirror", result);
    }
}
