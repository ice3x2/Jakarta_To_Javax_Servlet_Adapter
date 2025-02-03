package adapter.jakarta.servlet;

import jakarta.servlet.DispatcherType;
import java.util.Collection;
import java.util.EnumSet;


public class FilterRegistration extends Registration implements jakarta.servlet.FilterRegistration {
   private final javax.servlet.FilterRegistration filterRegistration;


   public FilterRegistration(javax.servlet.FilterRegistration registration) {
      super(registration);
      this.filterRegistration = registration;
   }

   @SuppressWarnings("DuplicatedCode")
   @Override
   public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
      EnumSet<javax.servlet.DispatcherType> enumSet = EnumSet.allOf(javax.servlet.DispatcherType.class);
      enumSet.clear();

      for (DispatcherType type : dispatcherTypes) {
         enumSet.add(javax.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForServletNames(enumSet, isMatchAfter, servletNames);
   }
   @Override
   public Collection<String> getServletNameMappings() {
      return this.filterRegistration.getServletNameMappings();
   }

   @SuppressWarnings("DuplicatedCode")
   @Override
   public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
      EnumSet<javax.servlet.DispatcherType> enumSet = EnumSet.allOf(javax.servlet.DispatcherType.class);
      enumSet.clear();

      for (DispatcherType type : dispatcherTypes) {
         enumSet.add(javax.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForUrlPatterns(enumSet, isMatchAfter, urlPatterns);
   }
   @Override
   public Collection<String> getUrlPatternMappings() {
      return this.filterRegistration.getUrlPatternMappings();
   }
}
