package adapter.jakarta.servlet5.http;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter5.StreamConverter;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;


import java.io.IOException;

 @SuppressWarnings("unused")
 public class WebConnection implements jakarta.servlet.http.WebConnection, AdapterUnwrap<javax.servlet.http.WebConnection>{
    private final javax.servlet.http.WebConnection webConnection;

    public javax.servlet.http.WebConnection getWebConnection() {
        return webConnection;
    }

    public WebConnection(javax.servlet.http.WebConnection webConnection) {
        this.webConnection = webConnection;

    }

   // @req FR-CONV-003
   @Override public javax.servlet.http.WebConnection unwrap() {
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