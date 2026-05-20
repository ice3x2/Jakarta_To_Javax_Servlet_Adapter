package adapter.javax.servlet.http;

import adapter.common.AdapterUnwrap;

import java.util.Enumeration;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.javax.servlet5.http.HttpSessionContext} instead. 본 클래스는 v2.0
 * 에서 제거됩니다. 원본의 생성자가 package-private 이므로 composition delegate
 * 형태로 제공합니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed", "removal"})
public class HttpSessionContext implements javax.servlet.http.HttpSessionContext, AdapterUnwrap<jakarta.servlet.http.HttpSessionContext> {
    private final jakarta.servlet.http.HttpSessionContext httpSessionContext;

    public HttpSessionContext(jakarta.servlet.http.HttpSessionContext httpSessionContext) {
        this.httpSessionContext = httpSessionContext;
    }

    @Override public jakarta.servlet.http.HttpSessionContext unwrap() {
        return this.httpSessionContext;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Object other = (o instanceof AdapterUnwrap<?>) ? ((AdapterUnwrap<?>) o).unwrap() : o;
        return this.unwrap().equals(other);
    }

    @Override public int hashCode() {
        return this.unwrap().hashCode();
    }

    @Override public javax.servlet.http.HttpSession getSession(String sessionId) {
        return new HttpSession(this.httpSessionContext.getSession(sessionId));
    }

    @Override public Enumeration<String> getIds() {
        return this.httpSessionContext.getIds();
    }
}
