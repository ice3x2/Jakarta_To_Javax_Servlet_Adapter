package adapter.javax.servlet;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.javax.servlet5.ServletResponse} instead. 본 클래스는 v2.0
 * 에서 제거됩니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class ServletResponse extends adapter.javax.servlet5.ServletResponse {
    public ServletResponse(jakarta.servlet.ServletResponse response) {
        super(response);
    }
}
