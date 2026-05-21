package adapter.jakarta.servlet61.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * servlet61 신규 3-arg 오버로드 {@code sendRedirect(String, int, boolean)} 의
 * edge case 단위 테스트 — {@code clearBuffer=false} 시 resetBuffer 호출 0회 lock-in.
 *
 * <p>본 클래스는 기존 {@code com.snoworca.SendRedirect61Test} (5건, FR-CONV-001/014)
 * 와 비중복인 시나리오만 cover 한다: SendRedirect61Test 는 clearBuffer=true 의 정상
 * 경로 + 미존재 override 의 reflection 검증을 다루므로, 본 테스트는 false 분기에서
 * resetBuffer 가 호출되지 않음을 명시 검증한다 (서비스 측에서 buffer 보존 의도가
 * 깨지지 않도록 lock-in).
 *
 * 어댑터 production 위치: {@code servlet61/src/main/java/adapter/jakarta/servlet61/
 * http/HttpServletResponse.java:106-118} — {@code if (clearBuffer) resetBuffer(); }.
 *
 * Mock 정책: 원본 javax 인터페이스만 RETURNS_SMART_NULLS 로 mock.
 *
 * @req FR-TEST-001
 */
class HttpServletResponseRedirect61EdgeCaseTest {

    // ----------------------------------------------------------------------
    // AC-2 edge case: sendRedirect(loc, sc, false) 는 resetBuffer 미호출.
    // setStatus + Location header + flushBuffer 는 호출한다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: sendRedirect(loc, sc, clearBuffer=false) 는 resetBuffer 를 호출하지 않는다 (servlet61 edge case)")
    void shouldNotResetBufferWhenClearBufferFalse_AC1() throws IOException {
        javax.servlet.http.HttpServletResponse origin =
            mock(javax.servlet.http.HttpServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        HttpServletResponse adapter = new HttpServletResponse(origin);

        adapter.sendRedirect("/keep-buffer", 303, false);

        // 핵심 lock-in: clearBuffer=false 분기 — resetBuffer 호출 금지.
        verify(origin, never()).resetBuffer();

        // 부수 동작 검증: setStatus + Location header + flushBuffer 는 정상 호출.
        verify(origin).setStatus(303);
        verify(origin).setHeader("Location", "/keep-buffer");
        verify(origin).flushBuffer();

        // 어댑터가 1-arg sendRedirect(loc) 로 위임하지 않음을 보조 확인 (override 동작).
        verify(origin, never()).sendRedirect("/keep-buffer");
    }
}
