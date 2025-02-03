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
    private final javax.servlet.ServletRequest request;

    public ServletRequest(javax.servlet.ServletRequest request) {
      this.request = request;
   }

    @SuppressWarnings("unused")
    public javax.servlet.ServletRequest getRequest() {
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

   @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.request.getRequestDispatcher(path));
   }

   @Override public String getRealPath(String path) {
       //noinspection deprecation
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

   @Override public jakarta.servlet.ServletContext getServletContext() {
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
      return DispatcherType.valueOf(this.request.getDispatcherType().name());
   }

   @Override public AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) {
      return AsyncContextConverter.convert(this.request.startAsync(ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse)));
   }

   @Override public long getContentLengthLong() {
      try {
         return this.request.getContentLengthLong();
      } catch (Exception e) {
         return this.request.getContentLength();
      }
   }
}
