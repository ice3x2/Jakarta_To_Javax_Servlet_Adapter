package adapter.javax.servlet6.http;

import adapter.common.AdapterUnwrap;

import java.util.Enumeration;

/** @deprecated placeholder — jakarta.servlet.http.HttpSessionContext was removed in Servlet 6.0 */
@SuppressWarnings("ALL")
@Deprecated
// @req FR-CONV-003
// @req FR-CONV-004
public class HttpSessionContext implements javax.servlet.http.HttpSessionContext, AdapterUnwrap<Object> {

   HttpSessionContext() {
   }

   // @req FR-CONV-003
   // placeholder: jakarta.servlet.http.HttpSessionContext was removed in Servlet 6.0,
   // so no underlying delegate exists. unwrap() returns null per placeholder contract.
   @Override public Object unwrap() {
      return null;
   }

   // @req FR-CONV-004
   @Override public boolean equals(Object o) {
      if (this == o) return true;
      return false;
   }

   // @req FR-CONV-004
   @Override public int hashCode() {
      return System.identityHashCode(this);
   }

   @Override public javax.servlet.http.HttpSession getSession(String sessionId) {
      // placeholder pending PH-004
      return null;
   }

   @Override public Enumeration<String> getIds() {
      // placeholder pending PH-004
      return java.util.Collections.emptyEnumeration();
   }
}
