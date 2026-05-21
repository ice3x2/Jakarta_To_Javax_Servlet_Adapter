package adapter.jakarta.servlet61.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * jakarta-side {@link adapter.jakarta.servlet61.http.HttpSession} 단위 위임 테스트.
 *
 * AC-2 의 폴백 검증은 jakarta 6.0+ 에서 getValue/putValue 가 인터페이스에서 제거되었지만,
 * 어댑터는 SRS §6.2.2 policy B (FR-CONV-010) 에 따라 attribute API 로 폴백하는 추가
 * public 메서드를 제공한다. 본 테스트는 그 폴백 위임을 검증한다.
 *
 * HttpSession 어댑터 생성자는 package-private — 동일 패키지 내 테스트로 직접 접근.
 *
 * @req FR-TEST-001
 */
class HttpSessionDelegationTest {

    private static HttpSession newAdapter(javax.servlet.http.HttpSession origin) {
        try {
            Constructor<HttpSession> ctor = HttpSession.class.getDeclaredConstructor(javax.servlet.http.HttpSession.class);
            ctor.setAccessible(true);
            return ctor.newInstance(origin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: getAttribute / setAttribute — 단순 위임
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpSession.getAttribute/setAttribute 는 원본 동일 메서드를 위임한다")
    void shouldDelegateGetSetAttribute_AC1() {
        javax.servlet.http.HttpSession origin =
            mock(javax.servlet.http.HttpSession.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getAttribute("key")).thenReturn("value");

        HttpSession adapter = newAdapter(origin);

        // setAttribute 위임
        adapter.setAttribute("key", "value");
        verify(origin).setAttribute("key", "value");

        // getAttribute 위임
        Object got = adapter.getAttribute("key");
        verify(origin).getAttribute("key");
        assertEquals("value", got, "원본 getAttribute 반환값 그대로 반환");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2: getValue/putValue 폴백 (FR-CONV-010 policy B).
    // jakarta 6.0+ 에서 제거된 deprecated 메서드들이 attribute API 로 폴백되는지 확인.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpSession.getValue/putValue 폴백은 원본 attribute API 로 위임된다")
    void shouldFallbackGetValuePutValueToAttribute_AC2() {
        javax.servlet.http.HttpSession origin =
            mock(javax.servlet.http.HttpSession.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getAttribute("k")).thenReturn("v");

        HttpSession adapter = newAdapter(origin);

        // putValue → 원본 setAttribute 호출
        adapter.putValue("k", "v");
        verify(origin).setAttribute("k", "v");

        // getValue → 원본 getAttribute 호출 + 결과 그대로 반환
        Object value = adapter.getValue("k");
        verify(origin).getAttribute("k");
        assertEquals("v", value, "getValue 폴백은 attribute 값 그대로 반환");
    }
}
