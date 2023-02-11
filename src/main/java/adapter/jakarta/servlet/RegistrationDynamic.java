package adapter.jakarta.servlet;

public class RegistrationDynamic extends Registration implements jakarta.servlet.Registration.Dynamic {
   javax.servlet.Registration.Dynamic dynamic;

   public RegistrationDynamic(javax.servlet.Registration.Dynamic dynamic) {
      super(dynamic);
      this.dynamic = dynamic;
   }

   public void setAsyncSupported(boolean isAsyncSupported) {
      this.dynamic.setAsyncSupported(isAsyncSupported);
   }
}
