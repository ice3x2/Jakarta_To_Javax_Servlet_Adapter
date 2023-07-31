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
   private jakarta.servlet.http.HttpServletRequest httpRequest = null;

    public HttpServletRequest(jakarta.servlet.http.HttpServletRequest request) {
      super(request);
      this.httpRequest = request;
   }

   @Override public Object getAttribute(String name) {
      return this.httpRequest.getAttribute(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.httpRequest.getAttributeNames();
   }

   @Override public String getCharacterEncoding() {
      return this.httpRequest.getCharacterEncoding();
   }

   @Override public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
      this.httpRequest.setCharacterEncoding(env);
   }

   @Override public int getContentLength() {
      return this.httpRequest.getContentLength();
   }

   @Override public String getContentType() {
      return this.httpRequest.getContentType();
   }

   @Override public ServletInputStream getInputStream() throws IOException {
      return StreamConverter.convert(this.httpRequest.getInputStream());
   }

   @Override public String getParameter(String name) {
      return this.httpRequest.getParameter(name);
   }

   @Override public Enumeration<String> getParameterNames() {
      return this.httpRequest.getParameterNames();
   }

   @Override public String[] getParameterValues(String name) {
      return this.httpRequest.getParameterValues(name);
   }

   @Override public Map<String, String[]> getParameterMap() {
      return this.httpRequest.getParameterMap();
   }

   @Override public String getProtocol() {
      return this.httpRequest.getProtocol();
   }

   @Override public String getScheme() {
      return this.httpRequest.getScheme();
   }

   @Override public String getServerName() {
      return this.httpRequest.getServerName();
   }

   @Override public int getServerPort() {
      return this.httpRequest.getServerPort();
   }

   @Override public BufferedReader getReader() throws IOException {
      return this.httpRequest.getReader();
   }

   @Override public String getRemoteAddr() {
      return this.httpRequest.getRemoteAddr();
   }

   @Override public String getRemoteHost() {
      return this.httpRequest.getRemoteHost();
   }

   @Override public void setAttribute(String name, Object o) {
      this.httpRequest.setAttribute(name, o);
   }

   @Override public void removeAttribute(String name) {
      this.httpRequest.removeAttribute(name);
   }

   @Override public Locale getLocale() {
      return this.httpRequest.getLocale();
   }

   @Override public Enumeration<Locale> getLocales() {
      return this.httpRequest.getLocales();
   }

   @Override public boolean isSecure() {
      return this.httpRequest.isSecure();
   }

   @Override public RequestDispatcher getRequestDispatcher(String path) {
      return new adapter.javax.servlet.RequestDispatcher(this.httpRequest.getRequestDispatcher(path));
   }

   @Override public String getRealPath(String path) {
      return this.httpRequest.getRealPath(path);
   }

   @Override public int getRemotePort() {
      return this.httpRequest.getRemotePort();
   }

   @Override public String getLocalName() {
      return this.httpRequest.getLocalName();
   }

   @Override public String getLocalAddr() {
      return this.httpRequest.getLocalAddr();
   }

   @Override public int getLocalPort() {
      return this.httpRequest.getLocalPort();
   }

   @Override public ServletContext getServletContext() {
      return new adapter.javax.servlet.ServletContext(this.httpRequest.getServletContext());
   }

   @Override public AsyncContext startAsync() {
      return AsyncContextConverter.convert(this.httpRequest.startAsync());
   }

   @Override public AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, ServletResponse servletResponse) {
      return AsyncContextConverter.convert(this.httpRequest.startAsync(ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse)));
   }

   @Override public boolean isAsyncStarted() {
      return this.httpRequest.isAsyncStarted();
   }

   @Override public boolean isAsyncSupported() {
      return this.httpRequest.isAsyncSupported();
   }

   @Override public AsyncContext getAsyncContext() {
      return AsyncContextConverter.convert(this.httpRequest.getAsyncContext());
   }

   @Override public DispatcherType getDispatcherType() {
      return HttpComponentConverter.convert(this.httpRequest.getDispatcherType());
   }

   @Override public String getAuthType() {
      return this.httpRequest.getAuthType();
   }

   @Override public javax.servlet.http.Cookie[] getCookies() {
      jakarta.servlet.http.Cookie[] cookies = this.httpRequest.getCookies();
      ArrayList<javax.servlet.http.Cookie> newArrayList = new ArrayList<>();

      for (jakarta.servlet.http.Cookie cookie : cookies) {
         newArrayList.add(new Cookie(cookie));
      }

      return newArrayList.toArray(new javax.servlet.http.Cookie[cookies.length]);
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

   @Override public String getPathInfo() {
      return this.httpRequest.getPathInfo();
   }

   @Override public String getPathTranslated() {
      return this.httpRequest.getPathTranslated();
   }

   @Override public String getContextPath() {
      return this.httpRequest.getContextPath();
   }

   @Override public String getQueryString() {
      return this.httpRequest.getQueryString();
   }

   @Override public String getRemoteUser() {
      return this.httpRequest.getRemoteUser();
   }

   @Override public boolean isUserInRole(String role) {
      return this.httpRequest.isUserInRole(role);
   }

   @Override public Principal getUserPrincipal() {
      return this.httpRequest.getUserPrincipal();
   }

   @Override public String getRequestedSessionId() {
      return this.httpRequest.getRequestedSessionId();
   }

   @Override public String getRequestURI() {
      return this.httpRequest.getRequestURI();
   }

   @Override public StringBuffer getRequestURL() {
      return this.httpRequest.getRequestURL();
   }

   @Override public String getServletPath() {
      return this.httpRequest.getServletPath();
   }

   @Override public javax.servlet.http.HttpSession getSession(boolean create) {
      return new HttpSession(this.httpRequest.getSession(create));
   }

   @Override public javax.servlet.http.HttpSession getSession() {
      return new HttpSession(this.httpRequest.getSession());
   }

   @Override public String changeSessionId() {
      return this.httpRequest.changeSessionId();
   }

   @Override public boolean isRequestedSessionIdValid() {
      return this.httpRequest.isRequestedSessionIdValid();
   }

   @Override public boolean isRequestedSessionIdFromCookie() {
      return this.httpRequest.isRequestedSessionIdFromCookie();
   }

   @Override public boolean isRequestedSessionIdFromURL() {
      return this.httpRequest.isRequestedSessionIdFromURL();
   }

   @Override public boolean isRequestedSessionIdFromUrl() {
      return this.httpRequest.isRequestedSessionIdFromUrl();
   }

   @Override public boolean authenticate(javax.servlet.http.HttpServletResponse response) throws IOException, ServletException {
      try {
         return this.httpRequest.authenticate(new adapter.jakarta.servlet.http.HttpServletResponse(response));
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   @Override public void login(String username, String password) throws ServletException {
      try {
         this.httpRequest.login(username, password);
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   @Override public void logout() throws ServletException {
      try {
         this.httpRequest.logout();
      } catch (jakarta.servlet.ServletException var2) {
         throw new ServletException(var2);
      }
   }

   @Override public Collection<javax.servlet.http.Part> getParts() throws IOException, IllegalStateException, ServletException {
      try {
         ArrayList<javax.servlet.http.Part> partList = new ArrayList<>();
         Collection<jakarta.servlet.http.Part> jakartaParts = this.httpRequest.getParts();

         for (jakarta.servlet.http.Part jakartaPart : jakartaParts) {
            partList.add(new Part(jakartaPart));
         }

         return partList;
      } catch (jakarta.servlet.ServletException var6) {
         throw new ServletException(var6);
      }
   }

   @Override public javax.servlet.http.Part getPart(String name) throws IOException, IllegalStateException, ServletException {
      try {
         return new Part(this.httpRequest.getPart(name));
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }


   @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {

      return null;
   }
}
