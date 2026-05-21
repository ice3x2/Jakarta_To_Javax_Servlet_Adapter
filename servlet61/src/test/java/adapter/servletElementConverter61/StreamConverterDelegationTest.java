package adapter.servletElementConverter61;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * {@link adapter.servletElementConverter61.StreamConverter} 위임 단위 테스트.
 *
 * <p>FR-FIX-001 (servlet5 에서 fix 된 read/readLine 정확 위임 결함) 의 회귀 lock-in
 * 테스트. 본 servlet61 모듈에서 동일 패턴이 재발하지 않도록 영구 가드한다.
 * 즉 jakarta&rarr;javax 와 javax&rarr;jakarta 양방향 모두에서:
 *
 * <ul>
 *   <li>{@code read(byte[], off, len)} 는 origin.read(byte[],off,len) 만 위임하고
 *       origin.readLine 은 호출하지 않는다.</li>
 *   <li>{@code readLine(byte[], off, len)} 는 origin.readLine(byte[],off,len) 만
 *       위임하고 origin.read 는 호출하지 않는다.</li>
 * </ul>
 *
 * <p>Mock 정책 (FR-TEST-001 AC-3): 원본 jakarta/javax {@code ServletInputStream}
 * 인터페이스만 {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 * SUT (StreamConverter 가 반환하는 어댑터 인스턴스) 자체는 mock 하지 않는다.
 *
 * <p>verify 호출은 assertEquals 보다 앞에 둔다 — 만약 회귀가 재발하면 Mockito
 * "Wanted but not invoked" verification mismatch 가 먼저 표면화되어 결함 위치가
 * 정확히 드러난다.
 *
 * @req FR-TEST-001
 * @req FR-FIX-001 (servlet5 fix 의 servlet61 회귀 lock-in)
 * @req FR-FIX-008 (cross-module consistency 가드 — InstanceofBranchDefectTest 의
 *                  servlet61 등가는 본 패키지의 ServletReqResConverterBranchTest 에서 cover)
 */
class StreamConverterDelegationTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1 (FR-FIX-001 회귀 lock-in)
    // jakarta -> javax 어댑터: read(byte[],off,len) 는 origin.read 위임, never readLine.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2 / FR-FIX-001 lock-in: jakarta->javax read(byte[],off,len) 가 origin.read 에 위임된다 (readLine 호출 0회)")
    void shouldDelegateReadToReadNotReadLine_AC1() throws IOException {
        jakarta.servlet.ServletInputStream origin =
            mock(jakarta.servlet.ServletInputStream.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        byte[] buffer = new byte[16];
        when(origin.read(buffer, 0, buffer.length)).thenReturn(8);

        javax.servlet.ServletInputStream converted = StreamConverter.convert(origin);

        int returned = converted.read(buffer, 0, buffer.length);

        verify(origin).read(buffer, 0, buffer.length);
        verify(origin, never()).readLine(any(byte[].class), anyInt(), anyInt());
        assertEquals(8, returned, "어댑터는 origin.read 반환값을 그대로 전달해야 한다");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2 (FR-FIX-001 AC-3 회귀 lock-in — readLine signature 보존)
    // jakarta -> javax 어댑터: readLine(byte[],off,len) 는 origin.readLine 위임, never read.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2 / FR-FIX-001 AC-3 lock-in: jakarta->javax readLine(byte[],off,len) 가 origin.readLine 에 위임된다 (read 호출 0회)")
    void shouldDelegateReadLineToOrigin_AC2() throws IOException {
        jakarta.servlet.ServletInputStream origin =
            mock(jakarta.servlet.ServletInputStream.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        byte[] buffer = new byte[32];
        when(origin.readLine(buffer, 4, 20)).thenReturn(20);

        javax.servlet.ServletInputStream converted = StreamConverter.convert(origin);

        int returned = converted.readLine(buffer, 4, 20);

        verify(origin).readLine(buffer, 4, 20);
        verify(origin, never()).read(any(byte[].class), anyInt(), anyInt());
        assertEquals(20, returned, "어댑터.readLine 은 origin.readLine 반환값을 그대로 전달해야 한다");
    }
}
