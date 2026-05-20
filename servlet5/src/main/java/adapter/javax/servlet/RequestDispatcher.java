package adapter.javax.servlet;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.javax.servlet5.RequestDispatcher} instead. 본 클래스는 v2.0
 * 에서 제거됩니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class RequestDispatcher extends adapter.javax.servlet5.RequestDispatcher {
    public RequestDispatcher(jakarta.servlet.RequestDispatcher requestDispatcher) {
        super(requestDispatcher);
    }
}
