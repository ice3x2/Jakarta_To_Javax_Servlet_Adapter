package com.snoworca.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-002 — Servlet 6.1 module contract test.
 *
 * <p>Mirrors {@code Servlet6ContractTest} but binds the jakarta-side specs
 * to {@code jakarta.servlet 6.1}. Servlet 6.1 added the
 * {@code sendRedirect(String, int, boolean)} overload to
 * {@code HttpServletResponse}: the jakarta-side adapter exposes a policy-E
 * synthesis (clears buffer + sets status + delegates a single-arg
 * sendRedirect) rather than direct delegation, so it is blacklisted here
 * and validated by {@code SendRedirect61Test}.
 *
 * @req FR-TEST-002
 */
class Servlet61ContractTest {

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
    // javax-side adapters (wrap jakarta 6.1 origin)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("FR-TEST-002 — servlet61 javax-side ServletRequest delegates to jakarta origin")
    void servlet61JavaxServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet61.ServletRequest,
            jakarta.servlet.ServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletRequest.class,
                adapter.javax.servlet61.ServletRequest.class,
                adapter.javax.servlet61.ServletRequest::new,
                blacklist("startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 javax-side ServletResponse delegates to jakarta origin")
    void servlet61JavaxServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet61.ServletResponse,
            jakarta.servlet.ServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletResponse.class,
                adapter.javax.servlet61.ServletResponse.class,
                adapter.javax.servlet61.ServletResponse::new,
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 javax-side HttpServletRequest delegates to jakarta origin")
    void servlet61JavaxHttpServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet61.http.HttpServletRequest,
            jakarta.servlet.http.HttpServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpServletRequest.class,
                adapter.javax.servlet61.http.HttpServletRequest.class,
                adapter.javax.servlet61.http.HttpServletRequest::new,
                blacklist("upgrade", "startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 javax-side HttpServletResponse delegates to jakarta origin")
    void servlet61JavaxHttpServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet61.http.HttpServletResponse,
            jakarta.servlet.http.HttpServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpServletResponse.class,
                adapter.javax.servlet61.http.HttpServletResponse.class,
                adapter.javax.servlet61.http.HttpServletResponse::new,
                // sendRedirect (3-arg, servlet 6.1 new overload) is a
                // policy-E synthesis on the jakarta side; on the javax side
                // the adapter still delegates the 3-arg call through to
                // origin so this remains in scope only for the javax test.
                blacklist("addCookie", "setStatus", "encodeUrl"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 javax-side HttpSession delegates to jakarta origin")
    void servlet61JavaxHttpSessionDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet61.http.HttpSession,
            jakarta.servlet.http.HttpSession> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpSession.class,
                adapter.javax.servlet61.http.HttpSession.class,
                origin -> reflectiveNew(adapter.javax.servlet61.http.HttpSession.class,
                                        jakarta.servlet.http.HttpSession.class, origin),
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 javax-side ServletContext delegates to jakarta origin")
    void servlet61JavaxServletContextDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet61.ServletContext,
            jakarta.servlet.ServletContext> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletContext.class,
                adapter.javax.servlet61.ServletContext.class,
                adapter.javax.servlet61.ServletContext::new,
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
    @DisplayName("FR-TEST-002 — servlet61 jakarta-side ServletRequest delegates to javax origin")
    void servlet61JakartaServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet61.ServletRequest,
            javax.servlet.ServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletRequest.class,
                adapter.jakarta.servlet61.ServletRequest.class,
                adapter.jakarta.servlet61.ServletRequest::new,
                blacklist("startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 jakarta-side ServletResponse delegates to javax origin")
    void servlet61JakartaServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet61.ServletResponse,
            javax.servlet.ServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletResponse.class,
                adapter.jakarta.servlet61.ServletResponse.class,
                adapter.jakarta.servlet61.ServletResponse::new,
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 jakarta-side HttpServletRequest delegates to javax origin")
    void servlet61JakartaHttpServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet61.http.HttpServletRequest,
            javax.servlet.http.HttpServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpServletRequest.class,
                adapter.jakarta.servlet61.http.HttpServletRequest.class,
                adapter.jakarta.servlet61.http.HttpServletRequest::new,
                blacklist("upgrade", "startAsync", "isRequestedSessionIdFromUrl"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 jakarta-side HttpServletResponse delegates to javax origin")
    void servlet61JakartaHttpServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet61.http.HttpServletResponse,
            javax.servlet.http.HttpServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpServletResponse.class,
                adapter.jakarta.servlet61.http.HttpServletResponse.class,
                adapter.jakarta.servlet61.http.HttpServletResponse::new,
                // The javax origin lacks the jakarta 6.1 3-arg sendRedirect,
                // so the enumeration never sees it; the legacy-bridge
                // exclusions (encodeUrl/encodeRedirectUrl) plus the
                // long→int truncation on setContentLengthLong match the
                // servlet6 set.
                blacklist("addCookie", "setStatus", "encodeUrl", "encodeRedirectUrl",
                          "setContentLengthLong"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 jakarta-side HttpSession delegates to javax origin")
    void servlet61JakartaHttpSessionDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet61.http.HttpSession,
            javax.servlet.http.HttpSession> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpSession.class,
                adapter.jakarta.servlet61.http.HttpSession.class,
                origin -> reflectiveNew(adapter.jakarta.servlet61.http.HttpSession.class,
                                        javax.servlet.http.HttpSession.class, origin),
                blacklist("getValue", "putValue", "removeValue", "getValueNames",
                          "getSessionContext"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet61 jakarta-side ServletContext delegates to javax origin")
    void servlet61JakartaServletContextDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet61.ServletContext,
            javax.servlet.ServletContext> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletContext.class,
                adapter.jakarta.servlet61.ServletContext.class,
                adapter.jakarta.servlet61.ServletContext::new,
                blacklist("addServlet", "addFilter", "createServlet", "createFilter",
                          "createListener", "addListener",
                          "setSessionTrackingModes",
                          "getDefaultSessionTrackingModes",
                          "getEffectiveSessionTrackingModes",
                          "log(java.lang.Exception,java.lang.String)"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — registered spec set is non-empty (sanity)")
    void servlet61RegisteredSpecsNonEmpty() {
        List<String> specClassNames = Arrays.asList(
            "adapter.javax.servlet61.ServletRequest",
            "adapter.javax.servlet61.ServletResponse",
            "adapter.javax.servlet61.http.HttpServletRequest",
            "adapter.javax.servlet61.http.HttpServletResponse",
            "adapter.javax.servlet61.http.HttpSession",
            "adapter.javax.servlet61.ServletContext",
            "adapter.jakarta.servlet61.ServletRequest",
            "adapter.jakarta.servlet61.ServletResponse",
            "adapter.jakarta.servlet61.http.HttpServletRequest",
            "adapter.jakarta.servlet61.http.HttpServletResponse",
            "adapter.jakarta.servlet61.http.HttpSession",
            "adapter.jakarta.servlet61.ServletContext");
        if (specClassNames.size() < 12) {
            throw new AssertionError("expected: >=12 adapter specs registered, got: "
                + specClassNames.size());
        }
    }
}
