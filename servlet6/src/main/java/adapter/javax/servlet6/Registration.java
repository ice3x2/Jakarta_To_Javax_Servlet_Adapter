package adapter.javax.servlet6;

import adapter.common.AdapterUnwrap;

import java.util.Map;
import java.util.Set;
 public class Registration implements javax.servlet.Registration, AdapterUnwrap<jakarta.servlet.Registration> {
   private jakarta.servlet.Registration registration;

   public Registration(jakarta.servlet.Registration registration) {
      this.registration = registration;
   }

   // @req FR-CONV-003
   @Override public jakarta.servlet.Registration unwrap() {
      return this.registration;
   }

   // @req FR-CONV-004
   @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null) return false;
      Object other = (o instanceof AdapterUnwrap<?>) ? ((AdapterUnwrap<?>) o).unwrap() : o;
      return this.unwrap().equals(other);
   }

   // @req FR-CONV-004
   @Override public int hashCode() {
      return this.unwrap().hashCode();
   }

   @Override public String getName() {
      return this.registration.getName();
   }

   @Override public String getClassName() {
      return this.registration.getClassName();
   }

   @Override public boolean setInitParameter(String name, String value) {
      return this.registration.setInitParameter(name, value);
   }

   @Override public String getInitParameter(String name) {
      return this.registration.getInitParameter(name);
   }

   @Override public Set<String> setInitParameters(Map<String, String> initParameters) {
      return this.registration.setInitParameters(initParameters);
   }

   @Override public Map<String, String> getInitParameters() {
      return this.registration.getInitParameters();
   }
}
