package adapter.jakarta.servlet;

import jakarta.servlet.DispatcherType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

public class FilterRegistration extends Registration implements jakarta.servlet.FilterRegistration {
   javax.servlet.FilterRegistration filterRegistration;

   public FilterRegistration(javax.servlet.FilterRegistration registration) {
      super(registration);
      this.filterRegistration = registration;
   }

   public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
      EnumSet<javax.servlet.DispatcherType> enumSet = EnumSet.allOf(javax.servlet.DispatcherType.class);
      enumSet.clear();
      Iterator var6 = dispatcherTypes.iterator();

      while(var6.hasNext()) {
         DispatcherType type = (DispatcherType)var6.next();
         enumSet.add(javax.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForServletNames(enumSet, isMatchAfter, servletNames);
   }

   public Collection<String> getServletNameMappings() {
      return this.filterRegistration.getServletNameMappings();
   }

   public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
      EnumSet<javax.servlet.DispatcherType> enumSet = EnumSet.allOf(javax.servlet.DispatcherType.class);
      enumSet.clear();
      Iterator var6 = dispatcherTypes.iterator();

      while(var6.hasNext()) {
         DispatcherType type = (DispatcherType)var6.next();
         enumSet.add(javax.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForUrlPatterns(enumSet, isMatchAfter, urlPatterns);
   }

   public Collection<String> getUrlPatternMappings() {
      return this.filterRegistration.getUrlPatternMappings();
   }
}
