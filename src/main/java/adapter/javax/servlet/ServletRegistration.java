package adapter.javax.servlet;

import java.util.Collection;
import java.util.Set;

 public class ServletRegistration extends Registration implements javax.servlet.ServletRegistration {
   private final jakarta.servlet.ServletRegistration servletRegistration;

    public ServletRegistration(jakarta.servlet.ServletRegistration servletRegistration) {
      super(servletRegistration);
      this.servletRegistration = servletRegistration;
   }

   @Override public Set<String> addMapping(String... urlPatterns) {
      return this.servletRegistration.addMapping(urlPatterns);
   }

   @Override public Collection<String> getMappings() {
      return this.servletRegistration.getMappings();
   }

   @Override public String getRunAsRole() {
      return this.servletRegistration.getRunAsRole();
   }
}
