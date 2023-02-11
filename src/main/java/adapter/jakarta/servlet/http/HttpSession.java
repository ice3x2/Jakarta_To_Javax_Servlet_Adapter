package adapter.jakarta.servlet.http;

import jakarta.servlet.ServletContext;
import java.util.Enumeration;

public class HttpSession implements jakarta.servlet.http.HttpSession {
   javax.servlet.http.HttpSession httpSession;

   HttpSession(javax.servlet.http.HttpSession httpSession) {
      this.httpSession = httpSession;
   }

   public long getCreationTime() {
      return this.httpSession.getCreationTime();
   }

   public String getId() {
      return this.httpSession.getId();
   }

   public long getLastAccessedTime() {
      return this.httpSession.getLastAccessedTime();
   }

   public ServletContext getServletContext() {
      return new adapter.jakarta.servlet.ServletContext(this.httpSession.getServletContext());
   }

   public void setMaxInactiveInterval(int interval) {
      this.httpSession.setMaxInactiveInterval(interval);
   }

   public int getMaxInactiveInterval() {
      return this.httpSession.getMaxInactiveInterval();
   }

   public jakarta.servlet.http.HttpSessionContext getSessionContext() {
      return new HttpSessionContext(this.httpSession.getSessionContext());
   }

   public Object getAttribute(String name) {
      return this.httpSession.getAttribute(name);
   }

   public Object getValue(String name) {
      return this.httpSession.getValue(name);
   }

   public Enumeration<String> getAttributeNames() {
      return this.httpSession.getAttributeNames();
   }

   public String[] getValueNames() {
      return this.httpSession.getValueNames();
   }

   public void setAttribute(String name, Object value) {
      this.httpSession.setAttribute(name, value);
   }

   public void putValue(String name, Object value) {
      this.httpSession.putValue(name, value);
   }

   public void removeAttribute(String name) {
      this.httpSession.removeAttribute(name);
   }

   public void removeValue(String name) {
      this.httpSession.removeValue(name);
   }

   public void invalidate() {
      this.httpSession.invalidate();
   }

   public boolean isNew() {
      return this.httpSession.isNew();
   }
}
