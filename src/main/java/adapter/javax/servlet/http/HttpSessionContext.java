package adapter.javax.servlet.http;

import java.util.Enumeration;

/** @deprecated */
public class HttpSessionContext implements javax.servlet.http.HttpSessionContext {
   jakarta.servlet.http.HttpSessionContext httpSessionContext;

   HttpSessionContext(jakarta.servlet.http.HttpSessionContext httpSessionContext) {
      this.httpSessionContext = httpSessionContext;
   }

   public javax.servlet.http.HttpSession getSession(String sessionId) {
      return new HttpSession(this.httpSessionContext.getSession(sessionId));
   }

   public Enumeration<String> getIds() {
      return this.httpSessionContext.getIds();
   }
}
