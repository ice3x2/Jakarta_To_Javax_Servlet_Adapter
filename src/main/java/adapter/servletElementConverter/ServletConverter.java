package adapter.servletElementConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;







@SuppressWarnings("DuplicatedCode")
public class ServletConverter {
   public ServletConverter() {
   }

   public static javax.servlet.Servlet convert(final jakarta.servlet.Servlet servlet) {
       //noinspection DuplicatedCode
       return new javax.servlet.Servlet() {
         public void init(javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
            try {
               servlet.init(ServletConverter.convert(config));
            } catch (jakarta.servlet.ServletException e) {
               throw new javax.servlet.ServletException(e);
            }
         }

         public javax.servlet.ServletConfig getServletConfig() {
            return ServletConverter.convert(servlet.getServletConfig());
         }

         public void service(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) throws javax.servlet.ServletException, IOException {
            try {
               servlet.service(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res));
            } catch (jakarta.servlet.ServletException e) {
               throw new javax.servlet.ServletException(e);
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

   public static jakarta.servlet.Servlet convert(final javax.servlet.Servlet servlet) {
       //noinspection DuplicatedCode
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
            } catch (javax.servlet.ServletException e) {
               throw new jakarta.servlet.ServletException(e);
            }
         }

         public void service(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse request) throws jakarta.servlet.ServletException, IOException {
            try {
               servlet.service(ServletReqResConverter.convert(req), ServletReqResConverter.convert(request));
            } catch (javax.servlet.ServletException e) {
               throw new jakarta.servlet.ServletException(e);
            }
         }
      };
   }



   public static jakarta.servlet.Registration convert(javax.servlet.Registration ori) {
      if (ori instanceof javax.servlet.ServletRegistration.Dynamic) {
         return new adapter.jakarta.servlet.ServletRegistrationDynamic((javax.servlet.ServletRegistration.Dynamic)ori);
      } else if (ori instanceof javax.servlet.ServletRegistration) {
         return new adapter.jakarta.servlet.ServletRegistration((javax.servlet.ServletRegistration)ori);
      } else if (ori instanceof javax.servlet.FilterRegistration.Dynamic) {
         return new adapter.jakarta.servlet.FilterRegistrationDynamic((javax.servlet.FilterRegistration.Dynamic)ori);
      } else if (ori instanceof javax.servlet.FilterRegistration) {
         return new adapter.jakarta.servlet.FilterRegistration((javax.servlet.FilterRegistration)ori);
      } else {
         return ori instanceof javax.servlet.Registration.Dynamic ? new adapter.jakarta.servlet.RegistrationDynamic((javax.servlet.Registration.Dynamic)ori) : new adapter.jakarta.servlet.Registration(ori);
      }
   }


   public static adapter.jakarta.servlet.ServletRegistrationDynamic convert(javax.servlet.ServletRegistration.Dynamic ori) {
      return new adapter.jakarta.servlet.ServletRegistrationDynamic(ori);
   }

   public static adapter.jakarta.servlet.ServletRegistration convert(javax.servlet.ServletRegistration ori) {
      return new adapter.jakarta.servlet.ServletRegistration(ori);
   }

   public static adapter.jakarta.servlet.FilterRegistrationDynamic convert(javax.servlet.FilterRegistration.Dynamic ori) {
      return new adapter.jakarta.servlet.FilterRegistrationDynamic(ori);
   }

   public static adapter.jakarta.servlet.FilterRegistration convert(javax.servlet.FilterRegistration ori) {
      return new adapter.jakarta.servlet.FilterRegistration(ori);
   }

   public static adapter.jakarta.servlet.RegistrationDynamic convert(javax.servlet.Registration.Dynamic ori) {
      return new adapter.jakarta.servlet.RegistrationDynamic(ori);
   }

