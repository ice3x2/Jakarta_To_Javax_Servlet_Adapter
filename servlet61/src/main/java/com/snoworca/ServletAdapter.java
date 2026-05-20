package com.snoworca;

import adapter.common.AdapterUnwrap;
import adapter.common.ConverterSupport;
import adapter.servletElementConverter61.ServletReqResConverter;

import java.util.Map;


@SuppressWarnings("unused")
// @req FR-CONV-003
public class ServletAdapter {
   private ServletAdapter() {
   }

   /**
    * FR-CONV-012 — diagnostics opt-in snapshot. 시스템 프로퍼티
    * {@code com.snoworca.adapter.diagnostics=true} 가 활성화된 경우에만
    * {@code Map<String, Long>} (methodKey → 호출 카운트) 을 반환한다.
    * 비활성 시 empty Map.
    *
    * @req FR-CONV-012
    */
   public static Map<String, Long> diagnostics() {
      ConverterSupport.initDiagnosticsFromSystemProperty();
      return ConverterSupport.diagnosticsSnapshot();
   }


   public static javax.servlet.http.HttpServletResponse adaptToJavax(jakarta.servlet.http.HttpServletResponse response) {
      if (response instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) response).unwrap();
         if (u instanceof javax.servlet.http.HttpServletResponse) return (javax.servlet.http.HttpServletResponse) u;
      }
      return new adapter.javax.servlet61.http.HttpServletResponse(response);
   }



   public static javax.servlet.http.HttpServletResponse adaptToJavax(javax.servlet.http.HttpServletResponse response) {
      return response;
   }

   public static javax.servlet.http.HttpServletRequest adaptToJavax(jakarta.servlet.http.HttpServletRequest request) {
      if (request instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) request).unwrap();
         if (u instanceof javax.servlet.http.HttpServletRequest) return (javax.servlet.http.HttpServletRequest) u;
      }
      return new adapter.javax.servlet61.http.HttpServletRequest(request);
   }

   public static javax.servlet.http.HttpServletRequest adaptToJavax(javax.servlet.http.HttpServletRequest request) {
      return request;
   }

   public static javax.servlet.ServletResponse adaptToJavax(jakarta.servlet.ServletResponse response) {
      if (response instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) response).unwrap();
         if (u instanceof javax.servlet.ServletResponse) return (javax.servlet.ServletResponse) u;
      }
      return ServletReqResConverter.convert(response);
   }

   public static javax.servlet.ServletResponse adaptToJavax(javax.servlet.ServletResponse response) {
      return response;
   }

   public static javax.servlet.ServletRequest adaptToJavax(jakarta.servlet.ServletRequest request) {
      if (request instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) request).unwrap();
         if (u instanceof javax.servlet.ServletRequest) return (javax.servlet.ServletRequest) u;
      }
      return ServletReqResConverter.convert(request);
   }

   public static javax.servlet.ServletRequest adaptToJavax(javax.servlet.ServletRequest request) {
      return request;
   }

   public static javax.servlet.ServletContext adaptToJavax(jakarta.servlet.ServletContext context) {
      if (context instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) context).unwrap();
         if (u instanceof javax.servlet.ServletContext) return (javax.servlet.ServletContext) u;
      }
      return new adapter.javax.servlet61.ServletContext(context);
   }

   public static javax.servlet.ServletContext adaptToJavax(javax.servlet.ServletContext context) {
      return context;
   }



   public static jakarta.servlet.http.HttpServletResponse adaptToJakarta(javax.servlet.http.HttpServletResponse response) {
      if (response instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) response).unwrap();
         if (u instanceof jakarta.servlet.http.HttpServletResponse) return (jakarta.servlet.http.HttpServletResponse) u;
      }
      return new adapter.jakarta.servlet61.http.HttpServletResponse(response);
   }


   public static jakarta.servlet.http.HttpServletRequest adaptToJakarta(javax.servlet.http.HttpServletRequest request) {
      if (request instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) request).unwrap();
         if (u instanceof jakarta.servlet.http.HttpServletRequest) return (jakarta.servlet.http.HttpServletRequest) u;
      }
      return new adapter.jakarta.servlet61.http.HttpServletRequest(request);
   }



   public static jakarta.servlet.ServletResponse adaptToJakarta(javax.servlet.ServletResponse response) {
      if (response instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) response).unwrap();
         if (u instanceof jakarta.servlet.ServletResponse) return (jakarta.servlet.ServletResponse) u;
      }
      return ServletReqResConverter.convert(response);
   }


   public static jakarta.servlet.ServletRequest adaptToJakarta(javax.servlet.ServletRequest request) {
      if (request instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) request).unwrap();
         if (u instanceof jakarta.servlet.ServletRequest) return (jakarta.servlet.ServletRequest) u;
      }
      return ServletReqResConverter.convert(request);
   }



   public static jakarta.servlet.ServletContext adaptToJakarta(javax.servlet.ServletContext context) {
      if (context instanceof AdapterUnwrap) {
         Object u = ((AdapterUnwrap<?>) context).unwrap();
         if (u instanceof jakarta.servlet.ServletContext) return (jakarta.servlet.ServletContext) u;
      }
      return new adapter.jakarta.servlet61.ServletContext(context);
   }
}
