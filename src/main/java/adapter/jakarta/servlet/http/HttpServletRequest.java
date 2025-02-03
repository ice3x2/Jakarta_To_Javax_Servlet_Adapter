package adapter.jakarta.servlet.http;

import adapter.jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpUpgradeHandler;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class HttpServletRequest extends ServletRequest implements jakarta.servlet.http.HttpServletRequest {
   private final javax.servlet.http.HttpServletRequest httpRequest;

    public HttpServletRequest(javax.servlet.http.HttpServletRequest request) {
      super(request);
      this.httpRequest = request;
   }

   @Override public boolean authenticate(jakarta.servlet.http.HttpServletResponse arg0) throws IOException, ServletException {
      try {
         return this.httpRequest.authenticate(new adapter.javax.servlet.http.HttpServletResponse(arg0));
      } catch (javax.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   @Override public String changeSessionId() {
         return this.httpRequest.changeSessionId();
   }

   @Override public String getAuthType() {
      return this.httpRequest.getAuthType();
   }

   @Override public String getContextPath() {
      return this.httpRequest.getContextPath();
   }

   @Override public long getDateHeader(String name) {
      return this.httpRequest.getDateHeader(name);
   }

   @Override public String getHeader(String name) {
      return this.httpRequest.getHeader(name);
   }

   @Override public Enumeration<String> getHeaders(String name) {
      return this.httpRequest.getHeaders(name);
   }

   @Override public Enumeration<String> getHeaderNames() {
      return this.httpRequest.getHeaderNames();
   }

   @Override public int getIntHeader(String name) {
      return this.httpRequest.getIntHeader(name);
   }

   @Override public String getMethod() {
      return this.httpRequest.getMethod();
   }

   @Override public jakarta.servlet.http.Part getPart(String arg0) throws IOException, ServletException {
      try {
         return new Part(this.httpRequest.getPart(arg0));
      } catch (javax.servlet.ServletException var5) {
         throw new ServletException(var5);
      }
   }

   @Override public Collection<jakarta.servlet.http.Part> getParts() throws IOException, ServletException {
      try {
         ArrayList<jakarta.servlet.http.Part> partList = new ArrayList<>();
         Collection<javax.servlet.http.Part> jakartaParts = this.httpRequest.getParts();

         for (javax.servlet.http.Part jakartaPart : jakartaParts) {
            partList.add(new Part(jakartaPart));
         }

         return partList;
      } catch (javax.servlet.ServletException var6) {
         throw new ServletException(var6);
      }
   }

   @Override public String getPathInfo() {
      return this.httpRequest.getPathInfo();
   }

   @Override public String getPathTranslated() {
      return this.httpRequest.getPathTranslated();
   }

   @Override public String getQueryString() {
      return this.httpRequest.getQueryString();
   }

   @Override public String getRemoteUser() {
      return this.httpRequest.getRemoteUser();
   }

   @Override public String getRequestedSessionId() {
      return this.httpRequest.getRequestedSessionId();
   }

   @Override public String getServletPath() {
      return this.httpRequest.getServletPath();
   }

   @Override public jakarta.servlet.http.HttpSession getSession() {
      return new HttpSession(this.httpRequest.getSession());
   }

   @Override public jakarta.servlet.http.HttpSession getSession(boolean arg0) {
      return new HttpSession(this.httpRequest.getSession(arg0));
   }

   @Override public Principal getUserPrincipal() {
      return this.httpRequest.getUserPrincipal();
   }

   @Override public boolean isRequestedSessionIdFromCookie() {
      return this.httpRequest.isRequestedSessionIdFromCookie();
   }

   @Override public boolean isRequestedSessionIdFromURL() {
      return this.httpRequest.isRequestedSessionIdFromURL();
   }

   @Override public boolean isRequestedSessionIdValid() {
      return this.httpRequest.isRequestedSessionIdValid();
   }

   @Override public boolean isUserInRole(String role) {
      return this.httpRequest.isUserInRole(role);
   }

   @Override public void login(String arg0, String arg1) throws ServletException {
      try {
         this.httpRequest.login(arg0, arg1);
      } catch (javax.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   @Override public void logout() throws ServletException {
      try {
         this.httpRequest.logout();
      } catch (javax.servlet.ServletException var2) {
         throw new ServletException(var2);
      }
   }

   @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws ServletException {
       try {
          adapter.javax.servlet.http.HttpUpgradeHandler.setJakartaUpgradeHandlerClass(handlerClass);
          adapter.javax.servlet.http.HttpUpgradeHandler handler = this.httpRequest.upgrade(adapter.javax.servlet.http.HttpUpgradeHandler.class);
          jakarta.servlet.http.HttpUpgradeHandler  httpUpgradeHandler = new adapter.jakarta.servlet.http.HttpUpgradeHandler(handler);
          return handlerClass.cast(httpUpgradeHandler);
       } catch (Exception e) {
          throw new ServletException(e);
       }
   }

   @Override public jakarta.servlet.http.Cookie[] getCookies() {
      @SuppressWarnings("DuplicatedCode")
      javax.servlet.http.Cookie[] cookies = this.httpRequest.getCookies();
      ArrayList<jakarta.servlet.http.Cookie> newArrayList = new ArrayList<>();
      for (javax.servlet.http.Cookie cookie : cookies) {
         newArrayList.add(new Cookie(cookie));
      }

      return newArrayList.toArray(new jakarta.servlet.http.Cookie[0]);
   }

   @Override public String getRequestURI() {
      return this.httpRequest.getRequestURI();
   }

   @Override public StringBuffer getRequestURL() {
      return this.httpRequest.getRequestURL();
   }

   @Override public boolean isRequestedSessionIdFromUrl() {
       //noinspection deprecation
       return this.httpRequest.isRequestedSessionIdFromUrl();
   }
}
