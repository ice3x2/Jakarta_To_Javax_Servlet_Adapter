package adapter.jakarta.servlet.http;

import java.util.Enumeration;

/** @deprecated */
public class HttpSessionContext implements jakarta.servlet.http.HttpSessionContext {
   javax.servlet.http.HttpSessionContext httpSessionContext;

   HttpSessionContext(javax.servlet.http.HttpSessionContext httpSessionContext) {
      this.httpSessionContext = httpSessionContext;
   }

   public jakarta.servlet.http.HttpSession getSession(String sessionId) {
      return new HttpSession(this.httpSessionContext.getSession(sessionId));
   }

   public Enumeration<String> getIds() {
      return this.httpSessionContext.getIds();
   }
}
