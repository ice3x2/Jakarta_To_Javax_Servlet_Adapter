package adapter.jakarta.servlet;

import java.util.Map;
import java.util.Set;

public class Registration implements jakarta.servlet.Registration {
   javax.servlet.Registration registration;

   public Registration(javax.servlet.Registration registration) {
      this.registration = registration;
   }

   public String getName() {
      return this.registration.getName();
   }

   public String getClassName() {
      return this.registration.getClassName();
   }

   public boolean setInitParameter(String name, String value) {
      return this.registration.setInitParameter(name, value);
   }

   public String getInitParameter(String name) {
      return this.registration.getInitParameter(name);
   }

   public Set<String> setInitParameters(Map<String, String> initParameters) {
      return this.registration.setInitParameters(initParameters);
   }

   public Map<String, String> getInitParameters() {
      return this.registration.getInitParameters();
   }
}
