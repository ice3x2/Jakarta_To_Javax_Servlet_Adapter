package adapter.jakarta.servlet;

import adapter.servletElementConverter.ServletReqResConverter;
import jakarta.servlet.ServletException;
import java.io.IOException;

public class RequestDispatcher implements jakarta.servlet.RequestDispatcher {
   private final javax.servlet.RequestDispatcher requestDispatcher;


   public RequestDispatcher(javax.servlet.RequestDispatcher requestDispatcher) {
      this.requestDispatcher = requestDispatcher;
   }
   @Override
   public void forward(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException, IOException {
      //noinspection CaughtExceptionImmediatelyRethrown
      try {
         this.requestDispatcher.forward(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (javax.servlet.ServletException e) {
         throw new ServletException(e);
      } catch (IOException ex) {
         throw ex;
      }
   }
   @Override
   public void include(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException, IOException {
      //noinspection CaughtExceptionImmediatelyRethrown
      try {
         this.requestDispatcher.include(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (javax.servlet.ServletException e) {
         throw new ServletException(e);
      } catch (IOException ex) {
         throw ex;
      }
   }
}
