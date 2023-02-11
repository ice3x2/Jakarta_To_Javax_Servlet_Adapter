package adapter.servletElementConverter;

import adapter.jakarta.servlet.FilterRegistrationDynamic;
import adapter.jakarta.servlet.RegistrationDynamic;
import adapter.jakarta.servlet.ServletRegistrationDynamic;
import jakarta.servlet.Registration;
import jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import jakarta.servlet.annotation.ServletSecurity.TransportGuarantee;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SessionCookieConfig;
import javax.servlet.annotation.ServletSecurity;

public class ServletConverter {
   public ServletConverter() {
   }

   public static Servlet convert(final jakarta.servlet.Servlet servlet) {
      return new Servlet() {
         public void init(ServletConfig config) throws ServletException {
            try {
               servlet.init(ServletConverter.convert(config));
            } catch (jakarta.servlet.ServletException var3) {
               throw new ServletException(var3);
            }
         }

         public ServletConfig getServletConfig() {
            return ServletConverter.convert(servlet.getServletConfig());
         }

         public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            try {
               servlet.service(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res));
            } catch (jakarta.servlet.ServletException var4) {
               throw new ServletException(var4);
            } catch (IOException var5) {
               throw var5;
            }
         }

         public String getServletInfo() {
            return servlet.getServletInfo();
         }

