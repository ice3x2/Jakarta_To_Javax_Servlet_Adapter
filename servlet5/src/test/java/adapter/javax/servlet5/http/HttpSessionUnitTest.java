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
 * G6 (javax-side mirror) — {@link adapter.javax.servlet5.http.HttpSession} 위임 단위
 * 테스트 (T-PH005-07).
 *
 * <p>SUT 의 생성자는 package-private 이므로 본 테스트도 동일 패키지에 배치한다.
 * jakarta mock origin → javax-side 어댑터 wrap. RETURNS_SMART_NULLS 적용 (FR-TEST-001
 * AC-3).
 *
 * @req FR-TEST-001
 */
class HttpSessionUnitTest {

    /**
     * @req FR-TEST-001
     */
    private static jakarta.servlet.http.HttpSession newOriginMock() {
        return mock(jakarta.servlet.http.HttpSession.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax-side HttpSession.getId 위임 (mirror)")
    void shouldDelegateMirrorGetId_AC2_76_10() {
        jakarta.servlet.http.HttpSession origin = newOriginMock();
        when(origin.getId()).thenReturn("MIRROR-SID");
        HttpSession sut = new HttpSession(origin);

        String result = sut.getId();

        verify(origin).getId();
        assertEquals("MIRROR-SID", result);
    }
}
