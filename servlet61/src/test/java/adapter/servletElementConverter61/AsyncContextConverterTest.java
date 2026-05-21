package adapter.servletElementConverter61;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * {@link adapter.servletElementConverter61.AsyncContextConverter} factory + listener
 * 위임 단위 테스트.
 *
 * <p>본 테스트는 javax {@code AsyncContext} 를 jakarta {@code AsyncContext} 로 변환한
 * 어댑터 인스턴스가 핵심 위임 메서드 (complete / addListener / getRequest /
 * getResponse) 를 원본에 정확히 위임하는지 검증한다. 동시에 onComplete 같은
 * listener 콜백이 변환된 listener 를 통해 원본 listener 로 round-trip 위임됨도
 * 검증한다 (round-trip = factory 한 번 호출 시 변환된 jakarta {@code AsyncEvent} 가
 * 다시 javax 어댑터를 거쳐 원본 listener 로 도달).
 *
 * <p>Mock 정책 (FR-TEST-001 AC-3): javax {@code AsyncContext} / {@code AsyncListener} /
 * {@code ServletRequest} / {@code ServletResponse} 만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 * SUT (AsyncContextConverter 가 반환하는 어댑터 인스턴스) 자체는 mock 하지 않는다.
 *
 * @req FR-TEST-001
 */
class AsyncContextConverterTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: javax->jakarta AsyncContext 변환 시
    //   (a) complete() 가 원본 complete 에 위임된다.
    //   (b) getRequest() / getResponse() 는 원본 호출 + 변환된 jakarta ServletRequest/Response 반환.
    //   (c) addListener(AsyncListener) 가 원본에 위임되고, 등록된 javax listener 가
    //       AsyncEvent round-trip 시 원본 listener.onComplete 로 위임된다.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: javax->jakarta AsyncContext 의 핵심 위임 메서드와 listener round-trip 이 동작한다")
    void shouldConvertAsyncContextRoundTrip_AC1() throws Exception {
        javax.servlet.AsyncContext originAsyncContext =
            mock(javax.servlet.AsyncContext.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.ServletRequest originRequest =
            mock(javax.servlet.ServletRequest.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.ServletResponse originResponse =
            mock(javax.servlet.ServletResponse.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(originAsyncContext.getRequest()).thenReturn(originRequest);
        when(originAsyncContext.getResponse()).thenReturn(originResponse);

        AsyncContext adapter = AsyncContextConverter.convert(originAsyncContext);

        // (a) complete 위임
        adapter.complete();
        verify(originAsyncContext).complete();

        // (b) getRequest / getResponse 위임 + 변환 결과 non-null
        jakarta.servlet.ServletRequest convertedRequest = adapter.getRequest();
        jakarta.servlet.ServletResponse convertedResponse = adapter.getResponse();
        verify(originAsyncContext).getRequest();
        verify(originAsyncContext).getResponse();
        assertNotNull(convertedRequest, "변환된 jakarta ServletRequest 는 null 이 아니다");
        assertNotNull(convertedResponse, "변환된 jakarta ServletResponse 는 null 이 아니다");

        // (c) listener round-trip: jakarta listener 추가 → 원본 javax listener 캡처 →
        //     변환된 javax AsyncEvent 로 onComplete 호출 시 원본 jakarta listener.onComplete 호출됨.
        AsyncListener jakartaListener =
            mock(AsyncListener.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        adapter.addListener(jakartaListener);

        org.mockito.ArgumentCaptor<javax.servlet.AsyncListener> javaxListenerCaptor =
            org.mockito.ArgumentCaptor.forClass(javax.servlet.AsyncListener.class);
        verify(originAsyncContext).addListener(javaxListenerCaptor.capture());
        javax.servlet.AsyncListener capturedJavaxListener = javaxListenerCaptor.getValue();
        assertNotNull(capturedJavaxListener, "원본 AsyncContext 에 등록된 javax listener 는 null 이 아니다");
        assertNotSame((Object) jakartaListener, (Object) capturedJavaxListener,
            "원본에 등록되는 listener 는 변환된 javax 어댑터 인스턴스여야 한다");

        // round-trip: javax 어댑터 listener.onComplete 호출 → 원본 jakarta listener.onComplete 호출
        javax.servlet.AsyncEvent javaxEvent =
            mock(javax.servlet.AsyncEvent.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(javaxEvent.getAsyncContext()).thenReturn(originAsyncContext);
        when(javaxEvent.getSuppliedRequest()).thenReturn(originRequest);
        when(javaxEvent.getSuppliedResponse()).thenReturn(originResponse);

        capturedJavaxListener.onComplete(javaxEvent);
        verify(jakartaListener).onComplete(org.mockito.ArgumentMatchers.any(jakarta.servlet.AsyncEvent.class));
    }
}
