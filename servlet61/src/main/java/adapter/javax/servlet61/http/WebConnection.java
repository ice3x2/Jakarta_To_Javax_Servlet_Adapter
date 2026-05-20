package adapter.javax.servlet61.http;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter61.StreamConverter;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import java.io.IOException;

 @SuppressWarnings("unused")
 public class WebConnection implements javax.servlet.http.WebConnection, AdapterUnwrap<jakarta.servlet.http.WebConnection>{
    private final jakarta.servlet.http.WebConnection webConnection;

     public jakarta.servlet.http.WebConnection getWebConnection() {
        return webConnection;
    }



     public WebConnection(jakarta.servlet.http.WebConnection webConnection) {
        this.webConnection = webConnection;

    }

   // @req FR-CONV-003
   @Override public jakarta.servlet.http.WebConnection unwrap() {
      return this.webConnection;
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


    @Override public void close() throws Exception {
        this.webConnection.close();

    }


    @Override public ServletInputStream getInputStream() throws IOException {
        return StreamConverter.convert(this.webConnection.getInputStream());
    }


    @Override public ServletOutputStream getOutputStream() throws IOException {
        return StreamConverter.convert(this.webConnection.getOutputStream());
    }
}
