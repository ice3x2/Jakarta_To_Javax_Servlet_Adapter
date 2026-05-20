package adapter.jakarta.servlet;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.jakarta.servlet5.ServletRequest} instead. 본 클래스는 v2.0
 * 에서 제거됩니다. 자세한 마이그레이션 안내는 {@code docs/migration/0.x-to-1.0.md}
 * 를 참고하세요.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class ServletRequest extends adapter.jakarta.servlet5.ServletRequest {
    public ServletRequest(javax.servlet.ServletRequest request) {
        super(request);
    }
}
