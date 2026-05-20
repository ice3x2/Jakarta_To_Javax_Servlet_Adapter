package adapter.jakarta.servlet61.http;

import adapter.common.AdapterUnwrap;

import jakarta.servlet.ServletContext;
import java.util.Enumeration;

 @SuppressWarnings("deprecation")
 public class HttpSession implements jakarta.servlet.http.HttpSession, AdapterUnwrap<javax.servlet.http.HttpSession> {
   private final javax.servlet.http.HttpSession httpSession;

   HttpSession(javax.servlet.http.HttpSession httpSession) {
      this.httpSession = httpSession;
   }

   // @req FR-CONV-003
   @Override public javax.servlet.http.HttpSession unwrap() {
      return this.httpSession;
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

   @Override public long getCreationTime() {
      return this.httpSession.getCreationTime();
   }

   @Override public String getId() {
      return this.httpSession.getId();
   }

   @Override public long getLastAccessedTime() {
      return this.httpSession.getLastAccessedTime();
   }

   @Override public ServletContext getServletContext() {
      return new adapter.jakarta.servlet61.ServletContext(this.httpSession.getServletContext());
   }

   @Override public void setMaxInactiveInterval(int interval) {
      this.httpSession.setMaxInactiveInterval(interval);
   }

   @Override public int getMaxInactiveInterval() {
      return this.httpSession.getMaxInactiveInterval();
   }

   // @Override removed: getSessionContext / HttpSessionContext removed from jakarta.servlet.http in 6.0
   // SRS §6.2.2 policy D — original delegation skipped, return empty dummy.
   // @req FR-CONV-010
   public EmptyHttpSessionContext getSessionContext() {
      return new EmptyHttpSessionContext();
   }

   @Override public Object getAttribute(String name) {
      return this.httpSession.getAttribute(name);
   }

   // @Override removed: getValue removed from jakarta.servlet.http.HttpSession in 6.0
   // SRS §6.2.2 policy B — fall back to attribute API instead of deprecated getValue.
   // @req FR-CONV-010
   public Object getValue(String name) {
      return this.httpSession.getAttribute(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.httpSession.getAttributeNames();
   }

   // @Override removed: getValueNames removed from jakarta.servlet.http.HttpSession in 6.0
   // SRS §6.2.2 policy B — derive value names from attribute names.
   // @req FR-CONV-010
   public String[] getValueNames() {
      Enumeration<String> names = this.httpSession.getAttributeNames();
      java.util.List<String> out = new java.util.ArrayList<>();
      while (names.hasMoreElements()) {
         out.add(names.nextElement());
      }
      return out.toArray(new String[0]);
   }

   @Override public void setAttribute(String name, Object value) {
      this.httpSession.setAttribute(name, value);
   }

   // @Override removed: putValue removed from jakarta.servlet.http.HttpSession in 6.0
   // SRS §6.2.2 policy B — fall back to attribute API instead of deprecated putValue.
   // @req FR-CONV-010
   public void putValue(String name, Object value) {
      this.httpSession.setAttribute(name, value);
   }

   @Override public void removeAttribute(String name) {
      this.httpSession.removeAttribute(name);
   }

   // @Override removed: removeValue removed from jakarta.servlet.http.HttpSession in 6.0
   // SRS §6.2.2 policy B — fall back to attribute API instead of deprecated removeValue.
   // @req FR-CONV-010
   public void removeValue(String name) {
      this.httpSession.removeAttribute(name);
   }

   @Override public void invalidate() {
      this.httpSession.invalidate();
   }

   @Override public boolean isNew() {
      return this.httpSession.isNew();
   }
}
