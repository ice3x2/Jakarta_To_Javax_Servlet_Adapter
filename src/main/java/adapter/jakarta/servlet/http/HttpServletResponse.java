package adapter.jakarta.servlet.http;

import adapter.jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

 public class HttpServletResponse extends ServletResponse implements jakarta.servlet.http.HttpServletResponse {
   private final javax.servlet.http.HttpServletResponse httpResponse;

    public HttpServletResponse(javax.servlet.http.HttpServletResponse response) {
      super(response);
      this.httpResponse = response;
   }

   @Override public String getCharacterEncoding() {
      return this.httpResponse.getCharacterEncoding();
   }

   @Override public String getContentType() {
      return this.httpResponse.getContentType();
   }

   @Override public PrintWriter getWriter() throws IOException {
      return this.httpResponse.getWriter();
   }

   @Override public void setCharacterEncoding(String charset) {
      this.httpResponse.setCharacterEncoding(charset);
   }

   @Override public void setContentLength(int len) {
      this.httpResponse.setContentLength(len);
   }

   @Override public void setContentType(String type) {
      this.httpResponse.setContentType(type);
   }

   @Override public void setBufferSize(int size) {
      this.httpResponse.setBufferSize(size);
   }

   @Override public int getBufferSize() {
      return this.httpResponse.getBufferSize();
   }

   @Override public void flushBuffer() throws IOException {
      this.httpResponse.flushBuffer();
   }

   @Override public void resetBuffer() {
      this.httpResponse.resetBuffer();
   }

   @Override public boolean isCommitted() {
      return this.httpResponse.isCommitted();
   }

   @Override public void reset() {
      this.httpResponse.reset();
   }

   @Override public void setLocale(Locale loc) {
      this.httpResponse.setLocale(loc);
   }

   @Override public Locale getLocale() {
      return this.httpResponse.getLocale();
   }

   @Override public boolean containsHeader(String name) {
      return this.httpResponse.containsHeader(name);
   }

   @Override public String encodeURL(String url) {
      return this.httpResponse.encodeURL(url);
   }

   @Override public String encodeRedirectURL(String url) {
      return this.httpResponse.encodeRedirectURL(url);
   }

   @Override public String encodeUrl(String url) {
      return this.httpResponse.encodeURL(url);
   }

   @SuppressWarnings("deprecation")
   @Override public String encodeRedirectUrl(String url) {
      return this.httpResponse.encodeRedirectUrl(url);
   }

   @Override public void sendError(int sc, String msg) throws IOException {
      this.httpResponse.sendError(sc, msg);
   }

   @Override public void sendError(int sc) throws IOException {
      this.httpResponse.sendError(sc);
   }

   @Override public void sendRedirect(String location) throws IOException {
      this.httpResponse.sendRedirect(location);
   }

   @Override public void setDateHeader(String name, long date) {
      this.httpResponse.setDateHeader(name, date);
   }

   @Override public void addDateHeader(String name, long date) {
      this.httpResponse.addDateHeader(name, date);
   }

   @Override public void setHeader(String name, String value) {
      this.httpResponse.setHeader(name, value);
   }

   @Override public void addHeader(String name, String value) {
      this.httpResponse.addHeader(name, value);
   }

   @Override public void setIntHeader(String name, int value) {
      this.httpResponse.setIntHeader(name, value);
   }

   @Override public void addIntHeader(String name, int value) {
      this.httpResponse.addIntHeader(name, value);
   }

   @Override public void setStatus(int sc) {
      this.httpResponse.setStatus(sc);
   }

   @SuppressWarnings("deprecation")
   @Override public void setStatus(int sc, String sm) {
      this.httpResponse.setStatus(sc, sm);
   }

   @Override public int getStatus() {
      return this.httpResponse.getStatus();
   }

   @Override public String getHeader(String name) {
      return this.httpResponse.getHeader(name);
   }

   @Override public Collection<String> getHeaders(String name) {
      return this.httpResponse.getHeaders(name);
   }

   @Override public Collection<String> getHeaderNames() {
      return this.httpResponse.getHeaderNames();
   }

   @Override public void setContentLengthLong(long arg0) {
      this.httpResponse.setContentLength((int)arg0);
   }

   @Override public void addCookie(jakarta.servlet.http.Cookie arg0) {
      this.httpResponse.addCookie(new adapter.javax.servlet.http.Cookie(arg0));
   }
}
