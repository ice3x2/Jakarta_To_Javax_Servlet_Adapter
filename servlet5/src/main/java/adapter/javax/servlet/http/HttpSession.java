package adapter.javax.servlet.http;

import adapter.common.AdapterUnwrap;

import java.util.Enumeration;
import javax.servlet.ServletContext;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.javax.servlet5.http.HttpSession} instead. 본 클래스는 v2.0
 * 에서 제거됩니다. 원본의 생성자가 package-private 이므로 composition delegate
 * 형태로 제공합니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@SuppressWarnings({"deprecation", "removal"})
public class HttpSession implements javax.servlet.http.HttpSession, AdapterUnwrap<jakarta.servlet.http.HttpSession> {
    private final jakarta.servlet.http.HttpSession httpSession;

    public HttpSession(jakarta.servlet.http.HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override public jakarta.servlet.http.HttpSession unwrap() {
        return this.httpSession;
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

    @Override public long getCreationTime() { return this.httpSession.getCreationTime(); }
    @Override public String getId() { return this.httpSession.getId(); }
    @Override public long getLastAccessedTime() { return this.httpSession.getLastAccessedTime(); }
    @Override public ServletContext getServletContext() {
        return new adapter.javax.servlet5.ServletContext(this.httpSession.getServletContext());
    }
    @Override public void setMaxInactiveInterval(int interval) { this.httpSession.setMaxInactiveInterval(interval); }
    @Override public int getMaxInactiveInterval() { return this.httpSession.getMaxInactiveInterval(); }
    @Override public javax.servlet.http.HttpSessionContext getSessionContext() {
        return new HttpSessionContext(this.httpSession.getSessionContext());
    }
    @Override public Object getAttribute(String name) { return this.httpSession.getAttribute(name); }
    @Override public Object getValue(String name) { return this.httpSession.getValue(name); }
    @Override public Enumeration<String> getAttributeNames() { return this.httpSession.getAttributeNames(); }
    @Override public String[] getValueNames() { return this.httpSession.getValueNames(); }
    @Override public void setAttribute(String name, Object value) { this.httpSession.setAttribute(name, value); }
    @Override public void putValue(String name, Object value) { this.httpSession.putValue(name, value); }
    @Override public void removeAttribute(String name) { this.httpSession.removeAttribute(name); }
    @Override public void removeValue(String name) { this.httpSession.removeValue(name); }
    @Override public void invalidate() { this.httpSession.invalidate(); }
    @Override public boolean isNew() { return this.httpSession.isNew(); }
}
