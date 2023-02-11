package adapter.javax.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class Part implements javax.servlet.http.Part {
   jakarta.servlet.http.Part part;

   Part(jakarta.servlet.http.Part part) {
      this.part = part;
   }

   public InputStream getInputStream() throws IOException {
      return this.part.getInputStream();
   }

   public String getContentType() {
      return this.part.getContentType();
   }

   public String getName() {
      return this.part.getName();
   }

   @Override
   public String getSubmittedFileName() {
      return this.part.getSubmittedFileName();
   }

   public long getSize() {
      return this.part.getSize();
   }

   public void write(String fileName) throws IOException {
      this.part.write(fileName);
   }

   public void delete() throws IOException {
      this.part.delete();
   }

   public String getHeader(String name) {
      return this.part.getHeader(name);
   }

   public Collection<String> getHeaders(String name) {
      return this.part.getHeaders(name);
   }

   public Collection<String> getHeaderNames() {
      return this.part.getHeaderNames();
   }
}
