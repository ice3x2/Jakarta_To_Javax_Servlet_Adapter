package adapter.jakarta.servlet6.http;

import adapter.common.AdapterUnwrap;

import java.util.Collections;
import java.util.Enumeration;

/**
 * Empty HttpSessionContext dummy — SRS §6.2.2 policy D (소거된 메서드 대체) for
 * {@code HttpSession.getSessionContext()}.
 *
 * <p>jakarta.servlet 6.0 removed {@code HttpSessionContext} entirely. javax.servlet 4.0
 * still has it (deprecated) but it has been required by spec to return an empty context
 * since Servlet 2.1. The adapter's jakarta→javax {@code HttpSession.getSessionContext()}
 * therefore returns this empty dummy instead of delegating to the original (which itself
 * is contractually empty but exists only as a deprecated stub).
 *
 * <p>Implements {@link AdapterUnwrap} with a {@code null} delegate (policy D — no original
 * is held). FR-CONV-003 marker contract is satisfied by interface presence.
 *
 * @req FR-CONV-010
 * @req FR-CONV-014
 */
public final class EmptyHttpSessionContext implements AdapterUnwrap<Void> {

   // @req FR-CONV-010
   EmptyHttpSessionContext() {
   }

   // @req FR-CONV-003
   @Override public Void unwrap() {
      return null;
   }

   /**
    * Always {@code null} — per HttpSessionContext spec since Servlet 2.1.
    *
    * @req FR-CONV-010
    */
   public jakarta.servlet.http.HttpSession getSession(String sessionId) {
      return null;
   }

   /**
    * Always an empty enumeration — per HttpSessionContext spec since Servlet 2.1.
    *
    * @req FR-CONV-010
    */
   public Enumeration<String> getIds() {
      return Collections.emptyEnumeration();
   }
}
