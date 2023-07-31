package adapter.javax.servlet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import javax.servlet.DispatcherType;

 public class FilterRegistration extends Registration implements javax.servlet.FilterRegistration {
   private jakarta.servlet.FilterRegistration filterRegistration;

    public FilterRegistration(jakarta.servlet.FilterRegistration registration) {
      super(registration);
      this.filterRegistration = registration;
   }

   @Override public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
      EnumSet<jakarta.servlet.DispatcherType> enumSet = EnumSet.allOf(jakarta.servlet.DispatcherType.class);
      enumSet.clear();

      for (DispatcherType type : dispatcherTypes) {
         enumSet.add(jakarta.servlet.DispatcherType.valueOf(type.name()));
      }
      this.filterRegistration.addMappingForServletNames(enumSet, isMatchAfter, servletNames);
   }

   @Override public Collection<String> getServletNameMappings() {
      return this.filterRegistration.getServletNameMappings();
   }

   @Override public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
      EnumSet<jakarta.servlet.DispatcherType> enumSet = EnumSet.allOf(jakarta.servlet.DispatcherType.class);
      enumSet.clear();

      for (DispatcherType type : dispatcherTypes) {
         enumSet.add(jakarta.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForUrlPatterns(enumSet, isMatchAfter, urlPatterns);
   }

   @Override public Collection<String> getUrlPatternMappings() {
      return this.filterRegistration.getUrlPatternMappings();
   }
}
