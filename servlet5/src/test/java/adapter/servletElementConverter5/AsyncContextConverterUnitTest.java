package adapter.servletElementConverter5;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G7 — {@link adapter.servletElementConverter5.AsyncContextConverter} factory +
 * listener callback 단위 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API 만 mock; 어댑터 SUT 는 mock 하지 않는다. RETURNS_SMART_NULLS
 * 적용 (FR-TEST-001 AC-3).
 *
 * <p>위임/변환 cover: {@code convert(javax.AsyncContext)} + complete/dispatch/getTimeout/setTimeout
 * 위임 + listener callback (onComplete/onError/onStartAsync/onTimeout) 4 종.
 *
 * @req FR-TEST-001
 */
class AsyncContextConverterUnitTest {

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncContext) → jakarta AsyncContext, complete 위임")
    void shouldConvertAndDelegateComplete_AC2_77_01() {
        javax.servlet.AsyncContext origin = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.AsyncContext sut = AsyncContextConverter.convert(origin);
        sut.complete();

        verify(origin).complete();
        assertNotNull(sut);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncContext) → jakarta AsyncContext, dispatch() 위임")
    void shouldConvertAndDelegateDispatch_AC2_77_02() {
        javax.servlet.AsyncContext origin = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.AsyncContext sut = AsyncContextConverter.convert(origin);
        sut.dispatch();

        verify(origin).dispatch();
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncContext) → jakarta, dispatch(String) 위임")
    void shouldDelegateDispatchPath_AC2_77_03() {
        javax.servlet.AsyncContext origin = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        jakarta.servlet.AsyncContext sut = AsyncContextConverter.convert(origin);
        sut.dispatch("/async-target");

        verify(origin).dispatch("/async-target");
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncContext) — getTimeout/setTimeout 위임 (long 통과)")
    void shouldDelegateTimeoutPair_AC2_77_04() {
        javax.servlet.AsyncContext origin = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getTimeout()).thenReturn(30000L);

        jakarta.servlet.AsyncContext sut = AsyncContextConverter.convert(origin);
        sut.setTimeout(60000L);
        long timeout = sut.getTimeout();

        verify(origin).setTimeout(60000L);
        verify(origin).getTimeout();
        assertEquals(30000L, timeout);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncListener) → jakarta AsyncListener.onComplete 위임")
    void shouldConvertAsyncListenerOnComplete_AC2_77_05() throws IOException {
        javax.servlet.AsyncListener origin = mock(javax.servlet.AsyncListener.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.AsyncContext javaxCtx = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.AsyncEvent javaxEvent =
                new javax.servlet.AsyncEvent(javaxCtx);
        // sut = jakarta AsyncListener; jakarta AsyncEvent 변환은 convert(AsyncEvent) 가 담당.
        AsyncListener sut = AsyncContextConverter.convert(origin);
        AsyncEvent jakartaEvent = AsyncContextConverter.convert(javaxEvent);

        sut.onComplete(jakartaEvent);

        ArgumentCaptor<javax.servlet.AsyncEvent> cap =
                ArgumentCaptor.forClass(javax.servlet.AsyncEvent.class);
        verify(origin).onComplete(cap.capture());
        assertNotNull(cap.getValue());
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncListener) → jakarta AsyncListener.onError 위임")
    void shouldConvertAsyncListenerOnError_AC2_77_06() throws IOException {
        javax.servlet.AsyncListener origin = mock(javax.servlet.AsyncListener.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.AsyncContext javaxCtx = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.AsyncEvent javaxEvent =
                new javax.servlet.AsyncEvent(javaxCtx, new RuntimeException("boom"));
        AsyncListener sut = AsyncContextConverter.convert(origin);
        AsyncEvent jakartaEvent = AsyncContextConverter.convert(javaxEvent);

        sut.onError(jakartaEvent);

        verify(origin).onError(org.mockito.ArgumentMatchers.any(javax.servlet.AsyncEvent.class));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncListener) → jakarta AsyncListener.onStartAsync + onTimeout 위임")
    void shouldConvertAsyncListenerOnStartAsyncAndOnTimeout_AC2_77_07() throws IOException {
        javax.servlet.AsyncListener origin = mock(javax.servlet.AsyncListener.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.AsyncContext javaxCtx = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.AsyncEvent javaxEvent =
                new javax.servlet.AsyncEvent(javaxCtx);
        AsyncListener sut = AsyncContextConverter.convert(origin);
        AsyncEvent jakartaEvent = AsyncContextConverter.convert(javaxEvent);

        sut.onStartAsync(jakartaEvent);
        sut.onTimeout(jakartaEvent);

        verify(origin).onStartAsync(org.mockito.ArgumentMatchers.any(javax.servlet.AsyncEvent.class));
        verify(origin).onTimeout(org.mockito.ArgumentMatchers.any(javax.servlet.AsyncEvent.class));
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.AsyncEvent) — jakarta AsyncEvent 인스턴스 생성 + throwable 보존")
    void shouldConvertAsyncEventAndPreserveThrowable_AC2_77_08() {
        javax.servlet.AsyncContext javaxCtx = mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        RuntimeException cause = new RuntimeException("async failure");
        javax.servlet.AsyncEvent javaxEvent =
                new javax.servlet.AsyncEvent(javaxCtx, cause);

        AsyncEvent jakartaEvent = AsyncContextConverter.convert(javaxEvent);

        assertNotNull(jakartaEvent);
        assertTrue(cause == jakartaEvent.getThrowable(),
                "throwable 은 동일 인스턴스로 보존되어야 한다");
    }
}
