package adapter.jakarta.servlet.http;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.jakarta.servlet5.http.WebConnection} instead. 본 클래스는 v2.0
 * 에서 제거됩니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class WebConnection extends adapter.jakarta.servlet5.http.WebConnection {
    public WebConnection(javax.servlet.http.WebConnection webConnection) {
        super(webConnection);
    }
}
