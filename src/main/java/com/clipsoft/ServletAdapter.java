package com.clipsoft;

import adapter.servletElementConverter.ServletReqResConverter;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletAdapter {
   public ServletAdapter() {
   }

   public static HttpServletResponse toJavax(jakarta.servlet.http.HttpServletResponse response) {
      return new adapter.javax.servlet.http.HttpServletResponse(response);
   }

   public static HttpServletResponse toJavax(HttpServletResponse response) {
      return response;
   }

   public static HttpServletRequest toJavax(jakarta.servlet.http.HttpServletRequest request) {
      return new adapter.javax.servlet.http.HttpServletRequest(request);
   }

   public static HttpServletRequest toJavax(HttpServletRequest request) {
      return request;
   }

   public static ServletResponse toJavax(jakarta.servlet.ServletResponse response) {
      return ServletReqResConverter.convert(response);
   }

   public static ServletResponse toJavax(ServletResponse response) {
      return response;
   }

   public static ServletRequest toJavax(jakarta.servlet.ServletRequest request) {
      return ServletReqResConverter.convert(request);
   }

   public static ServletRequest toJavax(ServletRequest request) {
      return request;
   }

   public static ServletContext toJavax(jakarta.servlet.ServletContext context) {
      return new adapter.javax.servlet.ServletContext(context);
   }

   public static ServletContext toJavax(ServletContext context) {
      return context;
   }

   public static HttpServletResponse adapt(jakarta.servlet.http.HttpServletResponse response) {
      return new adapter.javax.servlet.http.HttpServletResponse(response);
   }

   public static jakarta.servlet.http.HttpServletResponse adapt(HttpServletResponse response) {
      return new adapter.jakarta.servlet.http.HttpServletResponse(response);
   }

   public static HttpServletRequest adapt(jakarta.servlet.http.HttpServletRequest request) {
      return new adapter.javax.servlet.http.HttpServletRequest(request);
   }

   public static jakarta.servlet.http.HttpServletRequest adapt(HttpServletRequest request) {
      return new adapter.jakarta.servlet.http.HttpServletRequest(request);
   }

   public static ServletResponse adapt(jakarta.servlet.ServletResponse response) {
      return ServletReqResConverter.convert(response);
   }

   public static jakarta.servlet.ServletResponse adapt(ServletResponse response) {
      return ServletReqResConverter.convert(response);
   }

   public static ServletRequest adapt(jakarta.servlet.ServletRequest request) {
      return ServletReqResConverter.convert(request);
   }

   public static jakarta.servlet.ServletRequest adapt(ServletRequest request) {
      return ServletReqResConverter.convert(request);
   }

   public static ServletContext adapt(jakarta.servlet.ServletContext context) {
      return new adapter.javax.servlet.ServletContext(context);
   }

   public static jakarta.servlet.ServletContext adapt(ServletContext context) {
      return new adapter.jakarta.servlet.ServletContext(context);
   }
}
