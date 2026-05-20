package adapter.javax.servlet5;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter5.ServletReqResConverter;
import java.io.IOException;
import javax.servlet.ServletException;
public class RequestDispatcher implements javax.servlet.RequestDispatcher, AdapterUnwrap<jakarta.servlet.RequestDispatcher> {
   private final jakarta.servlet.RequestDispatcher requestDispatcher;

   public RequestDispatcher(jakarta.servlet.RequestDispatcher requestDispatcher) {
      this.requestDispatcher = requestDispatcher;
   }

   // @req FR-CONV-003
   @Override public jakarta.servlet.RequestDispatcher unwrap() {
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

   @Override public void forward(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) throws ServletException, IOException {
      try {
         this.requestDispatcher.forward(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }

   @Override public void include(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) throws ServletException, IOException {
      try {
         this.requestDispatcher.include(ServletReqResConverter.convert(request), ServletReqResConverter.convert(response));
      } catch (jakarta.servlet.ServletException var4) {
         throw new ServletException(var4);
      }
   }
}
