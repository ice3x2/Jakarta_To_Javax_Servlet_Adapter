package adapter.javax.servlet.http;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.javax.servlet5.http.Cookie} instead. 본 클래스는 v2.0
 * 에서 제거됩니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class Cookie extends adapter.javax.servlet5.http.Cookie {
    public Cookie(jakarta.servlet.http.Cookie cookie) {
        super(cookie);
    }
}
