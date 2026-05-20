package adapter.javax.servlet5.http;

import adapter.common.AdapterUnwrap;
import adapter.common.ConverterSupport;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

public class Cookie extends javax.servlet.http.Cookie implements AdapterUnwrap<jakarta.servlet.http.Cookie> {

   /**
    * FR-CONV-013 — SPEC-15 정책 D. jakarta 6.0+ Cookie 의 attribute API 가 javax 4.x
    * 에는 부재하므로, no-op + WARN-once 디스패치 시 dedup 용 methodKey.
    *
    * @req FR-CONV-013
    */
   private static final String ATTRIBUTE_METHOD_KEY =
           "Cookie.setAttribute@jakarta->javax";

   private final jakarta.servlet.http.Cookie cookie;

    public Cookie(jakarta.servlet.http.Cookie cookie) {
      super("ignored", "ignored");
      this.cookie = cookie;
   }

   // @req FR-CONV-003
   @Override public jakarta.servlet.http.Cookie unwrap() {
      return this.cookie;
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

   @Override public void setComment(String purpose) {
      this.cookie.setComment(purpose);
   }

   @Override public String getComment() {
      return this.cookie.getComment();
   }

   @Override public void setDomain(String pattern) {
      this.cookie.setDomain(pattern);
   }

   @Override public String getDomain() {
      return this.cookie.getDomain();
   }

   @Override public void setMaxAge(int expiry) {
      this.cookie.setMaxAge(expiry);
   }

   @Override public int getMaxAge() {
      return this.cookie.getMaxAge();
   }

   @Override public void setPath(String uri) {
      this.cookie.setPath(uri);
   }

   @Override public String getPath() {
      return this.cookie.getPath();
   }

   @Override public void setSecure(boolean flag) {
      this.cookie.setSecure(flag);
   }

   @Override public boolean getSecure() {
      return this.cookie.getSecure();
   }

   @Override public String getName() {
      return this.cookie.getName();
   }

   @Override public void setValue(String newValue) {
      this.cookie.setValue(newValue);
   }

   @Override public String getValue() {
      return this.cookie.getValue();
   }

   @Override public int getVersion() {
      return this.cookie.getVersion();
   }

   @Override public void setVersion(int v) {
      this.cookie.setVersion(v);
   }


   @Override public Cookie clone() {
      return new Cookie((jakarta.servlet.http.Cookie)this.cookie.clone());
   }

   @Override public void setHttpOnly(boolean httpOnly) {
      this.cookie.setHttpOnly(httpOnly);
   }

   @Override public boolean isHttpOnly() {
      return this.cookie.isHttpOnly();
   }

   /**
    * FR-CONV-013 / SPEC-15 정책 D — jakarta 6.0+ {@code setAttribute(String,String)}
    * 호출은 no-op 으로 처리하고 {@link ConverterSupport#logFallbackOnce} 로 1회만 WARN
    * 을 emit 한다 (methodKey {@value #ATTRIBUTE_METHOD_KEY}). 자체 attribute Map 은
    * 보관하지 않는다.
    *
    * @req FR-CONV-013
    */
   public void setAttribute(String name, String value) {
      ConverterSupport.logFallbackOnce(ATTRIBUTE_METHOD_KEY, Level.WARNING,
              "no-op (javax 4.x has no Cookie attribute API)");
      ConverterSupport.incrementDiagnostics(ATTRIBUTE_METHOD_KEY);
   }

   /**
    * FR-CONV-013 / SPEC-15 정책 D — 보관소가 없으므로 항상 {@code null} 을 반환한다.
    *
    * @req FR-CONV-013
    */
   public String getAttribute(String name) {
      return null;
   }

   /**
    * FR-CONV-013 / SPEC-15 정책 D — 항상 동일한 {@link Collections#emptyMap()} sentinel
    * 을 반환한다. 인스턴스별 새 Map 을 생성하지 않으며, mutation 시 호출 측에서
    * {@link UnsupportedOperationException} 이 발생한다.
    *
    * @req FR-CONV-013
    */
   public Map<String, String> getAttributes() {
      return Collections.emptyMap();
   }
}
