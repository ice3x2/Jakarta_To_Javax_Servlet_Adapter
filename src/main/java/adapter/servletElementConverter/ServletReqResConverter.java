package adapter.servletElementConverter;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletReqResConverter {
   public ServletReqResConverter() {
   }

   public static ServletResponse convert(javax.servlet.ServletResponse response) {
      return response instanceof HttpServletResponse ? new adapter.jakarta.servlet.http.HttpServletResponse((HttpServletResponse)response) : new adapter.jakarta.servlet.ServletResponse(response);
   }

   public static ServletRequest convert(javax.servlet.ServletRequest request) {
      return request instanceof HttpServletRequest ? new adapter.jakarta.servlet.http.HttpServletRequest((HttpServletRequest)request) : new adapter.jakarta.servlet.ServletRequest(request);
   }

   public static javax.servlet.ServletResponse convert(ServletResponse response) {
      return response instanceof HttpServletResponse ? new adapter.javax.servlet.http.HttpServletResponse((jakarta.servlet.http.HttpServletResponse)response) : new adapter.javax.servlet.ServletResponse(response);
   }

   public static javax.servlet.ServletRequest convert(ServletRequest request) {
      return request instanceof HttpServletRequest ? new adapter.javax.servlet.http.HttpServletRequest((jakarta.servlet.http.HttpServletRequest)request) : new adapter.javax.servlet.ServletRequest(request);
   }
}
