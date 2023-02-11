package adapter.jakarta.servlet.http;

public class Cookie extends jakarta.servlet.http.Cookie {
   javax.servlet.http.Cookie cookie;

   public Cookie(javax.servlet.http.Cookie cookie) {
      super("ignored", "ignored");
      this.cookie = cookie;
   }

   public void setComment(String purpose) {
      this.cookie.setComment(purpose);
   }

   public String getComment() {
      return this.cookie.getComment();
   }

   public void setDomain(String pattern) {
      this.cookie.setDomain(pattern);
   }

   public String getDomain() {
      return this.cookie.getDomain();
   }

   public void setMaxAge(int expiry) {
      this.cookie.setMaxAge(expiry);
   }

   public int getMaxAge() {
      return this.cookie.getMaxAge();
   }

   public void setPath(String uri) {
      this.cookie.setPath(uri);
   }

   public String getPath() {
      return this.cookie.getPath();
   }

   public void setSecure(boolean flag) {
      this.cookie.setSecure(flag);
   }

   public boolean getSecure() {
      return this.cookie.getSecure();
   }

   public String getName() {
      return this.cookie.getName();
   }

   public void setValue(String newValue) {
      this.cookie.setValue(newValue);
   }

   public String getValue() {
      return this.cookie.getValue();
   }

   public int getVersion() {
      return this.cookie.getVersion();
   }

   public void setVersion(int v) {
      this.cookie.setVersion(v);
   }

   public Object clone() {
      return new Cookie((javax.servlet.http.Cookie)this.cookie.clone());
   }

   public void setHttpOnly(boolean httpOnly) {
      this.cookie.setHttpOnly(httpOnly);
   }

   public boolean isHttpOnly() {
      return this.cookie.isHttpOnly();
   }
}
