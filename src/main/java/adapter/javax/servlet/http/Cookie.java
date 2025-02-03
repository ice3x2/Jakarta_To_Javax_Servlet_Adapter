package adapter.javax.servlet.http;

public class Cookie extends javax.servlet.http.Cookie {
   private final jakarta.servlet.http.Cookie cookie;

    public Cookie(jakarta.servlet.http.Cookie cookie) {
      super("ignored", "ignored");
      this.cookie = cookie;
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
}
