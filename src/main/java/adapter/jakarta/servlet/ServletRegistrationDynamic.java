package adapter.jakarta.servlet;

import adapter.servletElementConverter.ServletConverter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletSecurityElement;
import java.util.Set;

 public class ServletRegistrationDynamic extends ServletRegistration implements jakarta.servlet.ServletRegistration.Dynamic {
   private final javax.servlet.ServletRegistration.Dynamic servletRegistrationDynamic;

   public ServletRegistrationDynamic(javax.servlet.ServletRegistration.Dynamic servletRegistrationDynamic) {
      super(servletRegistrationDynamic);
      this.servletRegistrationDynamic = servletRegistrationDynamic;
   }

   @Override public void setAsyncSupported(boolean isAsyncSupported) {
      this.servletRegistrationDynamic.setAsyncSupported(isAsyncSupported);
   }

   @Override public void setLoadOnStartup(int loadOnStartup) {
      this.servletRegistrationDynamic.setLoadOnStartup(loadOnStartup);
   }

   @Override public void setMultipartConfig(MultipartConfigElement multipartConfig) {
      this.servletRegistrationDynamic.setMultipartConfig(ServletConverter.convert(multipartConfig));
   }

   @Override public void setRunAsRole(String roleName) {
      this.servletRegistrationDynamic.setRunAsRole(roleName);
   }

   @Override public Set<String> setServletSecurity(ServletSecurityElement constraint) {
      return this.servletRegistrationDynamic.setServletSecurity(ServletConverter.convert(constraint));
   }
}
