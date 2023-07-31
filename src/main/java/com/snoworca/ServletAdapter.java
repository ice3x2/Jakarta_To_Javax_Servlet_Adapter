package com.snoworca;

import adapter.servletElementConverter.ServletReqResConverter;


public class ServletAdapter {
   private ServletAdapter() {
   }


   public static javax.servlet.http.HttpServletResponse adaptToJavax(jakarta.servlet.http.HttpServletResponse response) {
      return new adapter.javax.servlet.http.HttpServletResponse(response);
   }

   public static javax.servlet.http.HttpServletResponse adaptToJavax(javax.servlet.http.HttpServletResponse response) {
      return response;
   }

   public static javax.servlet.http.HttpServletRequest adaptToJavax(jakarta.servlet.http.HttpServletRequest request) {
      return new adapter.javax.servlet.http.HttpServletRequest(request);
   }

   public static javax.servlet.http.HttpServletRequest adaptToJavax(javax.servlet.http.HttpServletRequest request) {
      return request;
   }

   public static javax.servlet.ServletResponse adaptToJavax(jakarta.servlet.ServletResponse response) {
      return ServletReqResConverter.convert(response);
   }

   public static javax.servlet.ServletResponse adaptToJavax(javax.servlet.ServletResponse response) {
      return response;
   }

   public static javax.servlet.ServletRequest adaptToJavax(jakarta.servlet.ServletRequest request) {
      return ServletReqResConverter.convert(request);
   }

   public static javax.servlet.ServletRequest adaptToJavax(javax.servlet.ServletRequest request) {
      return request;
   }

   public static javax.servlet.ServletContext adaptToJavax(jakarta.servlet.ServletContext context) {
      return new adapter.javax.servlet.ServletContext(context);
   }

   public static javax.servlet.ServletContext adaptToJavax(javax.servlet.ServletContext context) {
      return context;
   }



   public static jakarta.servlet.http.HttpServletResponse adaptToJakarta(javax.servlet.http.HttpServletResponse response) {
      return new adapter.jakarta.servlet.http.HttpServletResponse(response);
   }


   public static jakarta.servlet.http.HttpServletRequest adaptToJakarta(javax.servlet.http.HttpServletRequest request) {
      return new adapter.jakarta.servlet.http.HttpServletRequest(request);
   }



   public static jakarta.servlet.ServletResponse adaptToJakarta(javax.servlet.ServletResponse response) {
      return ServletReqResConverter.convert(response);
   }


   public static jakarta.servlet.ServletRequest adaptToJakarta(javax.servlet.ServletRequest request) {
      return ServletReqResConverter.convert(request);
   }


   public static jakarta.servlet.ServletContext adaptToJakarta(javax.servlet.ServletContext context) {
      return new adapter.jakarta.servlet.ServletContext(context);
   }
}
