package adapter.common.testfakes;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

/**
 * Minimal fake base class for {@link ServletContext} used exclusively by
 * {@code adapter.common.ConversionContract} verifier methods.
 *
 * @req FR-CONV-006
 */
public abstract class AbstractFakeServletContext implements ServletContext {

    @Override public String getContextPath() { return ""; }
    @Override public ServletContext getContext(String uripath) { return null; }
    @Override public int getMajorVersion() { return 4; }
    @Override public int getMinorVersion() { return 0; }
    @Override public int getEffectiveMajorVersion() { return 4; }
    @Override public int getEffectiveMinorVersion() { return 0; }
    @Override public String getMimeType(String file) { return null; }
    @Override public Set<String> getResourcePaths(String path) { return Collections.emptySet(); }
    @Override public URL getResource(String path) throws MalformedURLException { return null; }
    @Override public InputStream getResourceAsStream(String path) { return null; }
    @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public RequestDispatcher getNamedDispatcher(String name) { return null; }
    @Override public Servlet getServlet(String name) { return null; }
    @Override public Enumeration<Servlet> getServlets() { return Collections.emptyEnumeration(); }
    @Override public Enumeration<String> getServletNames() { return Collections.emptyEnumeration(); }
    @Override public void log(String msg) {}
    @Override public void log(Exception exception, String msg) {}
    @Override public void log(String message, Throwable throwable) {}
    @Override public String getRealPath(String path) { return null; }
    @Override public String getServerInfo() { return "fake"; }
    @Override public String getInitParameter(String name) { return null; }
    @Override public Enumeration<String> getInitParameterNames() { return Collections.emptyEnumeration(); }
    @Override public boolean setInitParameter(String name, String value) { return false; }
    @Override public Object getAttribute(String name) { return null; }
    @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
    @Override public void setAttribute(String name, Object object) {}
    @Override public void removeAttribute(String name) {}
    @Override public String getServletContextName() { return "fake"; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, String className) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) { return null; }
    @Override public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) { return null; }
    @Override public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException { return null; }
    @Override public ServletRegistration getServletRegistration(String servletName) { return null; }
    @Override public Map<String, ? extends ServletRegistration> getServletRegistrations() { return Collections.emptyMap(); }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, String className) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) { return null; }
    @Override public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException { return null; }
    @Override public FilterRegistration getFilterRegistration(String filterName) { return null; }
    @Override public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return Collections.emptyMap(); }
    @Override public SessionCookieConfig getSessionCookieConfig() { return null; }
    @Override public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {}
    @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() { return EnumSet.noneOf(SessionTrackingMode.class); }
    @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() { return EnumSet.noneOf(SessionTrackingMode.class); }
    @Override public void addListener(String className) {}
    @Override public <T extends EventListener> void addListener(T t) {}
    @Override public void addListener(Class<? extends EventListener> listenerClass) {}
    @Override public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException { return null; }
    @Override public JspConfigDescriptor getJspConfigDescriptor() { return null; }
    @Override public ClassLoader getClassLoader() { return getClass().getClassLoader(); }
    @Override public void declareRoles(String... roleNames) {}
    @Override public String getVirtualServerName() { return "fake"; }
    @Override public int getSessionTimeout() { return 0; }
    @Override public void setSessionTimeout(int sessionTimeout) {}
    @Override public String getRequestCharacterEncoding() { return null; }
    @Override public void setRequestCharacterEncoding(String encoding) {}
    @Override public String getResponseCharacterEncoding() { return null; }
    @Override public void setResponseCharacterEncoding(String encoding) {}
}
