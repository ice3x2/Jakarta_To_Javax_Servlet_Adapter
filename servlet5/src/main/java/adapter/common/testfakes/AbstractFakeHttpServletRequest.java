package adapter.common.testfakes;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal fake base class for {@link HttpServletRequest} used exclusively by
 * {@code adapter.common.ConversionContract} verifier methods. All methods
 * return safe defaults; tests override only the methods they exercise.
 *
 * <p>This is a hand-written fake — no mocking framework involvement
 * (kiwi-coder SSOT §0.6).
 *
 * @req FR-CONV-006
 * @req FR-CONV-009
 */
public abstract class AbstractFakeHttpServletRequest implements HttpServletRequest {

    @Override public String getAuthType() { return null; }
    @Override public Cookie[] getCookies() { return null; }
    @Override public long getDateHeader(String name) { return -1L; }
    @Override public String getHeader(String name) { return null; }
    @Override public Enumeration<String> getHeaders(String name) { return Collections.emptyEnumeration(); }
    @Override public Enumeration<String> getHeaderNames() { return Collections.emptyEnumeration(); }
    @Override public int getIntHeader(String name) { return -1; }
    @Override public String getMethod() { return "GET"; }
    @Override public String getPathInfo() { return null; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getContextPath() { return ""; }
    @Override public String getQueryString() { return null; }
    @Override public String getRemoteUser() { return null; }
    @Override public boolean isUserInRole(String role) { return false; }
    @Override public Principal getUserPrincipal() { return null; }
    @Override public String getRequestedSessionId() { return null; }
    @Override public String getRequestURI() { return "/"; }
    @Override public StringBuffer getRequestURL() { return new StringBuffer("/"); }
    @Override public String getServletPath() { return ""; }
    @Override public HttpSession getSession(boolean create) { return null; }
    @Override public HttpSession getSession() { return null; }
    @Override public String changeSessionId() { return ""; }
    @Override public boolean isRequestedSessionIdValid() { return false; }
    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
    @Override public boolean isRequestedSessionIdFromURL() { return false; }
    @Override public boolean isRequestedSessionIdFromUrl() { return false; }
    @Override public boolean authenticate(HttpServletResponse response) { return false; }
    @Override public void login(String username, String password) {}
    @Override public void logout() {}
    @Override public Collection<Part> getParts() { return Collections.emptyList(); }
    @Override public Part getPart(String name) { return null; }
    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
    @Override public Object getAttribute(String name) { return null; }
    @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
    @Override public String getCharacterEncoding() { return null; }
    @Override public void setCharacterEncoding(String env) {}
    @Override public int getContentLength() { return -1; }
    @Override public long getContentLengthLong() { return -1L; }
    @Override public String getContentType() { return null; }
    @Override public ServletInputStream getInputStream() throws IOException { return null; }
    @Override public String getParameter(String name) { return null; }
    @Override public Enumeration<String> getParameterNames() { return Collections.emptyEnumeration(); }
    @Override public String[] getParameterValues(String name) { return null; }
    @Override public Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }
    @Override public String getProtocol() { return "HTTP/1.1"; }
    @Override public String getScheme() { return "http"; }
    @Override public String getServerName() { return "localhost"; }
    @Override public int getServerPort() { return 80; }
    @Override public BufferedReader getReader() throws IOException { return null; }
    @Override public String getRemoteAddr() { return "127.0.0.1"; }
    @Override public String getRemoteHost() { return "localhost"; }
    @Override public void setAttribute(String name, Object o) {}
    @Override public void removeAttribute(String name) {}
    @Override public Locale getLocale() { return Locale.getDefault(); }
    @Override public Enumeration<Locale> getLocales() { return Collections.emptyEnumeration(); }
    @Override public boolean isSecure() { return false; }
    @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public String getRealPath(String path) { return null; }
    @Override public int getRemotePort() { return 0; }
    @Override public String getLocalName() { return "localhost"; }
    @Override public String getLocalAddr() { return "127.0.0.1"; }
    @Override public int getLocalPort() { return 80; }
    @Override public ServletContext getServletContext() { return null; }
    @Override public AsyncContext startAsync() { return null; }
    @Override public AsyncContext startAsync(ServletRequest req, ServletResponse res) { return null; }
    @Override public boolean isAsyncStarted() { return false; }
    @Override public boolean isAsyncSupported() { return false; }
    @Override public AsyncContext getAsyncContext() { return null; }
    @Override public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }
}