         public void destroy() {
            servlet.destroy();
         }
      };
   }

   public static jakarta.servlet.Servlet convert(final Servlet servlet) {
      return new jakarta.servlet.Servlet() {
         public void destroy() {
            servlet.destroy();
         }

         public jakarta.servlet.ServletConfig getServletConfig() {
            return ServletConverter.convert(servlet.getServletConfig());
         }

         public String getServletInfo() {
            return servlet.getServletInfo();
         }

         public void init(jakarta.servlet.ServletConfig config) throws jakarta.servlet.ServletException {
            try {
               servlet.init(ServletConverter.convert(config));
            } catch (ServletException var3) {
               throw new jakarta.servlet.ServletException(var3);
            }
         }

         public void service(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse request) throws jakarta.servlet.ServletException, IOException {
            try {
               servlet.service(ServletReqResConverter.convert(req), ServletReqResConverter.convert(request));
            } catch (ServletException var4) {
               throw new jakarta.servlet.ServletException(var4);
            } catch (IOException var5) {
               throw var5;
            }
         }
      };
   }

   public static Registration convert(javax.servlet.Registration ori) {
      if (ori instanceof ServletRegistration.Dynamic) {
         return new ServletRegistrationDynamic((ServletRegistration.Dynamic)ori);
      } else if (ori instanceof ServletRegistration) {
         return new adapter.jakarta.servlet.ServletRegistration((ServletRegistration)ori);
      } else if (ori instanceof FilterRegistration.Dynamic) {
         return new FilterRegistrationDynamic((FilterRegistration.Dynamic)ori);
      } else if (ori instanceof FilterRegistration) {
         return new adapter.jakarta.servlet.FilterRegistration((FilterRegistration)ori);
      } else {
         return (Registration)(ori instanceof javax.servlet.Registration.Dynamic ? new RegistrationDynamic((ServletRegistration.Dynamic)ori) : new adapter.jakarta.servlet.Registration(ori));
      }
   }

   public static javax.servlet.Registration convert(Registration ori) {
      if (ori instanceof jakarta.servlet.ServletRegistration.Dynamic) {
         return new adapter.javax.servlet.ServletRegistrationDynamic((jakarta.servlet.ServletRegistration.Dynamic)ori);
      } else if (ori instanceof jakarta.servlet.ServletRegistration) {
         return new adapter.javax.servlet.ServletRegistration((jakarta.servlet.ServletRegistration)ori);
      } else if (ori instanceof jakarta.servlet.FilterRegistration.Dynamic) {
         return new adapter.javax.servlet.FilterRegistrationDynamic((jakarta.servlet.FilterRegistration.Dynamic)ori);
      } else if (ori instanceof jakarta.servlet.FilterRegistration) {
         return new adapter.javax.servlet.FilterRegistration((jakarta.servlet.FilterRegistration)ori);
      } else {
         return (javax.servlet.Registration)(ori instanceof Registration.Dynamic ? new adapter.javax.servlet.RegistrationDynamic((jakarta.servlet.ServletRegistration.Dynamic)ori) : new adapter.javax.servlet.Registration(ori));
      }
   }

   public static Filter convert(final jakarta.servlet.Filter filter) {
      return new Filter() {
         public void init(FilterConfig filterConfig) throws ServletException {
            try {
               filter.init(ServletConverter.convert(filterConfig));
            } catch (jakarta.servlet.ServletException var3) {
               throw new ServletException(var3);
            }
         }

         public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            try {
               filter.doFilter(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response), ServletConverter.convert(chain));
            } catch (IOException var5) {
               throw var5;
            } catch (jakarta.servlet.ServletException var6) {
               throw new ServletException(var6);
            }
         }

         public void destroy() {
            filter.destroy();
         }
      };
   }

   public static jakarta.servlet.Filter convert(final Filter filter) {
      return new jakarta.servlet.Filter() {
         public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
            try {
               filter.doFilter(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res), ServletConverter.convert(chain));
            } catch (IOException var5) {
               throw var5;
            } catch (ServletException var6) {
               throw new jakarta.servlet.ServletException(var6);
            }
         }

         public void destroy() {
            filter.destroy();
         }

         public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
            try {
               filter.init(ServletConverter.convert(filterConfig));
            } catch (ServletException var3) {
               throw new jakarta.servlet.ServletException(var3);
            }
         }
      };
   }

   public static FilterConfig convert(final jakarta.servlet.FilterConfig filterConfig) {
      return new FilterConfig() {
         public String getFilterName() {
            return filterConfig.getFilterName();
         }

         public ServletContext getServletContext() {
            return new adapter.javax.servlet.ServletContext(filterConfig.getServletContext());
         }

         public String getInitParameter(String name) {
            return filterConfig.getInitParameter(name);
         }

         public Enumeration<String> getInitParameterNames() {
            return filterConfig.getInitParameterNames();
         }
      };
   }

   public static jakarta.servlet.FilterConfig convert(final FilterConfig filterConfig) {
      return new jakarta.servlet.FilterConfig() {
         public String getFilterName() {
            return filterConfig.getFilterName();
         }

         public jakarta.servlet.ServletContext getServletContext() {
            return new adapter.jakarta.servlet.ServletContext(filterConfig.getServletContext());
         }

         public String getInitParameter(String name) {
            return filterConfig.getInitParameter(name);
         }

         public Enumeration<String> getInitParameterNames() {
            return filterConfig.getInitParameterNames();
         }
      };
   }

   public static jakarta.servlet.FilterChain convert(final FilterChain filterChain) {
      return new jakarta.servlet.FilterChain() {
         public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException, jakarta.servlet.ServletException {
            try {
               filterChain.doFilter(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res));
            } catch (IOException var4) {
               throw var4;
            } catch (ServletException var5) {
               throw new jakarta.servlet.ServletException(var5);
            }
         }
      };
   }

   public static FilterChain convert(final jakarta.servlet.FilterChain filterChain) {
      return new FilterChain() {
         public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
            try {
               filterChain.doFilter(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res));
            } catch (IOException var4) {
               throw var4;
            } catch (jakarta.servlet.ServletException var5) {
               throw new ServletException(var5);
            }
         }
      };
   }

   public static jakarta.servlet.ServletConfig convert(final ServletConfig config) {
      return new jakarta.servlet.ServletConfig() {
         public String getServletName() {
            return config.getServletName();
         }

         public jakarta.servlet.ServletContext getServletContext() {
            return new adapter.jakarta.servlet.ServletContext(config.getServletContext());
         }

         public Enumeration<String> getInitParameterNames() {
            return config.getInitParameterNames();
         }

         public String getInitParameter(String name) {
            return config.getInitParameter(name);
         }
      };
   }

   public static ServletConfig convert(final jakarta.servlet.ServletConfig config) {
      return new ServletConfig() {
         public String getServletName() {
            return config.getServletName();
         }

         public ServletContext getServletContext() {
            return new adapter.javax.servlet.ServletContext(config.getServletContext());
         }

         public Enumeration<String> getInitParameterNames() {
            return config.getInitParameterNames();
         }

         public String getInitParameter(String name) {
            return config.getInitParameter(name);
         }
      };
   }

   public static SessionCookieConfig convert(final jakarta.servlet.SessionCookieConfig ori) {
      return new SessionCookieConfig() {
         public void setName(String name) {
            ori.setName(name);
         }

         public String getName() {
            return ori.getName();
         }

         public void setDomain(String domain) {
            ori.setDomain(domain);
         }

         public String getDomain() {
            return ori.getDomain();
         }

         public void setPath(String path) {
            ori.setPath(path);
         }

         public String getPath() {
            return ori.getPath();
         }

         public void setComment(String comment) {
            ori.setComment(comment);
         }

         public String getComment() {
            return ori.getComment();
         }

         public void setHttpOnly(boolean httpOnly) {
            ori.setHttpOnly(httpOnly);
         }

         public boolean isHttpOnly() {
            return ori.isHttpOnly();
         }

         public void setSecure(boolean secure) {
            ori.setSecure(secure);
         }

         public boolean isSecure() {
            return ori.isSecure();
         }

         public void setMaxAge(int MaxAge) {
            ori.setMaxAge(MaxAge);
         }

         public int getMaxAge() {
            return ori.getMaxAge();
         }
      };
   }

   public static jakarta.servlet.SessionCookieConfig convert(final SessionCookieConfig ori) {
      return new jakarta.servlet.SessionCookieConfig() {
         public void setName(String name) {
            ori.setName(name);
         }

         public String getName() {
            return ori.getName();
         }

         public void setDomain(String domain) {
            ori.setDomain(domain);
         }

         public String getDomain() {
            return ori.getDomain();
         }

         public void setPath(String path) {
            ori.setPath(path);
         }

         public String getPath() {
            return ori.getPath();
         }

         public void setComment(String comment) {
            ori.setComment(comment);
         }

         public String getComment() {
            return ori.getComment();
         }

         public void setHttpOnly(boolean httpOnly) {
            ori.setHttpOnly(httpOnly);
         }

         public boolean isHttpOnly() {
            return ori.isHttpOnly();
         }

         public void setSecure(boolean secure) {
            ori.setSecure(secure);
         }

         public boolean isSecure() {
            return ori.isSecure();
         }

         public void setMaxAge(int MaxAge) {
            ori.setMaxAge(MaxAge);
         }

         public int getMaxAge() {
            return ori.getMaxAge();
         }
      };
   }

   public static MultipartConfigElement convert(jakarta.servlet.MultipartConfigElement ori) {
      return new MultipartConfigElement(ori.getLocation(), ori.getMaxFileSize(), ori.getMaxRequestSize(), ori.getFileSizeThreshold());
   }

   public static jakarta.servlet.MultipartConfigElement convert(MultipartConfigElement ori) {
      return new jakarta.servlet.MultipartConfigElement(ori.getLocation(), ori.getMaxFileSize(), ori.getMaxRequestSize(), ori.getFileSizeThreshold());
   }

   public static ServletSecurityElement convert(final jakarta.servlet.ServletSecurityElement ori) {
      return new ServletSecurityElement() {
         public Collection<HttpMethodConstraintElement> getHttpMethodConstraints() {
            Collection<jakarta.servlet.HttpMethodConstraintElement> collectionsOri = ori.getHttpMethodConstraints();
            ArrayList<HttpMethodConstraintElement> collectionsDest = new ArrayList();

            for (jakarta.servlet.HttpMethodConstraintElement item : collectionsOri) {
               collectionsDest.add(ServletConverter.convert(item));
            }
            return collectionsDest;
         }

         public Collection<String> getMethodNames() {
            return ori.getMethodNames();
         }

         public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return ServletSecurity.EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return ServletSecurity.TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }
      };
   }

   public static jakarta.servlet.ServletSecurityElement convert(final ServletSecurityElement ori) {
      return new jakarta.servlet.ServletSecurityElement() {
         public Collection<jakarta.servlet.HttpMethodConstraintElement> getHttpMethodConstraints() {
            Collection<HttpMethodConstraintElement> collectionsOri = ori.getHttpMethodConstraints();
            ArrayList<jakarta.servlet.HttpMethodConstraintElement> collectionsDest = new ArrayList();
            /*collectionsOri.forEach((item) -> {
               collectionsDest.add(ServletConverter.convert(item));
            });*/

            for (HttpMethodConstraintElement item : collectionsOri) {
               collectionsDest.add(ServletConverter.convert(item));
            }
            return collectionsDest;
         }

         public Collection<String> getMethodNames() {
            return ori.getMethodNames();
         }

         public jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public jakarta.servlet.annotation.ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }
      };
   }

   public static HttpMethodConstraintElement convert(final jakarta.servlet.HttpMethodConstraintElement ori) {
      return new HttpMethodConstraintElement(ori.getMethodName()) {
         public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return ServletSecurity.EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return ServletSecurity.TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }

         public String getMethodName() {
            return ori.getMethodName();
         }
      };
   }

   public static jakarta.servlet.HttpMethodConstraintElement convert(final HttpMethodConstraintElement ori) {
      return new jakarta.servlet.HttpMethodConstraintElement(ori.getMethodName()) {
         public jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public jakarta.servlet.annotation.ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }

         public String getMethodName() {
            return ori.getMethodName();
         }
      };
   }
}
