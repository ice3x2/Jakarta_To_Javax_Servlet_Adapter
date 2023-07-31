package adapter.servletElementConverter;

import java.io.IOException;

public class ReadListenerConverter {

    public static javax.servlet.ReadListener convert(final jakarta.servlet.ReadListener readListener) {
        return new javax.servlet.ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                readListener.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                readListener.onAllDataRead();
            }

            @Override
            public void onError(Throwable t) {
                readListener.onError(t);
            }
        };
    }

    public static jakarta.servlet.ReadListener convert(final javax.servlet.ReadListener readListener) {
        return new jakarta.servlet.ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                readListener.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                readListener.onAllDataRead();
            }

            @Override
            public void onError(Throwable t) {
                readListener.onError(t);
            }
        };
    }
}
