package adapter.javax.servlet;

import adapter.servletElementConverter.DescriptorConverter;
import adapter.servletElementConverter.EnumsConverter;
import adapter.servletElementConverter.ServletConverter;
import jakarta.servlet.FilterRegistration;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class ServletContext implements javax.servlet.ServletContext {
   private final jakarta.servlet.ServletContext servletContext;

   public ServletContext(jakarta.servlet.ServletContext servletContext) {
      this.servletContext = servletContext;
   }

   @Override public String getContextPath() {
      return this.servletContext.getContextPath();
   }

   @Override public javax.servlet.ServletContext getContext(String uriPath) {
      return new ServletContext(this.servletContext.getContext(uriPath));
   }

   @Override public int getMajorVersion() {
      return this.servletContext.getMajorVersion();
   }

   @Override public int getMinorVersion() {
      return this.servletContext.getMinorVersion();
   }

   @Override public int getEffectiveMajorVersion() {
      return this.servletContext.getEffectiveMajorVersion();
   }

   @Override public int getEffectiveMinorVersion() {
      return this.servletContext.getEffectiveMinorVersion();
   }

   @Override public String getMimeType(String file) {
      return this.servletContext.getMimeType(file);
   }

   @Override public Set<String> getResourcePaths(String path) {
      return this.servletContext.getResourcePaths(path);
   }

   @Override public URL getResource(String path) throws MalformedURLException {
      return this.servletContext.getResource(path);
   }

   @Override public InputStream getResourceAsStream(String path) {
      return this.servletContext.getResourceAsStream(path);
   }

   @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.servletContext.getRequestDispatcher(path));
   }

   @Override public javax.servlet.RequestDispatcher getNamedDispatcher(String name) {
      return new RequestDispatcher(this.servletContext.getNamedDispatcher(name));
   }

   @Override public Servlet getServlet(String name) throws ServletException {
      try {
         return ServletConverter.convert(this.servletContext.getServlet(name));
      } catch (jakarta.servlet.ServletException var3) {
         throw new ServletException(var3);
      }
   }

   @Override public Enumeration<Servlet> getServlets() {
      final Enumeration<jakarta.servlet.Servlet> jakartaServlets = this.servletContext.getServlets();
      return new Enumeration<Servlet>() {
         @Override public boolean hasMoreElements() {
            return jakartaServlets.hasMoreElements();
         }

         @Override public Servlet nextElement() {
            return ServletConverter.convert((jakarta.servlet.Servlet)jakartaServlets.nextElement());
         }
      };
   }

   @Override public Enumeration<String> getServletNames() {
      return this.servletContext.getServletNames();
   }

   @Override public void log(String msg) {
      this.servletContext.log(msg);
   }

   @Override public void log(Exception exception, String msg) {
      this.servletContext.log(exception, msg);
   }

   @Override public void log(String message, Throwable throwable) {
      this.servletContext.log(message, throwable);
   }

   @Override public String getRealPath(String path) {
      return this.servletContext.getRealPath(path);
   }

   @Override public String getServerInfo() {
      return this.servletContext.getServerInfo();
   }

   @Override public String getInitParameter(String name) {
      return this.servletContext.getInitParameter(name);
   }

   @Override public Enumeration<String> getInitParameterNames() {
      return this.servletContext.getInitParameterNames();
   }

   @Override public boolean setInitParameter(String name, String value) {
      return this.servletContext.setInitParameter(name, value);
   }

   @Override public Object getAttribute(String name) {
      return this.servletContext.getAttribute(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.servletContext.getAttributeNames();
   }

   @Override public void setAttribute(String name, Object object) {
      this.servletContext.setAttribute(name, object);
   }

   @Override public void removeAttribute(String name) {
      this.servletContext.removeAttribute(name);
   }

   @Override public String getServletContextName() {
      return this.servletContext.getServletContextName();
   }

   @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
      Servlet javaXservlet = null;

      try {
         Class<?> servletClass = this.getClassLoader().loadClass(className);
         Constructor<?> constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = (Servlet)constructor.newInstance();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      return this.addServlet(servletName, javaXservlet);
   }

   @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
      return (javax.servlet.ServletRegistration.Dynamic)ServletConverter.convert(this.servletContext.addServlet(servletName, ServletConverter.convert(servlet)));
   }

   @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
      Servlet javaXservlet = null;

      try {
         Constructor<? extends Servlet> constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = constructor.newInstance();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      return this.addServlet(servletName, javaXservlet);
   }



   @Override public <T extends Servlet> T createServlet(Class<T> servletClass) throws ServletException {
      T javaXservlet = null;
      try {
         Constructor<T> constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = constructor.newInstance();
         return javaXservlet;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override public javax.servlet.ServletRegistration getServletRegistration(String servletName) {
      return (javax.servlet.ServletRegistration)ServletConverter.convert((jakarta.servlet.Registration)this.servletContext.getServletRegistration(servletName));
   }

   @Override public Map<String, ? extends javax.servlet.ServletRegistration> getServletRegistrations() {
      Map<String, ? extends jakarta.servlet.ServletRegistration> origin = this.servletContext.getServletRegistrations();
      Map<String, javax.servlet.ServletRegistration> dest = new HashMap<>();

      for (Map.Entry<String, ? extends jakarta.servlet.ServletRegistration> entry : origin.entrySet()) {
         dest.put(entry.getKey(), ServletConverter.convert(entry.getValue()));
      }
      return dest;
   }

   @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
      Filter javaXFilter = null;
      try {
         Class<?> filterClass = this.getClassLoader().loadClass(className);
         Constructor<?> constructor = filterClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXFilter = (Filter)constructor.newInstance();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      return this.addFilter(filterName, javaXFilter);
   }

   @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
      return (javax.servlet.FilterRegistration.Dynamic)ServletConverter.convert((jakarta.servlet.Registration)this.servletContext.addFilter(filterName, ServletConverter.convert(filter)));
   }

   @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
      try {
         Filter filter = this.createFilter(filterClass);
         return (javax.servlet.FilterRegistration.Dynamic)ServletConverter.convert((jakarta.servlet.Registration)this.servletContext.addFilter(filterName, ServletConverter.convert(filter)));
      } catch (ServletException var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override public <T extends Filter> T createFilter(Class<T> filterClass) throws ServletException {
      T javaXFilter = null;

      try {
         Constructor<T> constructor = filterClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXFilter = constructor.newInstance();
         return javaXFilter;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override public javax.servlet.FilterRegistration getFilterRegistration(String filterName) {
      return (javax.servlet.FilterRegistration)ServletConverter.convert((jakarta.servlet.Registration)this.servletContext.getFilterRegistration(filterName));
   }

   @Override public Map<String, ? extends javax.servlet.FilterRegistration> getFilterRegistrations() {
      Map<String, ? extends FilterRegistration> ori = this.servletContext.getFilterRegistrations();
      Map<String, javax.servlet.FilterRegistration> dest = new HashMap<>();

      for (Map.Entry<String, ? extends FilterRegistration> entry : ori.entrySet()) {
         dest.put(entry.getKey(), ServletConverter.convert(entry.getValue()));
      }
      return dest;
   }

   @Override public SessionCookieConfig getSessionCookieConfig() {
      return ServletConverter.convert(this.servletContext.getSessionCookieConfig());
   }

   @Override public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
      Set<jakarta.servlet.SessionTrackingMode> resultTrackModes = new HashSet<>();

         for (SessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
             resultTrackModes.add(EnumsConverter.convert(sessionTrackingMode));
         }



      this.servletContext.setSessionTrackingModes(resultTrackModes);
   }

   @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
      Set<jakarta.servlet.SessionTrackingMode> sessionTrackModes = this.servletContext.getDefaultSessionTrackingModes();
      Set<SessionTrackingMode> resultTrackModes = new HashSet<>();

         for (jakarta.servlet.SessionTrackingMode sessionTrackingMode : sessionTrackModes) {
             resultTrackModes.add(EnumsConverter.convert(sessionTrackingMode));
         }
      return resultTrackModes;
   }

   @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
      Set<jakarta.servlet.SessionTrackingMode> sessionTrackModes = this.servletContext.getEffectiveSessionTrackingModes();
      Set<SessionTrackingMode> resultTrackModes = new HashSet<>();
      for (jakarta.servlet.SessionTrackingMode sessionTrackingMode : sessionTrackModes) {
          resultTrackModes.add(EnumsConverter.convert(sessionTrackingMode));
      }
      return resultTrackModes;
   }

   @Override public void addListener(String className) {
      this.servletContext.addListener(className);
   }

   @Override public <T extends EventListener> void addListener(T t) {
      this.servletContext.addListener(t);
   }

   @Override public void addListener(Class<? extends EventListener> listenerClass) {
      this.servletContext.addListener(listenerClass);
   }

   @Override public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
      try {
         return this.servletContext.createListener(c);
      } catch (jakarta.servlet.ServletException var3) {
         throw new ServletException(var3);
      }
   }

   @Override public JspConfigDescriptor getJspConfigDescriptor() {
      return DescriptorConverter.convert(this.servletContext.getJspConfigDescriptor());
   }

   @Override public ClassLoader getClassLoader() {
      return this.servletContext.getClassLoader();
   }

   @Override public void declareRoles(String... roleNames) {
      this.servletContext.declareRoles(roleNames);
   }


   @Override public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
      return new ServletRegistrationDynamic(this.servletContext.addJspFile(servletName, jspFile));
   }

   @Override public String getVirtualServerName() {
      return this.servletContext.getVirtualServerName();
   }

   @Override public int getSessionTimeout() {
      return this.servletContext.getSessionTimeout();
   }

   @Override public void setSessionTimeout(int sessionTimeout) {
      this.servletContext.setSessionTimeout(sessionTimeout);
   }

   @Override public String getRequestCharacterEncoding() {
      return this.servletContext.getRequestCharacterEncoding();
   }

   @Override public void setRequestCharacterEncoding(String encoding) {
      this.setResponseCharacterEncoding(encoding);
   }

   @Override public String getResponseCharacterEncoding() {
      return this.servletContext.getResponseCharacterEncoding();
   }

   @Override public void setResponseCharacterEncoding(String encoding) {
      this.servletContext.setResponseCharacterEncoding(encoding);
   }
}
