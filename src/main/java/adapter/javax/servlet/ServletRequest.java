package adapter.javax.servlet;

import adapter.servletElementConverter.AsyncContextConverter;
import adapter.servletElementConverter.HttpComponentConverter;
import adapter.servletElementConverter.ServletReqResConverter;
import adapter.servletElementConverter.StreamConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletInputStream;

 public class ServletRequest implements javax.servlet.ServletRequest {
   private jakarta.servlet.ServletRequest request = null;

    public ServletRequest(jakarta.servlet.ServletRequest request) {
      this.request = request;
   }

    public jakarta.servlet.ServletRequest getRequest() {
      return this.request;
   }

   @Override public Object getAttribute(String name) {
      return this.request.getAttribute(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.request.getAttributeNames();
   }

   @Override public String getCharacterEncoding() {
      return this.request.getCharacterEncoding();
   }

   @Override public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
      this.request.setCharacterEncoding(env);
   }

   @Override public int getContentLength() {
      return this.request.getContentLength();
   }


   @Override public long getContentLengthLong() {
      return this.request.getContentLengthLong();
   }

   @Override public String getContentType() {
      return this.request.getContentType();
   }

   @Override public ServletInputStream getInputStream() throws IOException {
      return StreamConverter.convert(this.request.getInputStream());
   }

   @Override public String getParameter(String name) {
      return this.request.getParameter(name);
   }

   @Override public Enumeration<String> getParameterNames() {
      return this.request.getParameterNames();
   }

   @Override public String[] getParameterValues(String name) {
      return this.request.getParameterValues(name);
   }

   @Override public Map<String, String[]> getParameterMap() {
      return this.request.getParameterMap();
   }

   @Override public String getProtocol() {
      return this.request.getProtocol();
   }

   @Override public String getScheme() {
      return this.request.getScheme();
   }

   @Override public String getServerName() {
      return this.request.getServerName();
   }

   @Override public int getServerPort() {
      return this.request.getServerPort();
   }

   @Override public BufferedReader getReader() throws IOException {
      return this.request.getReader();
   }

   @Override public String getRemoteAddr() {
      return this.request.getRemoteAddr();
   }

   @Override public String getRemoteHost() {
      return this.request.getRemoteHost();
   }

   @Override public void setAttribute(String name, Object o) {
      this.request.setAttribute(name, o);
   }

   @Override public void removeAttribute(String name) {
      this.request.removeAttribute(name);
   }

   @Override public Locale getLocale() {
      return this.request.getLocale();
   }

   @Override public Enumeration<Locale> getLocales() {
      return this.request.getLocales();
   }

   @Override public boolean isSecure() {
      return this.request.isSecure();
   }

   @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.request.getRequestDispatcher(path));
   }

   @Override public String getRealPath(String path) {
      return this.request.getRealPath(path);
   }

   @Override public int getRemotePort() {
      return this.request.getRemotePort();
   }

   @Override public String getLocalName() {
      return this.request.getLocalName();
   }

   @Override public String getLocalAddr() {
      return this.request.getLocalAddr();
   }

   @Override public int getLocalPort() {
      return this.request.getLocalPort();
   }

   @Override public javax.servlet.ServletContext getServletContext() {
      return new ServletContext(this.request.getServletContext());
   }

   @Override public AsyncContext startAsync() {
      return AsyncContextConverter.convert(this.request.startAsync());
   }

   @Override public boolean isAsyncStarted() {
      return this.request.isAsyncStarted();
   }

   @Override public boolean isAsyncSupported() {
      return this.request.isAsyncSupported();
   }

   @Override public AsyncContext getAsyncContext() {
      return AsyncContextConverter.convert(this.request.getAsyncContext());
   }

   @Override public DispatcherType getDispatcherType() {
      return HttpComponentConverter.convert(this.request.getDispatcherType());
   }

   @Override public AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) {
      return AsyncContextConverter.convert(this.request.startAsync(ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse)));
   }
}
