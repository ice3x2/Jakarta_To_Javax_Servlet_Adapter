package adapter.javax.servlet;

import java.util.Collection;
import java.util.Set;

public class ServletRegistration extends Registration implements javax.servlet.ServletRegistration {
   jakarta.servlet.ServletRegistration servletRegistration;

   public ServletRegistration(jakarta.servlet.ServletRegistration servletRegistration) {
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
