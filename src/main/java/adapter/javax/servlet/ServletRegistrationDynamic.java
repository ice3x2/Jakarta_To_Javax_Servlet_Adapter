package adapter.javax.servlet;

import adapter.servletElementConverter.ServletConverter;
import java.util.Set;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletSecurityElement;

public class ServletRegistrationDynamic extends ServletRegistration implements javax.servlet.ServletRegistration.Dynamic {
   jakarta.servlet.ServletRegistration.Dynamic servletRegistrationDynamic;

   public ServletRegistrationDynamic(jakarta.servlet.ServletRegistration.Dynamic servletRegistrationDynamic) {
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
