package adapter.javax.servlet;

public class RegistrationDynamic extends Registration implements javax.servlet.Registration.Dynamic {
   jakarta.servlet.Registration.Dynamic dynamic;

   public RegistrationDynamic(jakarta.servlet.Registration.Dynamic dynamic) {
      super(dynamic);
      this.dynamic = dynamic;
   }

   public void setAsyncSupported(boolean isAsyncSupported) {
      this.dynamic.setAsyncSupported(isAsyncSupported);
   }
}
