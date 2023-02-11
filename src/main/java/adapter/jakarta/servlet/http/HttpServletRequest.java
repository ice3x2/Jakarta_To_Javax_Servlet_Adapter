package adapter.jakarta.servlet.http;

import adapter.jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpUpgradeHandler;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class HttpServletRequest extends ServletRequest implements jakarta.servlet.http.HttpServletRequest {
   javax.servlet.http.HttpServletRequest httpRequest = null;

   public HttpServletRequest(javax.servlet.http.HttpServletRequest request) {
      super(request);
      this.httpRequest = request;
   }

   public boolean authenticate(jakarta.servlet.http.HttpServletResponse arg0) throws IOException, ServletException {
      try {
         return this.httpRequest.authenticate(new adapter.javax.servlet.http.HttpServletResponse(arg0));
      } catch (IOException var3) {
         throw var3;
      } catch (javax.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   public String changeSessionId() {
      try {
         return this.httpRequest.changeSessionId();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public String getAuthType() {
      return this.httpRequest.getAuthType();
   }

   public String getContextPath() {
      return this.httpRequest.getContextPath();
   }

   public long getDateHeader(String name) {
      return this.httpRequest.getDateHeader(name);
   }

   public String getHeader(String name) {
      return this.httpRequest.getHeader(name);
   }

   public Enumeration<String> getHeaders(String name) {
      return this.httpRequest.getHeaders(name);
   }

   public Enumeration<String> getHeaderNames() {
      return this.httpRequest.getHeaderNames();
   }

   public int getIntHeader(String name) {
      return this.httpRequest.getIntHeader(name);
   }

   public String getMethod() {
      return this.httpRequest.getMethod();
   }

   public jakarta.servlet.http.Part getPart(String arg0) throws IOException, ServletException {
      try {
         return new Part(this.httpRequest.getPart(arg0));
      } catch (IllegalStateException var3) {
         throw var3;
      } catch (IOException var4) {
         throw var4;
      } catch (javax.servlet.ServletException var5) {
         throw new ServletException(var5);
      }
   }

   public Collection<jakarta.servlet.http.Part> getParts() throws IOException, ServletException {
      try {
         ArrayList<jakarta.servlet.http.Part> partList = new ArrayList();
         Collection<javax.servlet.http.Part> jakartaParts = this.httpRequest.getParts();
         Iterator var4 = jakartaParts.iterator();

         while(var4.hasNext()) {
            javax.servlet.http.Part jakartaPart = (javax.servlet.http.Part)var4.next();
            partList.add(new Part(jakartaPart));
         }

         return partList;
      } catch (IOException var5) {
         throw var5;
      } catch (javax.servlet.ServletException var6) {
         throw new ServletException(var6);
      }
   }

   public String getPathInfo() {
      return this.httpRequest.getPathInfo();
   }

   public String getPathTranslated() {
      return this.httpRequest.getPathTranslated();
   }

   public String getQueryString() {
      return this.httpRequest.getQueryString();
   }

   public String getRemoteUser() {
      return this.httpRequest.getRemoteUser();
   }

   public String getRequestedSessionId() {
      return this.httpRequest.getRequestedSessionId();
   }

   public String getServletPath() {
      return this.httpRequest.getServletPath();
   }

   public jakarta.servlet.http.HttpSession getSession() {
      return new HttpSession(this.httpRequest.getSession());
   }

   public jakarta.servlet.http.HttpSession getSession(boolean arg0) {
      return new HttpSession(this.httpRequest.getSession(arg0));
   }

   public Principal getUserPrincipal() {
      return this.httpRequest.getUserPrincipal();
   }

   public boolean isRequestedSessionIdFromCookie() {
      return this.httpRequest.isRequestedSessionIdFromCookie();
   }

   public boolean isRequestedSessionIdFromURL() {
      return this.httpRequest.isRequestedSessionIdFromURL();
   }

   public boolean isRequestedSessionIdValid() {
      return this.httpRequest.isRequestedSessionIdValid();
   }

   public boolean isUserInRole(String role) {
      return this.httpRequest.isUserInRole(role);
   }

   public void login(String arg0, String arg1) throws ServletException {
      try {
         this.httpRequest.login(arg0, arg1);
      } catch (javax.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   public void logout() throws ServletException {
      try {
         this.httpRequest.logout();
      } catch (javax.servlet.ServletException var2) {
         throw new ServletException(var2);
      }
   }

   public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
      return null;
   }

   public jakarta.servlet.http.Cookie[] getCookies() {
      javax.servlet.http.Cookie[] cookies = this.httpRequest.getCookies();
      ArrayList<jakarta.servlet.http.Cookie> newArrayList = new ArrayList();
      javax.servlet.http.Cookie[] var6 = cookies;
      int var5 = cookies.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         javax.servlet.http.Cookie cookie = var6[var4];
         newArrayList.add(new Cookie(cookie));
      }

      return (jakarta.servlet.http.Cookie[])newArrayList.toArray(new jakarta.servlet.http.Cookie[cookies.length]);
   }

   public String getRequestURI() {
      return this.httpRequest.getRequestURI();
   }

   public StringBuffer getRequestURL() {
      return this.httpRequest.getRequestURL();
   }

   public boolean isRequestedSessionIdFromUrl() {
      return this.httpRequest.isRequestedSessionIdFromUrl();
   }
}
