package adapter.jakarta.servlet5;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter5.ServletReqResConverter;
import jakarta.servlet.ServletException;
import java.io.IOException;

public class RequestDispatcher implements jakarta.servlet.RequestDispatcher, AdapterUnwrap<javax.servlet.RequestDispatcher> {
   private final javax.servlet.RequestDispatcher requestDispatcher;


   public RequestDispatcher(javax.servlet.RequestDispatcher requestDispatcher) {
      this.requestDispatcher = requestDispatcher;
   }

   // @req FR-CONV-003
   @Override public javax.servlet.RequestDispatcher unwrap() {
      return this.requestDispatcher;
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
