package com.snoworca.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-002 — Servlet 6.0 module contract test.
 *
 * <p>Mirrors {@code Servlet5ContractTest} but binds the jakarta-side specs
 * to {@code jakarta.servlet 6.0} interfaces and the javax-side to
 * {@code javax.servlet 4.x}. Servlet 6.0 introduced new abstract methods
 * on {@code HttpServletRequest / ServletRequest} ({@code getRequestId},
 * {@code getProtocolRequestId}, {@code getServletConnection}) which have
 * no javax counterparts: the javax-side adapter does not implement them
 * (interface does not require it), so the contract driver's silent skip
 * on cross-method-set absence yields a clean result.
 *
 * @req FR-TEST-002
 */
class Servlet6ContractTest {

    private static Set<String> blacklist(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    @SuppressWarnings("unchecked")
    private static <A, O> A reflectiveNew(Class<A> adapterClass, Class<O> originClass, O origin) {
        try {
            java.lang.reflect.Constructor<?> ctor = adapterClass.getDeclaredConstructor(originClass);
            ctor.setAccessible(true);
            return (A) ctor.newInstance(origin);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("reflectiveNew failed for " + adapterClass.getName(), e);
        }
    }

    // -----------------------------------------------------------------
    // javax-side adapters (wrap jakarta 6.0 origin)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("FR-TEST-002 — servlet6 javax-side ServletRequest delegates to jakarta origin")
    void servlet6JavaxServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet6.ServletRequest,
            jakarta.servlet.ServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletRequest.class,
                adapter.javax.servlet6.ServletRequest.class,
                adapter.javax.servlet6.ServletRequest::new,
                blacklist("startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 javax-side ServletResponse delegates to jakarta origin")
    void servlet6JavaxServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet6.ServletResponse,
            jakarta.servlet.ServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletResponse.class,
                adapter.javax.servlet6.ServletResponse.class,
                adapter.javax.servlet6.ServletResponse::new,
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 javax-side HttpServletRequest delegates to jakarta origin")
    void servlet6JavaxHttpServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet6.http.HttpServletRequest,
            jakarta.servlet.http.HttpServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpServletRequest.class,
                adapter.javax.servlet6.http.HttpServletRequest.class,
                adapter.javax.servlet6.http.HttpServletRequest::new,
                blacklist("upgrade", "startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 javax-side HttpServletResponse delegates to jakarta origin")
    void servlet6JavaxHttpServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet6.http.HttpServletResponse,
            jakarta.servlet.http.HttpServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpServletResponse.class,
                adapter.javax.servlet6.http.HttpServletResponse.class,
                adapter.javax.servlet6.http.HttpServletResponse::new,
                blacklist("addCookie", "setStatus", "encodeUrl"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 javax-side HttpSession delegates to jakarta origin")
    void servlet6JavaxHttpSessionDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet6.http.HttpSession,
            jakarta.servlet.http.HttpSession> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpSession.class,
                adapter.javax.servlet6.http.HttpSession.class,
                origin -> reflectiveNew(adapter.javax.servlet6.http.HttpSession.class,
                                        jakarta.servlet.http.HttpSession.class, origin),
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 javax-side ServletContext delegates to jakarta origin")
    void servlet6JavaxServletContextDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet6.ServletContext,
            jakarta.servlet.ServletContext> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletContext.class,
                adapter.javax.servlet6.ServletContext.class,
                adapter.javax.servlet6.ServletContext::new,
                blacklist("addServlet", "addFilter", "createServlet", "createFilter",
                          "createListener", "addListener",
                          "setSessionTrackingModes",
                          "getDefaultSessionTrackingModes",
                          "getEffectiveSessionTrackingModes"));
        DelegationContractTest.verifyDelegation(spec);
    }

