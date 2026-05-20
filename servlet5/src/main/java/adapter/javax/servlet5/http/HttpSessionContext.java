package adapter.javax.servlet5.http;

import adapter.common.AdapterUnwrap;

import java.util.Enumeration;

/** @deprecated */
@SuppressWarnings("ALL")
public class HttpSessionContext implements javax.servlet.http.HttpSessionContext, AdapterUnwrap<jakarta.servlet.http.HttpSessionContext> {
   private final jakarta.servlet.http.HttpSessionContext httpSessionContext;

   HttpSessionContext(jakarta.servlet.http.HttpSessionContext httpSessionContext) {
      this.httpSessionContext = httpSessionContext;
   }

   // @req FR-CONV-003
   @Override public jakarta.servlet.http.HttpSessionContext unwrap() {
      return this.httpSessionContext;
   }

   // @req FR-CONV-004
   @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null) return false;
      Object other = (o instanceof AdapterUnwrap<?>) ? ((AdapterUnwrap<?>) o).unwrap() : o;
      return this.unwrap().equals(other);
   }

   // @req FR-CONV-004
   @Override public int hashCode() {
      return this.unwrap().hashCode();
   }

   @Override public javax.servlet.http.HttpSession getSession(String sessionId) {
      return new HttpSession(this.httpSessionContext.getSession(sessionId));
   }

   @Override public Enumeration<String> getIds() {
      return this.httpSessionContext.getIds();
   }
}
