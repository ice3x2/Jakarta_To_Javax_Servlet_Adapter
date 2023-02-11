package adapter.jakarta.servlet;

import adapter.servletElementConverter.AsyncContextConverter;
import adapter.servletElementConverter.ServletReqResConverter;
import adapter.servletElementConverter.StreamConverter;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class ServletRequest implements jakarta.servlet.ServletRequest {
   javax.servlet.ServletRequest request = null;

   public ServletRequest(javax.servlet.ServletRequest request) {
      this.request = request;
   }

   public javax.servlet.ServletRequest getRequest() {
      return this.request;
   }

   public Object getAttribute(String name) {
      return this.request.getAttribute(name);
   }

   public Enumeration<String> getAttributeNames() {
      return this.request.getAttributeNames();
   }

   public String getCharacterEncoding() {
      return this.request.getCharacterEncoding();
   }

   public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
      this.request.setCharacterEncoding(env);
   }

   public int getContentLength() {
      return this.request.getContentLength();
   }

   public String getContentType() {
      return this.request.getContentType();
   }

   public ServletInputStream getInputStream() throws IOException {
      return StreamConverter.convert(this.request.getInputStream());
   }

   public String getParameter(String name) {
      return this.request.getParameter(name);
   }

   public Enumeration<String> getParameterNames() {
      return this.request.getParameterNames();
   }

   public String[] getParameterValues(String name) {
      return this.request.getParameterValues(name);
   }

   public Map<String, String[]> getParameterMap() {
      return this.request.getParameterMap();
   }

   public String getProtocol() {
      return this.request.getProtocol();
   }

   public String getScheme() {
      return this.request.getScheme();
   }

   public String getServerName() {
      return this.request.getServerName();
   }

   public int getServerPort() {
      return this.request.getServerPort();
   }

   public BufferedReader getReader() throws IOException {
      return this.request.getReader();
   }

   public String getRemoteAddr() {
      return this.request.getRemoteAddr();
   }

   public String getRemoteHost() {
      return this.request.getRemoteHost();
   }

   public void setAttribute(String name, Object o) {
      this.request.setAttribute(name, o);
   }

   public void removeAttribute(String name) {
      this.request.removeAttribute(name);
   }

   public Locale getLocale() {
      return this.request.getLocale();
   }

   public Enumeration<Locale> getLocales() {
      return this.request.getLocales();
   }

   public boolean isSecure() {
      return this.request.isSecure();
   }

   public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.request.getRequestDispatcher(path));
   }

   public String getRealPath(String path) {
      return this.request.getRealPath(path);
   }

   public int getRemotePort() {
      return this.request.getRemotePort();
   }

   public String getLocalName() {
      return this.request.getLocalName();
   }

   public String getLocalAddr() {
      return this.request.getLocalAddr();
   }

   public int getLocalPort() {
      return this.request.getLocalPort();
   }

   public jakarta.servlet.ServletContext getServletContext() {
      return new ServletContext(this.request.getServletContext());
   }

   public AsyncContext startAsync() {
      return AsyncContextConverter.convert(this.request.startAsync());
   }

   public boolean isAsyncStarted() {
      return this.request.isAsyncStarted();
   }

   public boolean isAsyncSupported() {
      return this.request.isAsyncSupported();
   }

   public AsyncContext getAsyncContext() {
      return AsyncContextConverter.convert(this.request.getAsyncContext());
   }

   public DispatcherType getDispatcherType() {
      return DispatcherType.valueOf(this.request.getDispatcherType().name());
   }

   public AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) {
      return AsyncContextConverter.convert(this.request.startAsync(ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse)));
   }

   public long getContentLengthLong() {
      try {
         return this.request.getContentLengthLong();
      } catch (Exception e) {
         return (long)this.request.getContentLength();
      }
   }
}
