package adapter.jakarta.servlet;

import adapter.servletElementConverter.DescriptorConverter;
import adapter.servletElementConverter.EnumsConverter;
import adapter.servletElementConverter.ServletConverter;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
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

public class ServletContext implements jakarta.servlet.ServletContext {
   javax.servlet.ServletContext servletContext;

   public ServletContext(javax.servlet.ServletContext servletContext) {
      this.servletContext = servletContext;
   }

   public String getContextPath() {
      return this.servletContext.getContextPath();
   }

   public int getMajorVersion() {
      return this.servletContext.getMajorVersion();
   }

   public int getMinorVersion() {
      return this.servletContext.getMinorVersion();
   }

   public int getEffectiveMajorVersion() {
      return this.servletContext.getEffectiveMajorVersion();
   }

   public int getEffectiveMinorVersion() {
      return this.servletContext.getEffectiveMinorVersion();
   }

   public String getMimeType(String file) {
      return this.servletContext.getMimeType(file);
   }

   public Set<String> getResourcePaths(String path) {
      return this.servletContext.getResourcePaths(path);
   }

   public URL getResource(String path) throws MalformedURLException {
      return this.servletContext.getResource(path);
   }

   public InputStream getResourceAsStream(String path) {
      return this.servletContext.getResourceAsStream(path);
   }