   public static javax.servlet.Registration convert(jakarta.servlet.Registration ori) {
      if (ori instanceof jakarta.servlet.ServletRegistration.Dynamic) {
         return new adapter.javax.servlet.ServletRegistrationDynamic((jakarta.servlet.ServletRegistration.Dynamic)ori);
      } else if (ori instanceof jakarta.servlet.ServletRegistration) {
         return new adapter.javax.servlet.ServletRegistration((jakarta.servlet.ServletRegistration)ori);
      } else if (ori instanceof jakarta.servlet.FilterRegistration.Dynamic) {
         return new adapter.javax.servlet.FilterRegistrationDynamic((jakarta.servlet.FilterRegistration.Dynamic)ori);
      } else if (ori instanceof jakarta.servlet.FilterRegistration) {
         return new adapter.javax.servlet.FilterRegistration((jakarta.servlet.FilterRegistration)ori);
      } else {
         return ori instanceof jakarta.servlet.Registration.Dynamic ? new adapter.javax.servlet.RegistrationDynamic((jakarta.servlet.Registration.Dynamic)ori) : new adapter.javax.servlet.Registration(ori);
      }
   }

   public static javax.servlet.ServletRegistration convert(jakarta.servlet.ServletRegistration.Dynamic ori) {
      return new adapter.javax.servlet.ServletRegistrationDynamic(ori);
   }

   public static javax.servlet.ServletRegistration convert(jakarta.servlet.ServletRegistration ori) {
      return new adapter.javax.servlet.ServletRegistration(ori);
   }

   public static javax.servlet.FilterRegistration convert(jakarta.servlet.FilterRegistration.Dynamic ori) {
      return new adapter.javax.servlet.FilterRegistrationDynamic(ori);
   }

   public static javax.servlet.FilterRegistration convert(jakarta.servlet.FilterRegistration ori) {
      return new adapter.javax.servlet.FilterRegistration(ori);
   }



