package adapter.javax.servlet.http;

import adapter.servletElementConverter.StreamConverter;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import java.io.IOException;

 @SuppressWarnings("unused")
 public class WebConnection implements javax.servlet.http.WebConnection{
    private final jakarta.servlet.http.WebConnection webConnection;

     public jakarta.servlet.http.WebConnection getWebConnection() {
        return webConnection;
    }



     public WebConnection(jakarta.servlet.http.WebConnection webConnection) {
        this.webConnection = webConnection;

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
