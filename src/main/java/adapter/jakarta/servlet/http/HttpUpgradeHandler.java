package adapter.jakarta.servlet.http;


public class HttpUpgradeHandler implements jakarta.servlet.http.HttpUpgradeHandler{

    javax.servlet.http.HttpUpgradeHandler httpUpgradeHandler;

    public javax.servlet.http.HttpUpgradeHandler getHttpUpgradeHandler() {
        return httpUpgradeHandler;
    }

    HttpUpgradeHandler(javax.servlet.http.HttpUpgradeHandler httpUpgradeHandler) {
        this.httpUpgradeHandler = httpUpgradeHandler;

    }


    @Override
    public void init(jakarta.servlet.http.WebConnection wc) {
        this.httpUpgradeHandler.init(new adapter.javax.servlet.http.WebConnection(wc));
    }

    @Override
    public void destroy() {
        this.httpUpgradeHandler.destroy();

    }
}