   public static javax.servlet.Filter convert(final jakarta.servlet.Filter filter) {
      return new javax.servlet.Filter() {
         public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
            try {
               filter.init(ServletConverter.convert(filterConfig));
            } catch (jakarta.servlet.ServletException e) {
               throw new javax.servlet.ServletException(e);
            }
         }

         public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, javax.servlet.FilterChain chain) throws IOException, javax.servlet.ServletException {
            try {
               filter.doFilter(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response), ServletConverter.convert(chain));
            } catch (jakarta.servlet.ServletException var6) {
               throw new javax.servlet.ServletException(var6);
            }
         }

         public void destroy() {
            filter.destroy();
         }
      };
   }

   public static jakarta.servlet.Filter convert(final javax.servlet.Filter filter) {
      return new jakarta.servlet.Filter() {
         public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
            try {
               filter.doFilter(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res), ServletConverter.convert(chain));
            } catch (javax.servlet.ServletException var6) {
               throw new jakarta.servlet.ServletException(var6);
            }
         }

         public void destroy() {
            filter.destroy();
         }

         public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
            try {
               filter.init(ServletConverter.convert(filterConfig));
            } catch (javax.servlet.ServletException e) {
               throw new jakarta.servlet.ServletException(e);
            }
         }
      };
   }

   public static javax.servlet.FilterConfig convert(final jakarta.servlet.FilterConfig filterConfig) {
      return new javax.servlet.FilterConfig() {
         public String getFilterName() {
            return filterConfig.getFilterName();
         }

         public javax.servlet.ServletContext getServletContext() {
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

   public static jakarta.servlet.FilterConfig convert(final javax.servlet.FilterConfig filterConfig) {
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

   public static jakarta.servlet.FilterChain convert(final javax.servlet.FilterChain filterChain) {
      return (req, res) -> {
         try {
            filterChain.doFilter(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res));
         } catch (javax.servlet.ServletException e) {
            throw new jakarta.servlet.ServletException(e);
         }
      };
   }

   public static javax.servlet.FilterChain convert(final jakarta.servlet.FilterChain filterChain) {
      return (req, res) -> {
         try {
            filterChain.doFilter(ServletReqResConverter.convert(req), ServletReqResConverter.convert(res));
         } catch (jakarta.servlet.ServletException e) {
            throw new javax.servlet.ServletException(e);
         }
      };
   }

   public static jakarta.servlet.ServletConfig convert(final javax.servlet.ServletConfig config) {
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

   public static javax.servlet.ServletConfig convert(final jakarta.servlet.ServletConfig config) {
      return new javax.servlet.ServletConfig() {
         public String getServletName() {
            return config.getServletName();
         }

         public javax.servlet.ServletContext getServletContext() {
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

   public static javax.servlet.SessionCookieConfig convert(final jakarta.servlet.SessionCookieConfig ori) {
      return new javax.servlet.SessionCookieConfig() {
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

   public static jakarta.servlet.SessionCookieConfig convert(final javax.servlet.SessionCookieConfig ori) {
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

   public static javax.servlet.MultipartConfigElement convert(jakarta.servlet.MultipartConfigElement ori) {
      return new javax.servlet.MultipartConfigElement(ori.getLocation(), ori.getMaxFileSize(), ori.getMaxRequestSize(), ori.getFileSizeThreshold());
   }

   public static jakarta.servlet.MultipartConfigElement convert(javax.servlet.MultipartConfigElement ori) {
      return new jakarta.servlet.MultipartConfigElement(ori.getLocation(), ori.getMaxFileSize(), ori.getMaxRequestSize(), ori.getFileSizeThreshold());
   }

   public static javax.servlet.ServletSecurityElement convert(final jakarta.servlet.ServletSecurityElement ori) {
      return new javax.servlet.ServletSecurityElement() {
         public Collection<javax.servlet.HttpMethodConstraintElement> getHttpMethodConstraints() {
            Collection<jakarta.servlet.HttpMethodConstraintElement> collectionsOri = ori.getHttpMethodConstraints();
            ArrayList<javax.servlet.HttpMethodConstraintElement> collectionsDest = new ArrayList<>();

            for (jakarta.servlet.HttpMethodConstraintElement item : collectionsOri) {
               collectionsDest.add(ServletConverter.convert(item));
            }
            return collectionsDest;
         }

         public Collection<String> getMethodNames() {
            return ori.getMethodNames();
         }

         public javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public javax.servlet.annotation.ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return javax.servlet.annotation.ServletSecurity.TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }
      };
   }

   public static jakarta.servlet.ServletSecurityElement convert(final javax.servlet.ServletSecurityElement ori) {
      return new jakarta.servlet.ServletSecurityElement() {
         public Collection<jakarta.servlet.HttpMethodConstraintElement> getHttpMethodConstraints() {
            Collection<javax.servlet.HttpMethodConstraintElement> collectionsOri = ori.getHttpMethodConstraints();
            ArrayList<jakarta.servlet.HttpMethodConstraintElement> collectionsDest = new ArrayList<>();

            for (javax.servlet.HttpMethodConstraintElement item : collectionsOri) {
               collectionsDest.add(ServletConverter.convert(item));
            }
            return collectionsDest;
         }

         public Collection<String> getMethodNames() {
            return ori.getMethodNames();
         }

         public jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public jakarta.servlet.annotation.ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return jakarta.servlet.annotation.ServletSecurity.TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }
      };
   }

   public static javax.servlet.HttpMethodConstraintElement convert(final jakarta.servlet.HttpMethodConstraintElement ori) {
      return new javax.servlet.HttpMethodConstraintElement(ori.getMethodName()) {
         public javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public javax.servlet.annotation.ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return javax.servlet.annotation.ServletSecurity.TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
         }

         public String[] getRolesAllowed() {
            return ori.getRolesAllowed();
         }

         public String getMethodName() {
            return ori.getMethodName();
         }
      };
   }

   public static jakarta.servlet.HttpMethodConstraintElement convert(final javax.servlet.HttpMethodConstraintElement ori) {
      return new jakarta.servlet.HttpMethodConstraintElement(ori.getMethodName()) {
         public jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
            return jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic.valueOf(ori.getEmptyRoleSemantic().name());
         }

         public jakarta.servlet.annotation.ServletSecurity.TransportGuarantee getTransportGuarantee() {
            return jakarta.servlet.annotation.ServletSecurity.TransportGuarantee.valueOf(ori.getTransportGuarantee().name());
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
