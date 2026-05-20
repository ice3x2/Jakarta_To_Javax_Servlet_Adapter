package adapter.jakarta.servlet61;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter61.StreamConverter;
import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

 public class ServletResponse implements jakarta.servlet.ServletResponse, AdapterUnwrap<javax.servlet.ServletResponse> {
   private final javax.servlet.ServletResponse response;

    public ServletResponse(javax.servlet.ServletResponse response) {
      this.response = response;
   }

   // @req FR-CONV-003
   @Override public javax.servlet.ServletResponse unwrap() {
      return this.response;
   }

   // @req FR-CONV-004
   @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null) return false;
      Object other = (o instanceof AdapterUnwrap<?>) ? ((AdapterUnwrap<?>) o).unwrap() : o;
      return this.unwrap().equals(other);
   }

   // @req FR-CONV-004
   @Override public int hashCode() {
      return this.unwrap().hashCode();
   }

    @SuppressWarnings("unused")
    public javax.servlet.ServletResponse getResponse() {
      return this.response;
   }

   @Override public String getCharacterEncoding() {
      return this.response.getCharacterEncoding();
   }

   @Override public String getContentType() {
      return this.response.getContentType();
   }

   @Override public ServletOutputStream getOutputStream() throws IOException {
      return StreamConverter.convert(this.response.getOutputStream());
   }

   @Override public PrintWriter getWriter() throws IOException {
      return this.response.getWriter();
   }

   @Override public void setCharacterEncoding(String charset) {
      this.response.setCharacterEncoding(charset);
   }

   @Override public void setContentLength(int len) {
      this.response.setContentLength(len);
   }

   @Override public void setContentType(String type) {
      this.response.setContentType(type);
   }

   @Override public void setBufferSize(int size) {
      this.response.setBufferSize(size);
   }

   @Override public int getBufferSize() {
      return this.response.getBufferSize();
   }

   @Override public void flushBuffer() throws IOException {
      this.response.flushBuffer();
   }

   @Override public void resetBuffer() {
      this.response.resetBuffer();
   }

   @Override public boolean isCommitted() {
      return this.response.isCommitted();
   }

   @Override public void reset() {
      this.response.reset();
   }

   @Override public void setLocale(Locale loc) {
      this.response.setLocale(loc);
   }

   @Override public Locale getLocale() {
      return this.response.getLocale();
   }

   @Override public void setContentLengthLong(long arg0) {
      this.response.setContentLengthLong(arg0);
   }
}
