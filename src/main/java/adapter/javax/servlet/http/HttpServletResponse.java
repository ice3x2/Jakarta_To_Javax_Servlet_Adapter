package adapter.javax.servlet.http;

import adapter.javax.servlet.ServletResponse;
import adapter.servletElementConverter.StreamConverter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;

public class HttpServletResponse extends ServletResponse implements javax.servlet.http.HttpServletResponse {
   jakarta.servlet.http.HttpServletResponse httpResponse = null;

   public HttpServletResponse(jakarta.servlet.http.HttpServletResponse response) {
      super(response);
      this.httpResponse = response;
   }

   public String getCharacterEncoding() {
      return this.httpResponse.getCharacterEncoding();
   }

   public String getContentType() {
      return this.httpResponse.getContentType();
   }

   public ServletOutputStream getOutputStream() throws IOException {
      return StreamConverter.convert(this.httpResponse.getOutputStream());
   }

   public PrintWriter getWriter() throws IOException {
      return this.httpResponse.getWriter();
   }

   public void setCharacterEncoding(String charset) {
      this.httpResponse.setCharacterEncoding(charset);
   }

   public void setContentLength(int len) {
      this.httpResponse.setContentLength(len);
   }

   public void setContentType(String type) {
      this.httpResponse.setContentType(type);
   }

   public void setBufferSize(int size) {
      this.httpResponse.setBufferSize(size);
   }

   public int getBufferSize() {
      return this.httpResponse.getBufferSize();
   }

   public void flushBuffer() throws IOException {
      this.httpResponse.flushBuffer();
   }

   public void resetBuffer() {
      this.httpResponse.resetBuffer();
   }

   public boolean isCommitted() {
      return this.httpResponse.isCommitted();
   }

   public void reset() {
      this.httpResponse.reset();
   }

   public void setLocale(Locale loc) {
      this.httpResponse.setLocale(loc);
   }

   public Locale getLocale() {
      return this.httpResponse.getLocale();
   }

   public void addCookie(javax.servlet.http.Cookie cookie) {
      this.httpResponse.addCookie(new adapter.jakarta.servlet.http.Cookie(cookie));
   }

   public boolean containsHeader(String name) {
      return this.httpResponse.containsHeader(name);
   }

   public String encodeURL(String url) {
      return this.httpResponse.encodeURL(url);
   }

   public String encodeRedirectURL(String url) {
      return this.httpResponse.encodeRedirectURL(url);
   }

   public String encodeUrl(String url) {
      return this.httpResponse.encodeURL(url);
   }

   public String encodeRedirectUrl(String url) {
      return this.httpResponse.encodeRedirectUrl(url);
   }

   public void sendError(int sc, String msg) throws IOException {
      this.httpResponse.sendError(sc, msg);
   }

   public void sendError(int sc) throws IOException {
      this.httpResponse.sendError(sc);
   }

   public void sendRedirect(String location) throws IOException {
      this.httpResponse.sendRedirect(location);
   }

   public void setDateHeader(String name, long date) {
      this.httpResponse.setDateHeader(name, date);
   }

   public void addDateHeader(String name, long date) {
      this.httpResponse.addDateHeader(name, date);
   }

   public void setHeader(String name, String value) {
      this.httpResponse.setHeader(name, value);
   }

   public void addHeader(String name, String value) {
      this.httpResponse.addHeader(name, value);
   }

   public void setIntHeader(String name, int value) {
      this.httpResponse.setIntHeader(name, value);
   }

   public void addIntHeader(String name, int value) {
      this.httpResponse.addIntHeader(name, value);
   }

   public void setStatus(int sc) {
      this.httpResponse.setStatus(sc);
   }

   public void setStatus(int sc, String sm) {
      this.httpResponse.setStatus(sc, sm);
   }

   public int getStatus() {
      return this.httpResponse.getStatus();
   }

   public String getHeader(String name) {
      return this.httpResponse.getHeader(name);
   }

   public Collection<String> getHeaders(String name) {
      return this.httpResponse.getHeaders(name);
   }

   public Collection<String> getHeaderNames() {
      return this.httpResponse.getHeaderNames();
   }
}
