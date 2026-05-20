package adapter.servletElementConverter61;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class ServletReqResConverter {
   public ServletReqResConverter() {
   }


   public static ServletResponse convert(javax.servlet.ServletResponse response) {
      return response instanceof javax.servlet.http.HttpServletResponse ? new adapter.jakarta.servlet61.http.HttpServletResponse((javax.servlet.http.HttpServletResponse)response) : new adapter.jakarta.servlet61.ServletResponse(response);
   }

   public static ServletRequest convert(javax.servlet.ServletRequest request) {
      return request instanceof javax.servlet.http.HttpServletRequest ? new adapter.jakarta.servlet61.http.HttpServletRequest((javax.servlet.http.HttpServletRequest)request) : new adapter.jakarta.servlet61.ServletRequest(request);
   }

   public static javax.servlet.ServletResponse convert(ServletResponse response) {
      return response instanceof jakarta.servlet.http.HttpServletResponse ? new adapter.javax.servlet61.http.HttpServletResponse((jakarta.servlet.http.HttpServletResponse)response) : new adapter.javax.servlet61.ServletResponse(response);
   }

   public static javax.servlet.ServletRequest convert(ServletRequest request) {
      return request instanceof jakarta.servlet.http.HttpServletRequest ? new adapter.javax.servlet61.http.HttpServletRequest((jakarta.servlet.http.HttpServletRequest)request) : new adapter.javax.servlet61.ServletRequest(request);
   }
}
