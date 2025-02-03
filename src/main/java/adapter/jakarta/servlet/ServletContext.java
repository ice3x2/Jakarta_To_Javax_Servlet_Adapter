package adapter.jakarta.servlet;

import adapter.servletElementConverter.DescriptorConverter;
import adapter.servletElementConverter.EnumsConverter;
import adapter.servletElementConverter.ServletConverter;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import javax.servlet.FilterRegistration;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"deprecation", "DuplicatedCode"})
public class ServletContext implements jakarta.servlet.ServletContext {
   private final javax.servlet.ServletContext servletContext;

    public ServletContext(javax.servlet.ServletContext servletContext) {
      this.servletContext = servletContext;
   }

   @Override public String getContextPath() {
      return this.servletContext.getContextPath();
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
   @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.servletContext.getRequestDispatcher(path));
   }
   @Override public Servlet getServlet(String name) throws ServletException {
      try {
         return ServletConverter.convert(this.servletContext.getServlet(name));
      } catch (javax.servlet.ServletException var3) {
         throw new ServletException(var3);
      }
   }
   @SuppressWarnings("DuplicatedCode")
   @Override public Enumeration<Servlet> getServlets() {
      final Enumeration<javax.servlet.Servlet> jakartaServlets = this.servletContext.getServlets();
      return new Enumeration<Servlet>() {
         @Override public boolean hasMoreElements() {
            return jakartaServlets.hasMoreElements();
         }

         @Override public Servlet nextElement() {
            return ServletConverter.convert(jakartaServlets.nextElement());
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
   @Override public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
      final Servlet javaXservlet;

       //noinspection DuplicatedCode
       try {
         Class<?> servletClass = this.getClassLoader().loadClass(className);
         Constructor<?> constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = (Servlet)constructor.newInstance();
      } catch (Exception var6) {
         throw new RuntimeException(var6);
      }

      return this.addServlet(servletName, javaXservlet);
   }

   @Override public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
      return (jakarta.servlet.ServletRegistration.Dynamic)ServletConverter.convert((javax.servlet.Registration)this.servletContext.addServlet(servletName, ServletConverter.convert(servlet)));
   }
   @Override public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
      final Servlet javaXservlet;

      try {
         Constructor<? extends Servlet> constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = constructor.newInstance();
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }

      return this.addServlet(servletName, javaXservlet);
   }

   @Override public <T extends Servlet> T createServlet(Class<T> servletClass) {
      final T javaXservlet;

      try {
         Constructor<T> constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = constructor.newInstance();

         return javaXservlet;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
   @Override public jakarta.servlet.ServletRegistration getServletRegistration(String servletName) {
      return (jakarta.servlet.ServletRegistration)ServletConverter.convert((javax.servlet.Registration)this.servletContext.getServletRegistration(servletName));
   }
   @Override public Map<String, ? extends jakarta.servlet.ServletRegistration> getServletRegistrations() {
      Map<String, ? extends javax.servlet.ServletRegistration> origin = this.servletContext.getServletRegistrations();
      Map<String, ServletRegistration> dest = new HashMap<>();

      for (Map.Entry<String, ? extends javax.servlet.ServletRegistration> stringEntry : origin.entrySet()) {
         javax.servlet.ServletRegistration servletRegistration = stringEntry.getValue();
         jakarta.servlet.ServletRegistration jakartaServletRegistration = ServletConverter.convert(servletRegistration);
         dest.put(stringEntry.getKey(),jakartaServletRegistration);
      }
      return dest;
   }
   @Override public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
      final Filter javaXFilter;

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
   @Override public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
      return ServletConverter.convert(this.servletContext.addFilter(filterName, ServletConverter.convert(filter)));
   }
   @Override public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
      try {
         Filter filter = this.createFilter(filterClass);
         return ServletConverter.convert(this.servletContext.addFilter(filterName, ServletConverter.convert(filter)));
      } catch (ServletException e) {
         throw new RuntimeException(e);
      }
   }
   @Override public <T extends Filter> T createFilter(Class<T> filterClass) throws ServletException {
      final T javaXFilter;
      try {
         Constructor<T> constructor = filterClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXFilter = constructor.newInstance();
         return javaXFilter;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
   @Override public jakarta.servlet.FilterRegistration getFilterRegistration(String filterName) {
      return (jakarta.servlet.FilterRegistration)ServletConverter.convert((javax.servlet.Registration)this.servletContext.getFilterRegistration(filterName));
   }
   @Override public Map<String, ? extends jakarta.servlet.FilterRegistration> getFilterRegistrations() {
      Map<String, ? extends FilterRegistration> ori = this.servletContext.getFilterRegistrations();
      Map<String, jakarta.servlet.FilterRegistration> dest = new HashMap<>();
      for (Map.Entry<String, ? extends FilterRegistration> entry : ori.entrySet()) {
         dest.put(entry.getKey(), ServletConverter.convert(entry.getValue()));
      }
      return dest;
   }
   @Override public SessionCookieConfig getSessionCookieConfig() {
      return ServletConverter.convert(this.servletContext.getSessionCookieConfig());
   }
   @Override public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
      Set<javax.servlet.SessionTrackingMode> resultTrackModes = new HashSet<>();
      /*
      sessionTrackingModes.forEach(new Consumer<SessionTrackingMode>() {
         @Override
         @Override public void accept(SessionTrackingMode mode) {
            resultTrackModes.add(EnumsConverter.convert(mode));
         }
      });**/

      for(SessionTrackingMode mode : sessionTrackingModes) {
          resultTrackModes.add(EnumsConverter.convert(mode));
      }
      this.servletContext.setSessionTrackingModes(resultTrackModes);
   }
   @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
      Set<javax.servlet.SessionTrackingMode> sessionTrackModes = this.servletContext.getDefaultSessionTrackingModes();
      Set<SessionTrackingMode> resultTrackModes = new HashSet<>();
      /*sessionTrackModes.forEach((mode) -> {
         resultTrackModes.add(EnumsConverter.convert(mode));
      });*/

        for(javax.servlet.SessionTrackingMode mode : sessionTrackModes) {
            resultTrackModes.add(EnumsConverter.convert(mode));
        }
      return resultTrackModes;
   }
   @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
      Set<javax.servlet.SessionTrackingMode> sessionTrackModes = this.servletContext.getEffectiveSessionTrackingModes();
      Set<SessionTrackingMode> resultTrackModes = new HashSet<>();

      /*sessionTrackModes.forEach((mode) -> {
         resultTrackModes.add(EnumsConverter.convert(mode));
      });*/

     for(javax.servlet.SessionTrackingMode mode : sessionTrackModes) {
         resultTrackModes.add(EnumsConverter.convert(mode));
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
      } catch (javax.servlet.ServletException var3) {
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
   @Override public jakarta.servlet.ServletRegistration.Dynamic addJspFile(String arg0, String arg1) {
      return new ServletRegistrationDynamic(this.servletContext.addJspFile(arg0, arg1));

   }
   @Override public ServletContext getContext(String arg0) {
      return new ServletContext(this.servletContext.getContext(arg0));
   }

   @Override public jakarta.servlet.RequestDispatcher getNamedDispatcher(String name) {
      return new RequestDispatcher(this.servletContext.getNamedDispatcher(name));
   }
   @Override public String getRequestCharacterEncoding() {
      return this.servletContext.getRequestCharacterEncoding();
   }
   @Override public String getResponseCharacterEncoding() {
      return this.servletContext.getResponseCharacterEncoding();
   }

   @Override public int getSessionTimeout() {
      return this.servletContext.getSessionTimeout();
   }

   @Override public String getVirtualServerName() {
      return this.servletContext.getVirtualServerName();
   }

   @Override public void setRequestCharacterEncoding(String arg0) {
      this.servletContext.setRequestCharacterEncoding(arg0);
   }

   @Override public void setResponseCharacterEncoding(String arg0) {
      this.setRequestCharacterEncoding(arg0);
   }

   @Override public void setSessionTimeout(int arg0) {
      this.servletContext.setSessionTimeout(arg0);
   }
}
