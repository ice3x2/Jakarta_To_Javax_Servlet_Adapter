package adapter.servletElementConverter;

import javax.servlet.DispatcherType;

public class HttpComponentConverter {
   public HttpComponentConverter() {

   }

   public static DispatcherType convert(jakarta.servlet.DispatcherType type) {
      if (type == jakarta.servlet.DispatcherType.ASYNC) {
         return DispatcherType.ASYNC;
      } else if (type == jakarta.servlet.DispatcherType.ERROR) {
         return DispatcherType.ERROR;
      } else if (type == jakarta.servlet.DispatcherType.FORWARD) {
         return DispatcherType.FORWARD;
      } else if (type == jakarta.servlet.DispatcherType.INCLUDE) {
         return DispatcherType.INCLUDE;
      } else {
         return DispatcherType.REQUEST;
      }
   }
}
