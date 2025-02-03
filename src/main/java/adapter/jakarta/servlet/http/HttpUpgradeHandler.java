package adapter.jakarta.servlet.http;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public class HttpUpgradeHandler implements jakarta.servlet.http.HttpUpgradeHandler {

    private static final ThreadLocal<Class<?>> jakartaUpgradeHandlerClass = new ThreadLocal<>();
     private final javax.servlet.http.HttpUpgradeHandler httpUpgradeHandler;

     public javax.servlet.http.HttpUpgradeHandler getHttpUpgradeHandler() {
        return httpUpgradeHandler;
    }

    public HttpUpgradeHandler() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz;
        clazz = jakartaUpgradeHandlerClass.get();
        jakartaUpgradeHandlerClass.remove();
        if (clazz == null) {
            throw new IllegalStateException("No javax.servlet.http.HttpUpgradeHandler class set");
        }
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        this.httpUpgradeHandler = (javax.servlet.http.HttpUpgradeHandler) constructor.newInstance();
    }


    public static void setJakartaUpgradeHandlerClass(Class<?> jakartaUpgradeHandlerClass) {
        HttpUpgradeHandler.jakartaUpgradeHandlerClass.set(jakartaUpgradeHandlerClass);
    }


    public HttpUpgradeHandler(javax.servlet.http.HttpUpgradeHandler httpUpgradeHandler) {
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
