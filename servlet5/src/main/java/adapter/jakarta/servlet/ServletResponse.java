package adapter.jakarta.servlet;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.jakarta.servlet5.ServletResponse} instead. 본 클래스는 v2.0
 * 에서 제거됩니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class ServletResponse extends adapter.jakarta.servlet5.ServletResponse {
    public ServletResponse(javax.servlet.ServletResponse response) {
        super(response);
    }
}
