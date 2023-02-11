package adapter.javax.servlet;

public class FilterRegistrationDynamic extends FilterRegistration implements javax.servlet.FilterRegistration.Dynamic {
   jakarta.servlet.FilterRegistration.Dynamic dynamic;

   public FilterRegistrationDynamic(jakarta.servlet.FilterRegistration.Dynamic dynamic) {
      super(dynamic);
      this.dynamic = dynamic;
   }

   public void setAsyncSupported(boolean isAsyncSupported) {
      this.dynamic.setAsyncSupported(isAsyncSupported);
   }
}
