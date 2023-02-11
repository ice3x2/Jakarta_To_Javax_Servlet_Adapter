package adapter.servletElementConverter;

import java.io.IOException;

public class WriteListenerConverter {

    public static javax.servlet.WriteListener convert(final jakarta.servlet.WriteListener writeListener) {
        return new javax.servlet.WriteListener() {

            @Override
            public void onWritePossible() throws IOException {
                writeListener.onWritePossible();
            }

            @Override
            public void onError(Throwable t) {
                writeListener.onError(t);
            }
        };
    }

    public static jakarta.servlet.WriteListener convert(final javax.servlet.WriteListener writeListener) {
        return new jakarta.servlet.WriteListener() {

            @Override
            public void onWritePossible() throws IOException {
                writeListener.onWritePossible();
            }

            @Override
            public void onError(Throwable t) {
                writeListener.onError(t);
            }
        };
    }
}
