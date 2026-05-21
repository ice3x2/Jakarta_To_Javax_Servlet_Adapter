package adapter.javax.servlet61.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * javax-side {@link adapter.javax.servlet61.http.HttpSession} 단위 위임 테스트.
 *
 * Mirror 패턴: jakarta 원본 (mock) → javax-side 어댑터 wrap → javax 4.x API 호출 시
 * 원본 jakarta 6.1 의 대응 메서드가 호출되는지 검증한다.
 *
 * javax 4.x 에는 deprecated 메서드 {@code getValue(String)}, {@code putValue(String,Object)} 가
 * 존재하지만 jakarta 6.1 에서는 제거되었다. 어댑터는 이를 원본의 attribute API
 * ({@code getAttribute}/{@code setAttribute}) 로 직접 위임한다
 * (HttpSession.java:67-71/87-91 placeholder pending PH-004).
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * HttpSession 어댑터 생성자는 package-private — 동일 패키지 내 테스트로 직접 접근.
 *
 * @req FR-TEST-001
 */
class HttpSessionDelegationTest {

    private static HttpSession newAdapter(jakarta.servlet.http.HttpSession origin) {
        try {
            Constructor<HttpSession> ctor =
                HttpSession.class.getDeclaredConstructor(jakarta.servlet.http.HttpSession.class);
            ctor.setAccessible(true);
            return ctor.newInstance(origin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: javax 4.x getValue/putValue 가 원본 jakarta 의 attribute
    // API 로 직접 위임되는지 확인.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpSession.getValue/putValue 는 원본 jakarta getAttribute/setAttribute 에 직접 위임된다")
    void shouldDelegateGetValuePutValueDirectly_AC1() {
        jakarta.servlet.http.HttpSession origin =
            mock(jakarta.servlet.http.HttpSession.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getAttribute("k")).thenReturn("v");

        HttpSession adapter = newAdapter(origin);

        // putValue → 원본 setAttribute 호출.
        adapter.putValue("k", "v");
        verify(origin).setAttribute("k", "v");

        // getValue → 원본 getAttribute 호출 + 결과 그대로 반환.
        Object value = adapter.getValue("k");
        verify(origin).getAttribute("k");
        assertEquals("v", value, "getValue 는 attribute 값 그대로 반환");
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #2: getAttributeNames — Enumeration 단순 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: HttpSession.getAttributeNames 는 원본의 동일 메서드를 위임 호출한다")
    void shouldDelegateGetAttributeNames_AC2() {
        jakarta.servlet.http.HttpSession origin =
            mock(jakarta.servlet.http.HttpSession.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        Enumeration<String> names = Collections.enumeration(Collections.singletonList("k"));
        when(origin.getAttributeNames()).thenReturn(names);

        HttpSession adapter = newAdapter(origin);

        Enumeration<String> result = adapter.getAttributeNames();

        verify(origin).getAttributeNames();
        assertSame(names, result, "원본 Enumeration 인스턴스가 그대로 반환된다");
    }
}
