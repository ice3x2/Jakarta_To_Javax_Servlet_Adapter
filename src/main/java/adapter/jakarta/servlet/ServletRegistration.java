package adapter.jakarta.servlet;

import java.util.Collection;
import java.util.Set;

public class ServletRegistration extends Registration implements jakarta.servlet.ServletRegistration {
   private final javax.servlet.ServletRegistration servletRegistration;

   public ServletRegistration(javax.servlet.ServletRegistration servletRegistration) {
      super(servletRegistration);
      this.servletRegistration = servletRegistration;
   }

   @Override
   public Set<String> addMapping(String... urlPatterns) {
      return this.servletRegistration.addMapping(urlPatterns);
   }
   @Override
   public Collection<String> getMappings() {
      return this.servletRegistration.getMappings();
   }
   @Override
   public String getRunAsRole() {
      return this.servletRegistration.getRunAsRole();
   }
}