   public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.servletContext.getRequestDispatcher(path));
   }

   public Servlet getServlet(String name) throws ServletException {
      try {
         return ServletConverter.convert(this.servletContext.getServlet(name));
      } catch (javax.servlet.ServletException var3) {
         throw new ServletException(var3);
      }
   }

   public Enumeration<Servlet> getServlets() {
      final Enumeration<javax.servlet.Servlet> jakartaServlets = this.servletContext.getServlets();
      return new Enumeration<Servlet>() {
         public boolean hasMoreElements() {
            return jakartaServlets.hasMoreElements();
         }

         public Servlet nextElement() {
            return ServletConverter.convert((javax.servlet.Servlet)jakartaServlets.nextElement());
         }
      };
   }

   public Enumeration<String> getServletNames() {
      return this.servletContext.getServletNames();
   }

   public void log(String msg) {
      this.servletContext.log(msg);
   }

   public void log(Exception exception, String msg) {
      this.servletContext.log(exception, msg);
   }

   public void log(String message, Throwable throwable) {
      this.servletContext.log(message, throwable);
   }

   public String getRealPath(String path) {
      return this.servletContext.getRealPath(path);
   }

   public String getServerInfo() {
      return this.servletContext.getServerInfo();
   }

   public String getInitParameter(String name) {
      return this.servletContext.getInitParameter(name);
   }

   public Enumeration<String> getInitParameterNames() {
      return this.servletContext.getInitParameterNames();
   }

   public boolean setInitParameter(String name, String value) {
      return this.servletContext.setInitParameter(name, value);
   }

   public Object getAttribute(String name) {
      return this.servletContext.getAttribute(name);
   }

   public Enumeration<String> getAttributeNames() {
      return this.servletContext.getAttributeNames();
   }

   public void setAttribute(String name, Object object) {
      this.servletContext.setAttribute(name, object);
   }

   public void removeAttribute(String name) {
      this.servletContext.removeAttribute(name);
   }

   public String getServletContextName() {
      return this.servletContext.getServletContextName();
   }

   public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
      Servlet javaXservlet = null;

      try {
         Class servletClass = this.getClassLoader().loadClass(className);
         Constructor constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = (Servlet)constructor.newInstance();
      } catch (Exception var6) {
         throw new RuntimeException(var6);
      }

      return this.addServlet(servletName, javaXservlet);
   }

   public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
      return (jakarta.servlet.ServletRegistration.Dynamic)ServletConverter.convert((javax.servlet.Registration)this.servletContext.addServlet(servletName, ServletConverter.convert(servlet)));
   }

   public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
      Servlet javaXservlet = null;

      try {
         Constructor constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = (Servlet)constructor.newInstance();
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }

      return this.addServlet(servletName, javaXservlet);
   }

   public <T extends Servlet> T createServlet(Class<T> servletClass) throws ServletException {
      Servlet javaXservlet = null;

      try {
         Constructor constructor = servletClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXservlet = (Servlet)constructor.newInstance();
         return (T)javaXservlet;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public jakarta.servlet.ServletRegistration getServletRegistration(String servletName) {
      return (jakarta.servlet.ServletRegistration)ServletConverter.convert((javax.servlet.Registration)this.servletContext.getServletRegistration(servletName));
   }

   public Map<String, ? extends jakarta.servlet.ServletRegistration> getServletRegistrations() {
      Map<String, ? extends jakarta.servlet.ServletRegistration> origin = (Map<String, ? extends jakarta.servlet.ServletRegistration>) this.servletContext.getServletRegistrations();
      Map dest = new HashMap();
      Iterator iter = origin.entrySet().iterator();

      while(iter.hasNext()) {
         Map.Entry entry = (Map.Entry)iter.next();
         dest.put((String)entry.getKey(), ServletConverter.convert((jakarta.servlet.Registration)((jakarta.servlet.ServletRegistration)entry.getValue())));
      }

      return dest;
   }

   public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
      Filter javaXFilter = null;

      try {
         Class filterClass = this.getClassLoader().loadClass(className);
         Constructor constructor = filterClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXFilter = (Filter)constructor.newInstance();
      } catch (Exception var6) {
         throw new RuntimeException(var6);
      }

      return this.addFilter(filterName, javaXFilter);
   }

   public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
      return (jakarta.servlet.FilterRegistration.Dynamic)ServletConverter.convert((javax.servlet.Registration)this.servletContext.addFilter(filterName, ServletConverter.convert(filter)));
   }

   public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
      try {
         Filter filter = this.createFilter(filterClass);
         return (jakarta.servlet.FilterRegistration.Dynamic)ServletConverter.convert((javax.servlet.Registration)this.servletContext.addFilter(filterName, ServletConverter.convert(filter)));
      } catch (ServletException var4) {
         throw new RuntimeException(var4);
      }
   }

   public <T extends Filter> T createFilter(Class<T> filterClass) throws ServletException {
      Filter javaXFilter = null;

      try {
         Constructor constructor = filterClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         javaXFilter = (Filter)constructor.newInstance();
         return (T)javaXFilter;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public jakarta.servlet.FilterRegistration getFilterRegistration(String filterName) {
      return (jakarta.servlet.FilterRegistration)ServletConverter.convert((javax.servlet.Registration)this.servletContext.getFilterRegistration(filterName));
   }

   public Map<String, ? extends jakarta.servlet.FilterRegistration> getFilterRegistrations() {
      Map ori = this.servletContext.getFilterRegistrations();
      Map dest = new HashMap();
      Iterator iter = ori.keySet().iterator();

      while(iter.hasNext()) {
         Map.Entry entry = (Map.Entry)iter.next();
         dest.put(entry.getKey(), ServletConverter.convert((jakarta.servlet.Registration)((jakarta.servlet.FilterRegistration)entry.getValue())));
      }

      return dest;
   }

   public SessionCookieConfig getSessionCookieConfig() {
      return ServletConverter.convert(this.servletContext.getSessionCookieConfig());
   }

   public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
      Set<javax.servlet.SessionTrackingMode> resultTrackModes = new HashSet();
      /**
      sessionTrackingModes.forEach(new Consumer<SessionTrackingMode>() {
         @Override
         public void accept(SessionTrackingMode mode) {
            resultTrackModes.add(EnumsConverter.convert(mode));
         }
      });**/

        for(SessionTrackingMode mode : sessionTrackingModes) {
             resultTrackModes.add(EnumsConverter.convert(mode));
        }
      this.servletContext.setSessionTrackingModes(resultTrackModes);
   }

   public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
      Set<javax.servlet.SessionTrackingMode> sessionTrackModes = this.servletContext.getDefaultSessionTrackingModes();
      Set<SessionTrackingMode> resultTrackModes = new HashSet();
      /*sessionTrackModes.forEach((mode) -> {
         resultTrackModes.add(EnumsConverter.convert(mode));
      });*/

        for(javax.servlet.SessionTrackingMode mode : sessionTrackModes) {
            resultTrackModes.add(EnumsConverter.convert(mode));
        }
      return resultTrackModes;
   }

   public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
      Set<javax.servlet.SessionTrackingMode> sessionTrackModes = this.servletContext.getEffectiveSessionTrackingModes();
      Set<SessionTrackingMode> resultTrackModes = new HashSet();

      /*sessionTrackModes.forEach((mode) -> {
         resultTrackModes.add(EnumsConverter.convert(mode));
      });*/

     for(javax.servlet.SessionTrackingMode mode : sessionTrackModes) {
         resultTrackModes.add(EnumsConverter.convert(mode));
     }
      return resultTrackModes;
   }

   public void addListener(String className) {
      this.servletContext.addListener(className);
   }

   public <T extends EventListener> void addListener(T t) {
      this.servletContext.addListener(t);
   }

   public void addListener(Class<? extends EventListener> listenerClass) {
      this.servletContext.addListener(listenerClass);
   }

   public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
      try {
         return this.servletContext.createListener(c);
      } catch (javax.servlet.ServletException var3) {
         throw new ServletException(var3);
      }
   }

   public JspConfigDescriptor getJspConfigDescriptor() {
      return DescriptorConverter.convert(this.servletContext.getJspConfigDescriptor());
   }

   public ClassLoader getClassLoader() {
      return this.servletContext.getClassLoader();
   }

   public void declareRoles(String... roleNames) {
      this.servletContext.declareRoles(roleNames);
   }

   public jakarta.servlet.ServletRegistration.Dynamic addJspFile(String arg0, String arg1) {
      try {
         return new ServletRegistrationDynamic(this.servletContext.addJspFile(arg0, arg1));
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;

   }

   public ServletContext getContext(String arg0) {
      return new ServletContext(this.servletContext.getContext(arg0));
   }

   public jakarta.servlet.RequestDispatcher getNamedDispatcher(String name) {
      return new RequestDispatcher(this.servletContext.getNamedDispatcher(name));
   }

   public String getRequestCharacterEncoding() {
      try {
         return this.servletContext.getRequestCharacterEncoding();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public String getResponseCharacterEncoding() {
      try {
         return this.servletContext.getResponseCharacterEncoding();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public int getSessionTimeout() {
      try {
         return this.servletContext.getSessionTimeout();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return -1;
   }

   public String getVirtualServerName() {
      try {
         return this.servletContext.getVirtualServerName();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public void setRequestCharacterEncoding(String arg0) {
      this.servletContext.setRequestCharacterEncoding(arg0);
   }

   public void setResponseCharacterEncoding(String arg0) {
      this.setRequestCharacterEncoding(arg0);
   }

   public void setSessionTimeout(int arg0) {
      this.setSessionTimeout(arg0);
   }
}
