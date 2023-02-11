package adapter.jakarta.servlet;

import adapter.servletElementConverter.ServletReqResConverter;
import jakarta.servlet.ServletException;
import java.io.IOException;

public class RequestDispatcher implements jakarta.servlet.RequestDispatcher {
   javax.servlet.RequestDispatcher requestDispatcher;

   public RequestDispatcher(javax.servlet.RequestDispatcher requestDispatcher) {
      this.requestDispatcher = requestDispatcher;
   }

   public void forward(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException, IOException {
      try {
         this.requestDispatcher.forward(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (javax.servlet.ServletException var4) {
         throw new ServletException(var4);
      } catch (IOException var5) {
         throw var5;
      }
   }

   public void include(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException, IOException {
      try {
         this.requestDispatcher.include(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (javax.servlet.ServletException var4) {
         throw new ServletException(var4);
      } catch (IOException var5) {
         throw var5;
      }
   }
}
