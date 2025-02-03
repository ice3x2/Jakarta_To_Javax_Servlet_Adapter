package adapter.jakarta.servlet.http;

import java.util.Enumeration;

/** @deprecated */
 @SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
 public class HttpSessionContext implements jakarta.servlet.http.HttpSessionContext {
   private final javax.servlet.http.HttpSessionContext httpSessionContext;

   HttpSessionContext(javax.servlet.http.HttpSessionContext httpSessionContext) {
      this.httpSessionContext = httpSessionContext;
   }

   @Override public jakarta.servlet.http.HttpSession getSession(String sessionId) {
      return new HttpSession(this.httpSessionContext.getSession(sessionId));
   }

   @Override public Enumeration<String> getIds() {
      return this.httpSessionContext.getIds();
   }
}
