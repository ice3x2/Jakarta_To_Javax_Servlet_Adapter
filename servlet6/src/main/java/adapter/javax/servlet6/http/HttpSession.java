package adapter.javax.servlet6.http;

import adapter.common.AdapterUnwrap;

import java.util.Enumeration;
import javax.servlet.ServletContext;

 public class HttpSession implements javax.servlet.http.HttpSession, AdapterUnwrap<jakarta.servlet.http.HttpSession> {
   private final jakarta.servlet.http.HttpSession httpSession;

   HttpSession(jakarta.servlet.http.HttpSession httpSession) {
      this.httpSession = httpSession;
   }

   // @req FR-CONV-003
   @Override public jakarta.servlet.http.HttpSession unwrap() {
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
      return new adapter.javax.servlet6.ServletContext(this.httpSession.getServletContext());
   }

   @Override public void setMaxInactiveInterval(int interval) {
      this.httpSession.setMaxInactiveInterval(interval);
   }

   @Override public int getMaxInactiveInterval() {
      return this.httpSession.getMaxInactiveInterval();
   }

   @SuppressWarnings("deprecation")
   @Override public javax.servlet.http.HttpSessionContext getSessionContext() {
      // placeholder pending PH-004 — jakarta.servlet.http.HttpSessionContext removed in 6.0
      return new HttpSessionContext();
   }

   @Override public Object getAttribute(String name) {
      return this.httpSession.getAttribute(name);
   }

   @SuppressWarnings("deprecation")
   @Override public Object getValue(String name) {
      // placeholder pending PH-004 — getValue removed in Servlet 6.0; fallback to getAttribute
      return this.httpSession.getAttribute(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.httpSession.getAttributeNames();
   }

   @SuppressWarnings("deprecation")
   @Override public String[] getValueNames() {
      // placeholder pending PH-004 — getValueNames removed in Servlet 6.0; fallback to attribute names
      return java.util.Collections.list(this.httpSession.getAttributeNames()).toArray(new String[0]);
   }

   @Override public void setAttribute(String name, Object value) {
      this.httpSession.setAttribute(name, value);
   }

   @SuppressWarnings("deprecation")
   @Override public void putValue(String name, Object value) {
      // placeholder pending PH-004 — putValue removed in Servlet 6.0; fallback to setAttribute
      this.httpSession.setAttribute(name, value);
   }

   @Override public void removeAttribute(String name) {
      this.httpSession.removeAttribute(name);
   }

   @SuppressWarnings("deprecation")
   @Override public void removeValue(String name) {
      // placeholder pending PH-004 — removeValue removed in Servlet 6.0; fallback to removeAttribute
      this.httpSession.removeAttribute(name);
   }

   @Override public void invalidate() {
      this.httpSession.invalidate();
   }

   @Override public boolean isNew() {
      return this.httpSession.isNew();
   }
}
