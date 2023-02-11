package adapter.servletElementConverter;

import javax.servlet.SessionTrackingMode;

public class EnumsConverter {
   public EnumsConverter() {
   }

   public static SessionTrackingMode convert(jakarta.servlet.SessionTrackingMode mode) {
      if (mode == jakarta.servlet.SessionTrackingMode.URL) {
         return SessionTrackingMode.URL;
      } else {
         return mode == jakarta.servlet.SessionTrackingMode.SSL ? SessionTrackingMode.SSL : SessionTrackingMode.COOKIE;
      }
   }

   public static jakarta.servlet.SessionTrackingMode convert(SessionTrackingMode mode) {
      if (mode == SessionTrackingMode.URL) {
         return jakarta.servlet.SessionTrackingMode.URL;
      } else {
         return mode == SessionTrackingMode.SSL ? jakarta.servlet.SessionTrackingMode.SSL : jakarta.servlet.SessionTrackingMode.COOKIE;
      }
   }
}
