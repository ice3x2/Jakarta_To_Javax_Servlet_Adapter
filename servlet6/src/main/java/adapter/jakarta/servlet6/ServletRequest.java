package adapter.jakarta.servlet6;

import adapter.common.AdapterUnwrap;

import adapter.servletElementConverter6.AsyncContextConverter;
import adapter.servletElementConverter6.ServletReqResConverter;
import adapter.servletElementConverter6.StreamConverter;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

 public class ServletRequest implements jakarta.servlet.ServletRequest, AdapterUnwrap<javax.servlet.ServletRequest> {
    private final javax.servlet.ServletRequest request;

    public ServletRequest(javax.servlet.ServletRequest request) {
      this.request = request;
   }

   // @req FR-CONV-003
   @Override public javax.servlet.ServletRequest unwrap() {
      return this.request;
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

    @SuppressWarnings("unused")
    public javax.servlet.ServletRequest getRequest() {
      return this.request;
   }

   @Override public Object getAttribute(String name) {
      return this.request.getAttribute(name);
   }

   @Override public Enumeration<String> getAttributeNames() {
      return this.request.getAttributeNames();
   }

   @Override public String getCharacterEncoding() {
      return this.request.getCharacterEncoding();
   }

   @Override public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
      this.request.setCharacterEncoding(env);
   }

   @Override public int getContentLength() {
      return this.request.getContentLength();
   }

   @Override public String getContentType() {
      return this.request.getContentType();
   }

   @Override public ServletInputStream getInputStream() throws IOException {
      return StreamConverter.convert(this.request.getInputStream());
   }

   @Override public String getParameter(String name) {
      return this.request.getParameter(name);
   }

   @Override public Enumeration<String> getParameterNames() {
      return this.request.getParameterNames();
   }

   @Override public String[] getParameterValues(String name) {
      return this.request.getParameterValues(name);
   }

   @Override public Map<String, String[]> getParameterMap() {
      return this.request.getParameterMap();
   }

   @Override public String getProtocol() {
      return this.request.getProtocol();
   }

   @Override public String getScheme() {
      return this.request.getScheme();
   }

   @Override public String getServerName() {
      return this.request.getServerName();
   }

   @Override public int getServerPort() {
      return this.request.getServerPort();
   }

   @Override public BufferedReader getReader() throws IOException {
      return this.request.getReader();
   }

   @Override public String getRemoteAddr() {
      return this.request.getRemoteAddr();
   }

   @Override public String getRemoteHost() {
      return this.request.getRemoteHost();
   }

   @Override public void setAttribute(String name, Object o) {
      this.request.setAttribute(name, o);
   }

   @Override public void removeAttribute(String name) {
      this.request.removeAttribute(name);
   }

   @Override public Locale getLocale() {
      return this.request.getLocale();
   }

   @Override public Enumeration<Locale> getLocales() {
      return this.request.getLocales();
   }

   @Override public boolean isSecure() {
      return this.request.isSecure();
   }

   @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
      return new RequestDispatcher(this.request.getRequestDispatcher(path));
   }

   // @Override removed: getRealPath(String) removed from jakarta.servlet.ServletRequest in 6.0 (PH-004 fallback pending)
   public String getRealPath(String path) {
       //noinspection deprecation
       return this.request.getRealPath(path);
   }

   // @req FR-CONV-001
   private static final String REQUEST_ID_ATTR = "com.snoworca.adapter.requestId";

   // @req FR-CONV-001 — lazy UUID 캐시. wrapper 인스턴스 단위 보장 (attribute store 가
   // 동작하지 않는 fake/proxy 환경에서도 동일 UUID 반환).
   private volatile String cachedRequestId;

   // @req FR-CONV-001
   @Override public jakarta.servlet.ServletConnection getServletConnection() {
      // javax 측 원본에는 ServletConnection 자체가 없음 — 어댑터가 합성 폴백 반환.
      return new DummyServletConnection();
   }

   // @req FR-CONV-001
   @Override public String getRequestId() {
      // jakarta 6.0 신규 추상 메서드 — request attribute 우선 + wrapper 인스턴스 캐시.
      String local = this.cachedRequestId;
      if (local != null) {
         return local;
      }
      Object attr = this.request.getAttribute(REQUEST_ID_ATTR);
      if (attr instanceof String) {
         this.cachedRequestId = (String) attr;
         return this.cachedRequestId;
      }
      String generated = UUID.randomUUID().toString();
      this.request.setAttribute(REQUEST_ID_ATTR, generated);
      this.cachedRequestId = generated;
      return generated;
   }

   // @req FR-CONV-001
   @Override public String getProtocolRequestId() {
      // javax 측 원본에는 protocol-level request id 개념이 없음 — 빈 문자열 반환.
      return "";
   }

   @Override public int getRemotePort() {
      return this.request.getRemotePort();
   }

   @Override public String getLocalName() {
      return this.request.getLocalName();
   }

   @Override public String getLocalAddr() {
      return this.request.getLocalAddr();
   }

   @Override public int getLocalPort() {
      return this.request.getLocalPort();
   }

   @Override public jakarta.servlet.ServletContext getServletContext() {
      return new ServletContext(this.request.getServletContext());
   }

   @Override public AsyncContext startAsync() {
      return AsyncContextConverter.convert(this.request.startAsync());
   }

   @Override public boolean isAsyncStarted() {
      return this.request.isAsyncStarted();
   }

   @Override public boolean isAsyncSupported() {
      return this.request.isAsyncSupported();
   }

   @Override public AsyncContext getAsyncContext() {
      return AsyncContextConverter.convert(this.request.getAsyncContext());
   }

   @Override public DispatcherType getDispatcherType() {
      return DispatcherType.valueOf(this.request.getDispatcherType().name());
   }

   @Override public AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) {
      return AsyncContextConverter.convert(this.request.startAsync(ServletReqResConverter.convert(servletRequest), ServletReqResConverter.convert(servletResponse)));
   }

   @Override public long getContentLengthLong() {
      return this.request.getContentLengthLong();
   }
}
