package adapter.javax.servlet61.http;

import adapter.common.AdapterUnwrap;

import javax.servlet.http.WebConnection;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public class HttpUpgradeHandler implements javax.servlet.http.HttpUpgradeHandler, AdapterUnwrap<jakarta.servlet.http.HttpUpgradeHandler> {

    private static final ThreadLocal<Class<?>> jakartaUpgradeHandlerClass = new ThreadLocal<>();
    private final jakarta.servlet.http.HttpUpgradeHandler httpUpgradeHandler;

    public jakarta.servlet.http.HttpUpgradeHandler getHttpUpgradeHandler() {
        return httpUpgradeHandler;
    }

    public HttpUpgradeHandler(jakarta.servlet.http.HttpUpgradeHandler httpUpgradeHandler) {
        this.httpUpgradeHandler = httpUpgradeHandler;
    }

   // @req FR-CONV-003
   @Override public jakarta.servlet.http.HttpUpgradeHandler unwrap() {
      return this.httpUpgradeHandler;
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

    public HttpUpgradeHandler() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz;
        clazz = jakartaUpgradeHandlerClass.get();
        jakartaUpgradeHandlerClass.remove();
        if (clazz == null) {
            throw new IllegalStateException("No jakarta.servlet.http.HttpUpgradeHandler class set");
        }
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        this.httpUpgradeHandler = (jakarta.servlet.http.HttpUpgradeHandler) constructor.newInstance();
    }

    public static void setJakartaUpgradeHandlerClass(Class<?> jakartaUpgradeHandlerClass) {
        HttpUpgradeHandler.jakartaUpgradeHandlerClass.set(jakartaUpgradeHandlerClass);
    }


    @Override public void init(WebConnection wc) {
        this.httpUpgradeHandler.init(new adapter.jakarta.servlet61.http.WebConnection(wc));

    }


    @Override public void destroy() {
        this.httpUpgradeHandler.destroy();

    }
}
