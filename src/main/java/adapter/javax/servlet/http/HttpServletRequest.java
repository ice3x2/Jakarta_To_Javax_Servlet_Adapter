package adapter.javax.servlet.http;

import adapter.javax.servlet.ServletRequest;
import adapter.servletElementConverter.AsyncContextConverter;
import adapter.servletElementConverter.HttpComponentConverter;
import adapter.servletElementConverter.ServletReqResConverter;
import adapter.servletElementConverter.StreamConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpUpgradeHandler;

public class HttpServletRequest extends ServletRequest implements javax.servlet.http.HttpServletRequest {
   jakarta.servlet.http.HttpServletRequest httpRequest = null;

   public HttpServletRequest(jakarta.servlet.http.HttpServletRequest request) {
      super(request);
      this.httpRequest = request;
   }

   public Object getAttribute(String name) {
      return this.httpRequest.getAttribute(name);
   }

   public Enumeration<String> getAttributeNames() {
      return this.httpRequest.getAttributeNames();
   }

   public String getCharacterEncoding() {
      return this.httpRequest.getCharacterEncoding();
   }

   public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
      this.httpRequest.setCharacterEncoding(env);
   }

   public int getContentLength() {
      return this.httpRequest.getContentLength();
   }

   public String getContentType() {
      return this.httpRequest.getContentType();
   }

   public ServletInputStream getInputStream() throws IOException {
      return StreamConverter.convert(this.httpRequest.getInputStream());
   }

   public String getParameter(String name) {
      return this.httpRequest.getParameter(name);
   }

   public Enumeration<String> getParameterNames() {
      return this.httpRequest.getParameterNames();
   }

   public String[] getParameterValues(String name) {
      return this.httpRequest.getParameterValues(name);
   }

   public Map<String, String[]> getParameterMap() {
      return this.httpRequest.getParameterMap();
   }

   public String getProtocol() {
      return this.httpRequest.getProtocol();
   }

   public String getScheme() {
      return this.httpRequest.getScheme();
   }

   public String getServerName() {
      return this.httpRequest.getServerName();
   }

   public int getServerPort() {
      return this.httpRequest.getServerPort();
   }

   public BufferedReader getReader() throws IOException {
      return this.httpRequest.getReader();
   }

   public String getRemoteAddr() {
      return this.httpRequest.getRemoteAddr();
   }

   public String getRemoteHost() {
      return this.httpRequest.getRemoteHost();
   }

   public void setAttribute(String name, Object o) {
      this.httpRequest.setAttribute(name, o);
   }

   public void removeAttribute(String name) {
      this.httpRequest.removeAttribute(name);
   }

   public Locale getLocale() {
      return this.httpRequest.getLocale();
   }

   public Enumeration<Locale> getLocales() {
      return this.httpRequest.getLocales();
   }

   public boolean isSecure() {
      return this.httpRequest.isSecure();
   }

   public RequestDispatcher getRequestDispatcher(String path) {
      return new adapter.javax.servlet.RequestDispatcher(this.httpRequest.getRequestDispatcher(path));
   }

   public String getRealPath(String path) {
      return this.httpRequest.getRealPath(path);
   }

   public int getRemotePort() {
      return this.httpRequest.getRemotePort();
   }

   public String getLocalName() {
      return this.httpRequest.getLocalName();
   }

   public String getLocalAddr() {
      return this.httpRequest.getLocalAddr();
   }

   public int getLocalPort() {
      return this.httpRequest.getLocalPort();
   }

   public ServletContext getServletContext() {
      return new adapter.javax.servlet.ServletContext(this.httpRequest.getServletContext());
   }

   public AsyncContext startAsync() {
      return AsyncContextConverter.convert(this.httpRequest.startAsync());
   }

   public AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, ServletResponse servletResponse) {
      return AsyncContextConverter.convert(this.httpRequest.startAsync(ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse)));
   }

   public boolean isAsyncStarted() {
      return this.httpRequest.isAsyncStarted();
   }

   public boolean isAsyncSupported() {
      return this.httpRequest.isAsyncSupported();
   }

   public AsyncContext getAsyncContext() {
      return AsyncContextConverter.convert(this.httpRequest.getAsyncContext());
   }

   public DispatcherType getDispatcherType() {
      return HttpComponentConverter.convert(this.httpRequest.getDispatcherType());
   }

   public String getAuthType() {
      return this.httpRequest.getAuthType();
   }

   public javax.servlet.http.Cookie[] getCookies() {
      jakarta.servlet.http.Cookie[] cookies = this.httpRequest.getCookies();
      ArrayList<javax.servlet.http.Cookie> newArrayList = new ArrayList();
      jakarta.servlet.http.Cookie[] var6 = cookies;
      int var5 = cookies.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         jakarta.servlet.http.Cookie cookie = var6[var4];
         newArrayList.add(new Cookie(cookie));
      }

      return (javax.servlet.http.Cookie[])newArrayList.toArray(new javax.servlet.http.Cookie[cookies.length]);
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

   public String getPathInfo() {
      return this.httpRequest.getPathInfo();
   }

   public String getPathTranslated() {
      return this.httpRequest.getPathTranslated();
   }

   public String getContextPath() {
      return this.httpRequest.getContextPath();
   }

   public String getQueryString() {
      return this.httpRequest.getQueryString();
   }

   public String getRemoteUser() {
      return this.httpRequest.getRemoteUser();
   }

   public boolean isUserInRole(String role) {
      return this.httpRequest.isUserInRole(role);
   }

   public Principal getUserPrincipal() {
      return this.httpRequest.getUserPrincipal();
   }

   public String getRequestedSessionId() {
      return this.httpRequest.getRequestedSessionId();
   }

   public String getRequestURI() {
      return this.httpRequest.getRequestURI();
   }

   public StringBuffer getRequestURL() {
      return this.httpRequest.getRequestURL();
   }

   public String getServletPath() {
      return this.httpRequest.getServletPath();
   }

   public javax.servlet.http.HttpSession getSession(boolean create) {
      return new HttpSession(this.httpRequest.getSession(create));
   }

   public javax.servlet.http.HttpSession getSession() {
      return new HttpSession(this.httpRequest.getSession());
   }

   @Override
   public String changeSessionId() {
      return this.httpRequest.changeSessionId();
   }

   public boolean isRequestedSessionIdValid() {
      return this.httpRequest.isRequestedSessionIdValid();
   }

   public boolean isRequestedSessionIdFromCookie() {
      return this.httpRequest.isRequestedSessionIdFromCookie();
   }

   public boolean isRequestedSessionIdFromURL() {
      return this.httpRequest.isRequestedSessionIdFromURL();
   }

   public boolean isRequestedSessionIdFromUrl() {
      return this.httpRequest.isRequestedSessionIdFromUrl();
   }

   public boolean authenticate(javax.servlet.http.HttpServletResponse response) throws IOException, ServletException {
      try {
         return this.httpRequest.authenticate(new adapter.jakarta.servlet.http.HttpServletResponse(response));
      } catch (IOException var3) {
         throw var3;
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   public void login(String username, String password) throws ServletException {
      try {
         this.httpRequest.login(username, password);
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   public void logout() throws ServletException {
      try {
         this.httpRequest.logout();
      } catch (jakarta.servlet.ServletException var2) {
         throw new ServletException(var2);
      }
   }

   public Collection<javax.servlet.http.Part> getParts() throws IOException, IllegalStateException, ServletException {
      try {
         ArrayList<javax.servlet.http.Part> partList = new ArrayList();
         Collection<jakarta.servlet.http.Part> jakartaParts = this.httpRequest.getParts();
         Iterator var4 = jakartaParts.iterator();

         while(var4.hasNext()) {
            jakarta.servlet.http.Part jakartaPart = (jakarta.servlet.http.Part)var4.next();
            partList.add(new Part(jakartaPart));
         }

         return partList;
      } catch (IOException var5) {
         throw var5;
      } catch (jakarta.servlet.ServletException var6) {
         throw new ServletException(var6);
      }
   }

   public javax.servlet.http.Part getPart(String name) throws IOException, IllegalStateException, ServletException {
      try {
         return new Part(this.httpRequest.getPart(name));
      } catch (IOException var3) {
         throw var3;
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   @Override
   public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {

      return null;
   }
}
