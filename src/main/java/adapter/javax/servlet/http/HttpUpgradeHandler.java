package adapter.javax.servlet.http;

import javax.servlet.http.WebConnection;

 public class HttpUpgradeHandler implements javax.servlet.http.HttpUpgradeHandler{

    private final jakarta.servlet.http.HttpUpgradeHandler httpUpgradeHandler;

     public jakarta.servlet.http.HttpUpgradeHandler getHttpUpgradeHandler() {
        return httpUpgradeHandler;
    }

     public HttpUpgradeHandler(jakarta.servlet.http.HttpUpgradeHandler httpUpgradeHandler) {
        this.httpUpgradeHandler = httpUpgradeHandler;

    }


    @Override public void init(WebConnection wc) {
        this.httpUpgradeHandler.init(new adapter.jakarta.servlet.http.WebConnection(wc));

    }


    @Override public void destroy() {
        this.httpUpgradeHandler.destroy();

    }
}
