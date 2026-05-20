package adapter.common.testfakes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import java.util.Collections;
import java.util.Enumeration;

/**
 * Minimal fake base class for {@link HttpSession} used exclusively by
 * {@code adapter.common.ConversionContract} verifier methods.
 *
 * @req FR-CONV-009
 */
@SuppressWarnings("deprecation")
public abstract class AbstractFakeHttpSession implements HttpSession {

    @Override public long getCreationTime() { return 0L; }
    @Override public String getId() { return "fake"; }
    @Override public long getLastAccessedTime() { return 0L; }
    @Override public ServletContext getServletContext() { return null; }
    @Override public void setMaxInactiveInterval(int interval) {}
    @Override public int getMaxInactiveInterval() { return 0; }
    @Override public HttpSessionContext getSessionContext() { return null; }
    @Override public Object getAttribute(String name) { return null; }
    @Override public Object getValue(String name) { return null; }
    @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
    @Override public String[] getValueNames() { return new String[0]; }
    @Override public void setAttribute(String name, Object value) {}
    @Override public void putValue(String name, Object value) {}
    @Override public void removeAttribute(String name) {}
    @Override public void removeValue(String name) {}
    @Override public void invalidate() {}
    @Override public boolean isNew() { return false; }
}
