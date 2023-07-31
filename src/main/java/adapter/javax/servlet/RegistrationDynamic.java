package adapter.javax.servlet;

public class RegistrationDynamic extends Registration implements javax.servlet.Registration.Dynamic {
   private final jakarta.servlet.Registration.Dynamic dynamic;

   public RegistrationDynamic(jakarta.servlet.Registration.Dynamic dynamic) {
      super(dynamic);
      this.dynamic = dynamic;
   }

   @Override public void setAsyncSupported(boolean isAsyncSupported) {
      this.dynamic.setAsyncSupported(isAsyncSupported);
   }
}
