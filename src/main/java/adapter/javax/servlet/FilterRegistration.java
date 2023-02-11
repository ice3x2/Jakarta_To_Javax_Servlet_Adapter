package adapter.javax.servlet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import javax.servlet.DispatcherType;

public class FilterRegistration extends Registration implements javax.servlet.FilterRegistration {
   jakarta.servlet.FilterRegistration filterRegistration;

   public FilterRegistration(jakarta.servlet.FilterRegistration registration) {
      super(registration);
      this.filterRegistration = registration;
   }

   public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
      EnumSet<jakarta.servlet.DispatcherType> enumSet = EnumSet.allOf(jakarta.servlet.DispatcherType.class);
      enumSet.clear();
      Iterator var6 = dispatcherTypes.iterator();

      while(var6.hasNext()) {
         DispatcherType type = (DispatcherType)var6.next();
         enumSet.add(jakarta.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForServletNames(enumSet, isMatchAfter, servletNames);
   }

   public Collection<String> getServletNameMappings() {
      return this.filterRegistration.getServletNameMappings();
   }

   public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
      EnumSet<jakarta.servlet.DispatcherType> enumSet = EnumSet.allOf(jakarta.servlet.DispatcherType.class);
      enumSet.clear();
      Iterator var6 = dispatcherTypes.iterator();

      while(var6.hasNext()) {
         DispatcherType type = (DispatcherType)var6.next();
         enumSet.add(jakarta.servlet.DispatcherType.valueOf(type.name()));
      }

      this.filterRegistration.addMappingForUrlPatterns(enumSet, isMatchAfter, urlPatterns);
   }

   public Collection<String> getUrlPatternMappings() {
      return this.filterRegistration.getUrlPatternMappings();
   }
}