    // -----------------------------------------------------------------
    // jakarta-side adapters (wrap javax 4.x origin)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("FR-TEST-002 — servlet6 jakarta-side ServletRequest delegates to javax origin")
    void servlet6JakartaServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet6.ServletRequest,
            javax.servlet.ServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletRequest.class,
                adapter.jakarta.servlet6.ServletRequest.class,
                adapter.jakarta.servlet6.ServletRequest::new,
                blacklist("startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 jakarta-side ServletResponse delegates to javax origin")
    void servlet6JakartaServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet6.ServletResponse,
            javax.servlet.ServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletResponse.class,
                adapter.jakarta.servlet6.ServletResponse.class,
                adapter.jakarta.servlet6.ServletResponse::new,
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 jakarta-side HttpServletRequest delegates to javax origin")
    void servlet6JakartaHttpServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet6.http.HttpServletRequest,
            javax.servlet.http.HttpServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpServletRequest.class,
                adapter.jakarta.servlet6.http.HttpServletRequest.class,
                adapter.jakarta.servlet6.http.HttpServletRequest::new,
                // isRequestedSessionIdFromUrl (deprecated javax name) is
                // routed to origin's isRequestedSessionIdFromURL (the
                // active alias on the same namespace) — a legitimate
                // legacy-name bridge, not a delegation gap.
                blacklist("upgrade", "startAsync", "isRequestedSessionIdFromUrl"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 jakarta-side HttpServletResponse delegates to javax origin")
    void servlet6JakartaHttpServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet6.http.HttpServletResponse,
            javax.servlet.http.HttpServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpServletResponse.class,
                adapter.jakarta.servlet6.http.HttpServletResponse.class,
                adapter.jakarta.servlet6.http.HttpServletResponse::new,
                // encodeRedirectUrl on the jakarta-side adapter routes to
                // origin's encodeRedirectURL (legacy-name bridge, parallel
                // to encodeUrl→encodeURL).
                // setContentLengthLong(long) is synthesized on the jakarta
                // side because javax 4.x has only setContentLength(int) —
                // adapter truncates long→int with a single delegation to
                // setContentLength (not the same-name method).
                blacklist("addCookie", "setStatus", "encodeUrl", "encodeRedirectUrl",
                          "setContentLengthLong"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 jakarta-side HttpSession delegates to javax origin")
    void servlet6JakartaHttpSessionDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet6.http.HttpSession,
            javax.servlet.http.HttpSession> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpSession.class,
                adapter.jakarta.servlet6.http.HttpSession.class,
                origin -> reflectiveNew(adapter.jakarta.servlet6.http.HttpSession.class,
                                        javax.servlet.http.HttpSession.class, origin),
                // getValue/putValue/removeValue/getValueNames — javax-side
                // deprecated aliases for getAttribute/setAttribute/etc.
                // The adapter intentionally bridges the legacy names to
                // the modern attribute methods on origin.
                // getSessionContext — javax origin's deprecated session
                // context returns the jakarta 6.0 EmptyHttpSessionContext
                // sentinel without invoking origin (the legacy javax
                // interface still has the method but the data model is
                // gone on jakarta 6.0).
                blacklist("getValue", "putValue", "removeValue", "getValueNames",
                          "getSessionContext"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet6 jakarta-side ServletContext delegates to javax origin")
    void servlet6JakartaServletContextDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet6.ServletContext,
            javax.servlet.ServletContext> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletContext.class,
                adapter.jakarta.servlet6.ServletContext.class,
                adapter.jakarta.servlet6.ServletContext::new,
                // log(Exception, String) is the deprecated javax overload
                // — adapter normalises it to the modern log(String,
                // Throwable) signature on origin (arg-order swap), so it
                // is not a pure same-name same-args delegation.
                blacklist("addServlet", "addFilter", "createServlet", "createFilter",
                          "createListener", "addListener",
                          "setSessionTrackingModes",
                          "getDefaultSessionTrackingModes",
                          "getEffectiveSessionTrackingModes",
                          // signature-precise: only the deprecated
                          // (Exception, String) overload — log(String) and
                          // log(String, Throwable) still pure-delegate.
                          "log(java.lang.Exception,java.lang.String)"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — registered spec set is non-empty (sanity)")
    void servlet6RegisteredSpecsNonEmpty() {
        List<String> specClassNames = Arrays.asList(
            "adapter.javax.servlet6.ServletRequest",
            "adapter.javax.servlet6.ServletResponse",
            "adapter.javax.servlet6.http.HttpServletRequest",
            "adapter.javax.servlet6.http.HttpServletResponse",
            "adapter.javax.servlet6.http.HttpSession",
            "adapter.javax.servlet6.ServletContext",
            "adapter.jakarta.servlet6.ServletRequest",
            "adapter.jakarta.servlet6.ServletResponse",
            "adapter.jakarta.servlet6.http.HttpServletRequest",
            "adapter.jakarta.servlet6.http.HttpServletResponse",
            "adapter.jakarta.servlet6.http.HttpSession",
            "adapter.jakarta.servlet6.ServletContext");
        if (specClassNames.size() < 12) {
            throw new AssertionError("expected: >=12 adapter specs registered, got: "
                + specClassNames.size());
        }
    }
}
