package adapter.jakarta.servlet;

public class FilterRegistrationDynamic extends FilterRegistration implements jakarta.servlet.FilterRegistration.Dynamic {
   private final javax.servlet.FilterRegistration.Dynamic dynamic;


   public FilterRegistrationDynamic(javax.servlet.FilterRegistration.Dynamic dynamic) {
      super(dynamic);
      this.dynamic = dynamic;
   }
   @Override
   public void setAsyncSupported(boolean isAsyncSupported) {
      this.dynamic.setAsyncSupported(isAsyncSupported);
   }
}
