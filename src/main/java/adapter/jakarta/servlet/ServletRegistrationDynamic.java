package adapter.jakarta.servlet;

import adapter.servletElementConverter.ServletConverter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletSecurityElement;
import java.util.Set;

public class ServletRegistrationDynamic extends ServletRegistration implements jakarta.servlet.ServletRegistration.Dynamic {
   javax.servlet.ServletRegistration.Dynamic servletRegistrationDynamic;

   public ServletRegistrationDynamic(javax.servlet.ServletRegistration.Dynamic servletRegistrationDynamic) {
      super(servletRegistrationDynamic);
      this.servletRegistrationDynamic = servletRegistrationDynamic;
   }

   public void setAsyncSupported(boolean isAsyncSupported) {
      this.servletRegistrationDynamic.setAsyncSupported(isAsyncSupported);
   }

   public void setLoadOnStartup(int loadOnStartup) {
      this.servletRegistrationDynamic.setLoadOnStartup(loadOnStartup);
   }

   public void setMultipartConfig(MultipartConfigElement multipartConfig) {
      this.servletRegistrationDynamic.setMultipartConfig(ServletConverter.convert(multipartConfig));
   }

   public void setRunAsRole(String roleName) {
      this.servletRegistrationDynamic.setRunAsRole(roleName);
   }

   public Set<String> setServletSecurity(ServletSecurityElement constraint) {
      return this.servletRegistrationDynamic.setServletSecurity(ServletConverter.convert(constraint));
   }
}
