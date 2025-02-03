package adapter.javax.servlet.http;

import java.util.Enumeration;
import javax.servlet.ServletContext;

 public class HttpSession implements javax.servlet.http.HttpSession {
   private final jakarta.servlet.http.HttpSession httpSession;

   HttpSession(jakarta.servlet.http.HttpSession httpSession) {
      this.httpSession = httpSession;
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
      return new adapter.javax.servlet.ServletContext(this.httpSession.getServletContext());
   }

   @Override public void setMaxInactiveInterval(int interval) {
      this.httpSession.setMaxInactiveInterval(interval);
   }

   @Override public int getMaxInactiveInterval() {
      return this.httpSession.getMaxInactiveInterval();
   }

   @SuppressWarnings("deprecation")
   @Override public javax.servlet.http.HttpSessionContext getSessionContext() {
      return new HttpSessionContext(this.httpSession.getSessionContext());
   }

   @Override public Object getAttribute(String name) {
      return this.httpSession.getAttribute(name);
   }

   @SuppressWarnings("deprecation")
   @Override public Object getValue(String name) {
      return this.httpSession.getValue(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.httpSession.getAttributeNames();
   }

   @SuppressWarnings("deprecation")
   @Override public String[] getValueNames() {
      return this.httpSession.getValueNames();
   }

   @Override public void setAttribute(String name, Object value) {
      this.httpSession.setAttribute(name, value);
   }

   @SuppressWarnings("deprecation")
   @Override public void putValue(String name, Object value) {
      this.httpSession.putValue(name, value);
   }

   @Override public void removeAttribute(String name) {
      this.httpSession.removeAttribute(name);
   }

   @SuppressWarnings("deprecation")
   @Override public void removeValue(String name) {
      this.httpSession.removeValue(name);
   }

   @Override public void invalidate() {
      this.httpSession.invalidate();
   }

   @Override public boolean isNew() {
      return this.httpSession.isNew();
   }
}
