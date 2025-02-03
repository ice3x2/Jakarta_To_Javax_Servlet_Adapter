package adapter.servletElementConverter;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class AsyncContextConverter {
   public AsyncContextConverter() {
   }

   public static AsyncContext convert(final javax.servlet.AsyncContext asyncContext) {
      return new AsyncContext() {
         public void addListener(AsyncListener listener) {
            asyncContext.addListener(AsyncContextConverter.convert(listener));
         }

         public void addListener(AsyncListener asyncListener, ServletRequest servletRequest, ServletResponse servletResponse) {
            asyncContext.addListener(AsyncContextConverter.convert(asyncListener), ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse));
         }

         public void complete() {
            asyncContext.complete();
         }

         public <T extends AsyncListener> T createListener(Class<T> arg0) throws ServletException {
            try {
               AsyncListener asyncListener = arg0.newInstance();
               javax.servlet.AsyncListener javaxAsyncListener =  AsyncContextConverter.convert(asyncListener);
               asyncContext.addListener(javaxAsyncListener);
               return arg0.cast(javaxAsyncListener);
            } catch (InstantiationException | IllegalAccessException e) {
               throw new ServletException(e);
            }
         }

         public void dispatch() {
            asyncContext.dispatch();
         }

         public void dispatch(String arg0) {
            asyncContext.dispatch(arg0);
         }

         public void dispatch(ServletContext arg0, String arg1) {
            asyncContext.dispatch(new adapter.javax.servlet.ServletContext(arg0), arg1);
         }

         public ServletRequest getRequest() {
            return ServletReqResConverter.convert(asyncContext.getRequest());
         }

         public ServletResponse getResponse() {
            return ServletReqResConverter.convert(asyncContext.getResponse());
         }

         public long getTimeout() {
            return asyncContext.getTimeout();
         }

         public boolean hasOriginalRequestAndResponse() {
            return asyncContext.hasOriginalRequestAndResponse();
         }

         public void setTimeout(long arg0) {
            asyncContext.setTimeout(arg0);
         }

         public void start(Runnable arg0) {
            asyncContext.start(arg0);
         }
      };
   }

   public static javax.servlet.AsyncContext convert(final AsyncContext asyncContext) {
      return new javax.servlet.AsyncContext() {
         public void addListener(javax.servlet.AsyncListener listener) {
             asyncContext.addListener(AsyncContextConverter.convert(listener));
         }

         public void addListener(javax.servlet.AsyncListener arg0, javax.servlet.ServletRequest arg1, javax.servlet.ServletResponse arg2) {
            asyncContext.addListener(AsyncContextConverter.convert(arg0), ServletReqResConverter.convert(arg1), ServletReqResConverter.convert(arg2));
         }

         public void complete() {
            asyncContext.complete();
         }

         public void dispatch() {
            asyncContext.dispatch();
         }

         public void dispatch(String arg0) {
            asyncContext.dispatch(arg0);
         }

         public void dispatch(javax.servlet.ServletContext arg0, String arg1) {
            asyncContext.dispatch(new adapter.jakarta.servlet.ServletContext(arg0), arg1);
         }

         public javax.servlet.ServletRequest getRequest() {
            return ServletReqResConverter.convert(asyncContext.getRequest());
         }

         public javax.servlet.ServletResponse getResponse() {
            return ServletReqResConverter.convert(asyncContext.getResponse());
         }

         public long getTimeout() {
            return asyncContext.getTimeout();
         }

         public boolean hasOriginalRequestAndResponse() {
            return asyncContext.hasOriginalRequestAndResponse();
         }

         public void setTimeout(long arg0) {
            asyncContext.setTimeout(arg0);
         }

         public void start(Runnable arg0) {
            asyncContext.start(arg0);
         }

         public <T extends javax.servlet.AsyncListener> T createListener(Class<T> arg0) throws javax.servlet.ServletException {
            try {
               javax.servlet.AsyncListener javaxAsyncListener = arg0.newInstance();
               AsyncListener asyncListener =  AsyncContextConverter.convert(javaxAsyncListener);
               asyncContext.addListener(asyncListener);
               return arg0.cast(javaxAsyncListener);
            } catch (InstantiationException | IllegalAccessException e) {
               throw new javax.servlet.ServletException(e);
            }
         }
      };
   }

   public static AsyncListener convert(final javax.servlet.AsyncListener listener) {
      return new AsyncListener() {
         public void onComplete(AsyncEvent arg0) throws IOException {
            listener.onComplete(AsyncContextConverter.convert(arg0));
         }

         public void onError(AsyncEvent arg0) throws IOException {
            listener.onError(AsyncContextConverter.convert(arg0));
         }

         public void onStartAsync(AsyncEvent arg0) throws IOException {
            listener.onStartAsync(AsyncContextConverter.convert(arg0));
         }

         public void onTimeout(AsyncEvent arg0) throws IOException {
            listener.onTimeout(AsyncContextConverter.convert(arg0));
         }
      };
   }

   public static javax.servlet.AsyncListener convert(final AsyncListener listener) {
      return new javax.servlet.AsyncListener() {
         public void onComplete(javax.servlet.AsyncEvent arg0) throws IOException {
            listener.onComplete(AsyncContextConverter.convert(arg0));
         }

         public void onError(javax.servlet.AsyncEvent arg0) throws IOException {
            listener.onError(AsyncContextConverter.convert(arg0));
         }

         public void onStartAsync(javax.servlet.AsyncEvent arg0) throws IOException {
            listener.onStartAsync(AsyncContextConverter.convert(arg0));
         }

         public void onTimeout(javax.servlet.AsyncEvent arg0) throws IOException {
            listener.onTimeout(AsyncContextConverter.convert(arg0));
         }
      };
   }

   public static AsyncEvent convert(javax.servlet.AsyncEvent ori) {
      return new AsyncEvent(convert(ori.getAsyncContext()), ServletReqResConverter.convert(ori.getSuppliedRequest()), ServletReqResConverter.convert(ori.getSuppliedResponse()), ori.getThrowable());
   }

   public static javax.servlet.AsyncEvent convert(AsyncEvent ori) {
      return new javax.servlet.AsyncEvent(convert(ori.getAsyncContext()), ServletReqResConverter.convert(ori.getSuppliedRequest()), ServletReqResConverter.convert(ori.getSuppliedResponse()), ori.getThrowable());
   }
}
