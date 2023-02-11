package adapter.jakarta.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class Part implements jakarta.servlet.http.Part {
   javax.servlet.http.Part part;

   Part(javax.servlet.http.Part part) {
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

   public String getSubmittedFileName() {
      try {
         return this.getSubmittedFileName();
      } catch (Exception e) {
         return this.getName();
      }
   }
}
