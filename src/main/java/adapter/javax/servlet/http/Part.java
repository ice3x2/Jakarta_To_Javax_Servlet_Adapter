package adapter.javax.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
public class Part implements javax.servlet.http.Part {
   private final jakarta.servlet.http.Part part;

   Part(jakarta.servlet.http.Part part) {
      this.part = part;
   }

   @Override public InputStream getInputStream() throws IOException {
      return this.part.getInputStream();
   }

   @Override public String getContentType() {
      return this.part.getContentType();
   }

   @Override public String getName() {
      return this.part.getName();
   }

   @Override public String getSubmittedFileName() {
      return this.part.getSubmittedFileName();
   }

   @Override public long getSize() {
      return this.part.getSize();
   }

   @Override public void write(String fileName) throws IOException {
      this.part.write(fileName);
   }

   @Override public void delete() throws IOException {
      this.part.delete();
   }

   @Override public String getHeader(String name) {
      return this.part.getHeader(name);
   }

   @Override public Collection<String> getHeaders(String name) {
      return this.part.getHeaders(name);
   }

   @Override public Collection<String> getHeaderNames() {
      return this.part.getHeaderNames();
   }
}
