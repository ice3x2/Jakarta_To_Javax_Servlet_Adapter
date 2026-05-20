package adapter.javax.servlet61;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter61.StreamConverter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;

@SuppressWarnings("unused")
public class ServletResponse implements javax.servlet.ServletResponse, AdapterUnwrap<jakarta.servlet.ServletResponse> {
   private final jakarta.servlet.ServletResponse response;

   public ServletResponse(jakarta.servlet.ServletResponse response) {
      this.response = response;
   }

   // @req FR-CONV-003
   @Override public jakarta.servlet.ServletResponse unwrap() {
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

   public jakarta.servlet.ServletResponse getResponse() {
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

   @Override
   public void setContentLengthLong(long len) {
      this.response.setContentLengthLong(len);
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
}
