package adapter.jakarta.servlet61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet61.ServletRequest} 단위 위임 테스트.
 *
 * @req FR-TEST-001
 */
class ServletRequestDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: getContentLengthLong — 원본의 long 반환 메서드 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletRequest.getContentLengthLong 은 원본의 동일 메서드를 위임 호출한다")
    void shouldDelegateGetContentLengthLong_AC1() {
        javax.servlet.ServletRequest origin =
            mock(javax.servlet.ServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getContentLengthLong()).thenReturn(9_876_543_210L);

        ServletRequest adapter = new ServletRequest(origin);

        long result = adapter.getContentLengthLong();

        verify(origin).getContentLengthLong();
        assertEquals(9_876_543_210L, result, "원본 long 반환값 그대로 반환 (int 캐스팅 손실 없음)");
    }
}
