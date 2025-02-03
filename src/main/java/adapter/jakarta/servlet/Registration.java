package adapter.jakarta.servlet;

import java.util.Map;
import java.util.Set;

public class Registration implements jakarta.servlet.Registration {
   private final javax.servlet.Registration registration;


   public Registration(javax.servlet.Registration registration) {
      this.registration = registration;
   }
   @Override
   public String getName() {
      return this.registration.getName();
   }
   @Override
   public String getClassName() {
      return this.registration.getClassName();
   }
   @Override
   public boolean setInitParameter(String name, String value) {
      return this.registration.setInitParameter(name, value);
   }
   @Override
   public String getInitParameter(String name) {
      return this.registration.getInitParameter(name);
   }
   @Override
   public Set<String> setInitParameters(Map<String, String> initParameters) {
      return this.registration.setInitParameters(initParameters);
   }
   @Override
   public Map<String, String> getInitParameters() {
      return this.registration.getInitParameters();
   }
}
