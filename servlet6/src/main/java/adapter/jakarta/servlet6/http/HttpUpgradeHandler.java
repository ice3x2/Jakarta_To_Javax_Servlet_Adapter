package adapter.jakarta.servlet6.http;

import adapter.common.AdapterUnwrap;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public class HttpUpgradeHandler implements jakarta.servlet.http.HttpUpgradeHandler, AdapterUnwrap<javax.servlet.http.HttpUpgradeHandler> {

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

   // @req FR-CONV-003
   @Override public javax.servlet.http.HttpUpgradeHandler unwrap() {
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


    @Override
    public void init(jakarta.servlet.http.WebConnection wc) {
        this.httpUpgradeHandler.init(new adapter.javax.servlet6.http.WebConnection(wc));
    }

    @Override
    public void destroy() {
        this.httpUpgradeHandler.destroy();

    }
}
