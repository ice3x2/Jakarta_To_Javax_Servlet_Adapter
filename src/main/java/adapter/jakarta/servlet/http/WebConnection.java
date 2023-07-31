package adapter.jakarta.servlet.http;

import adapter.servletElementConverter.StreamConverter;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;


import java.io.IOException;

 public class WebConnection implements jakarta.servlet.http.WebConnection{
    private javax.servlet.http.WebConnection webConnection;

    public javax.servlet.http.WebConnection getWebConnection() {
        return webConnection;
    }

    public WebConnection(javax.servlet.http.WebConnection webConnection) {
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