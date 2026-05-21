package adapter.jakarta.servlet61;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet61.ServletResponse} 단위 위임 테스트.
 *
 * @req FR-TEST-001
 */
class ServletResponseDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: setContentLengthLong — 원본의 long 인자 메서드 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: ServletResponse.setContentLengthLong 은 원본의 동일 메서드를 위임 호출한다 (long 인자 보존)")
    void shouldDelegateSetContentLengthLong_AC1() {
        javax.servlet.ServletResponse origin =
            mock(javax.servlet.ServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        ServletResponse adapter = new ServletResponse(origin);

        adapter.setContentLengthLong(5_000_000_000L);

        verify(origin).setContentLengthLong(5_000_000_000L);
    }
}
