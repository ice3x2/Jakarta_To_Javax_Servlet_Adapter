package adapter.jakarta.servlet;

import java.util.Collection;
import java.util.Set;

public class ServletRegistration extends Registration implements jakarta.servlet.ServletRegistration {
   javax.servlet.ServletRegistration servletRegistration;

   public ServletRegistration(javax.servlet.ServletRegistration servletRegistration) {
      super(servletRegistration);
      this.servletRegistration = servletRegistration;
   }

   public Set<String> addMapping(String... urlPatterns) {
      return this.servletRegistration.addMapping(urlPatterns);
   }

   public Collection<String> getMappings() {
      return this.servletRegistration.getMappings();
   }

   public String getRunAsRole() {
      return this.servletRegistration.getRunAsRole();
   }
}
