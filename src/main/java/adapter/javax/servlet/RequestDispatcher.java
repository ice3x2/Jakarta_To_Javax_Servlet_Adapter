package adapter.javax.servlet;

import adapter.servletElementConverter.ServletReqResConverter;
import java.io.IOException;
import javax.servlet.ServletException;

public class RequestDispatcher implements javax.servlet.RequestDispatcher {
   jakarta.servlet.RequestDispatcher requestDispatcher;

   public RequestDispatcher(jakarta.servlet.RequestDispatcher requestDispatcher) {
      this.requestDispatcher = requestDispatcher;
   }

   public void forward(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) throws ServletException, IOException {
      try {
         this.requestDispatcher.forward(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      } catch (IOException var5) {
         throw var5;
      }
   }

   public void include(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) throws ServletException, IOException {
      try {
         this.requestDispatcher.include(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      } catch (IOException var5) {
         throw var5;
      }
   }
}
