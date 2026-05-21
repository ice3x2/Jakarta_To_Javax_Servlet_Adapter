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
 * jakarta-side {@link adapter.jakarta.servlet61.http.Part} 단위 위임 테스트.
 *
 * Part 어댑터 생성자는 package-private — 동일 패키지 내 테스트로 직접 접근.
 *
 * @req FR-TEST-001
 */
class PartDelegationTest {

    private static Part newAdapter(javax.servlet.http.Part origin) {
        try {
            Constructor<Part> ctor = Part.class.getDeclaredConstructor(javax.servlet.http.Part.class);
            ctor.setAccessible(true);
            return ctor.newInstance(origin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: getSubmittedFileName — 원본 위임.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: Part.getSubmittedFileName 은 원본 part 의 동일 메서드를 위임 호출한다")
    void shouldDelegateGetSubmittedFileName_AC1() {
        javax.servlet.http.Part origin =
            mock(javax.servlet.http.Part.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getSubmittedFileName()).thenReturn("upload.txt");

        Part adapter = newAdapter(origin);

        String result = adapter.getSubmittedFileName();

        verify(origin).getSubmittedFileName();
        assertEquals("upload.txt", result, "원본 반환값 그대로 반환");
    }
}
