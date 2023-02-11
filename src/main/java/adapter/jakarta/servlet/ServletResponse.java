package adapter.jakarta.servlet;

import adapter.servletElementConverter.StreamConverter;
import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class ServletResponse implements jakarta.servlet.ServletResponse {
   javax.servlet.ServletResponse response = null;

   public ServletResponse(javax.servlet.ServletResponse response) {
      this.response = response;
   }

   public javax.servlet.ServletResponse getResponse() {
      return this.response;
   }

   public String getCharacterEncoding() {
      return this.response.getCharacterEncoding();
   }

   public String getContentType() {
      return this.response.getContentType();
   }

   public ServletOutputStream getOutputStream() throws IOException {
      return StreamConverter.convert(this.response.getOutputStream());
   }

   public PrintWriter getWriter() throws IOException {
      return this.response.getWriter();
   }

   public void setCharacterEncoding(String charset) {
      this.response.setCharacterEncoding(charset);
   }

   public void setContentLength(int len) {
      this.response.setContentLength(len);
   }

   public void setContentType(String type) {
      this.response.setContentType(type);
   }

   public void setBufferSize(int size) {
      this.response.setBufferSize(size);
   }

   public int getBufferSize() {
      return this.response.getBufferSize();
   }

   public void flushBuffer() throws IOException {
      this.response.flushBuffer();
   }

   public void resetBuffer() {
      this.response.resetBuffer();
   }

   public boolean isCommitted() {
      return this.response.isCommitted();
   }

   public void reset() {
      this.response.reset();
   }

   public void setLocale(Locale loc) {
      this.response.setLocale(loc);
   }

   public Locale getLocale() {
      return this.response.getLocale();
   }

   public void setContentLengthLong(long arg0) {
      this.response.setContentLength((int)arg0);
   }
}
