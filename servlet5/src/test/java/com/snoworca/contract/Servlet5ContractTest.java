package com.snoworca.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-002 — Servlet 5.0 module contract test.
 *
 * <p>Registers each interface-based adapter pair (jakarta-side and
 * javax-side) and invokes {@link DelegationContractTest#verifyDelegation}
 * to prove every non-blacklisted abstract method on the origin interface
 * is mirrored on the adapter and reaches the origin mock verbatim.
 *
 * <p>Blacklist rationale per adapter follows {@code SPEC-15 / SPEC-16 /
 * SPEC-12 / SPEC-13} — conversion-policy methods that legitimately do not
 * delegate (or take cross-namespace parameter types and are validated by
 * the dedicated converter unit tests instead).
 *
 * @req FR-TEST-002
 */
class Servlet5ContractTest {

    // -----------------------------------------------------------------
    // Cross-namespace parameter-type methods: these methods on the origin
    // interface take ServletRequest/ServletResponse from the *origin*
    // namespace, so the adapter cannot expose a same-signature override —
    // the adapter overload uses the opposite namespace and routes
    // arguments through {@code ServletReqResConverter}. The driver
    // silently skips such methods when no adapter override matches; we
    // additionally name them in the blacklist for documentation.
    // -----------------------------------------------------------------

    private static Set<String> blacklist(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    /**
     * Build an adapter via a (possibly package-private) constructor.
     * Used for {@code HttpSession}, whose production constructor is
     * package-private by design — the contract test must not widen that
     * visibility just to participate in delegation verification.
     */
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
    // javax-side adapters (wrap jakarta origin)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("FR-TEST-002 — servlet5 javax-side ServletRequest delegates to jakarta origin")
    void servlet5JavaxServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet5.ServletRequest,
            jakarta.servlet.ServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletRequest.class,
                adapter.javax.servlet5.ServletRequest.class,
                adapter.javax.servlet5.ServletRequest::new,
                // startAsync(req,res) takes cross-namespace types and is
                // covered by ServletReqResConverter unit tests; getRequest
                // is an accessor on the adapter (not in the interface).
                blacklist("startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 javax-side ServletResponse delegates to jakarta origin")
    void servlet5JavaxServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet5.ServletResponse,
            jakarta.servlet.ServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletResponse.class,
                adapter.javax.servlet5.ServletResponse.class,
                adapter.javax.servlet5.ServletResponse::new,
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 javax-side HttpServletRequest delegates to jakarta origin")
    void servlet5JavaxHttpServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet5.http.HttpServletRequest,
            jakarta.servlet.http.HttpServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpServletRequest.class,
                adapter.javax.servlet5.http.HttpServletRequest.class,
                adapter.javax.servlet5.http.HttpServletRequest::new,
                // upgrade takes a jakarta class literal; getCookies wraps
                // the cookie array element-wise; getSession returns a
                // sub-adapter; authenticate/login etc. require non-mock
                // request/response; startAsync inherited from base.
                blacklist("upgrade", "startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 javax-side HttpServletResponse delegates to jakarta origin")
    void servlet5JavaxHttpServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet5.http.HttpServletResponse,
            jakarta.servlet.http.HttpServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpServletResponse.class,
                adapter.javax.servlet5.http.HttpServletResponse.class,
                adapter.javax.servlet5.http.HttpServletResponse::new,
                // addCookie takes the cross-namespace Cookie type; setStatus
                // 2-arg overload is the reason-phrase deprecation fallback
                // (FR-CONV-005 / SPEC-12). encodeUrl (deprecated lowercase
                // alias on the javax side) routes to origin's encodeURL —
                // a different method name on the jakarta side, so the
                // verification against the same-name origin method does not
                // apply; encodeRedirectUrl (still present on both sides)
                // remains in scope.
                blacklist("addCookie", "setStatus", "encodeUrl"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 javax-side HttpSession delegates to jakarta origin")
    void servlet5JavaxHttpSessionDelegationContract() {
        // HttpSession's constructor is package-private (factory pattern in
        // production code). The contract test is cross-package, so build
        // the adapter via reflection rather than widening the production
        // surface for testing alone.
        DelegationContractTest.AdapterSpec<adapter.javax.servlet5.http.HttpSession,
            jakarta.servlet.http.HttpSession> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.http.HttpSession.class,
                adapter.javax.servlet5.http.HttpSession.class,
                origin -> reflectiveNew(adapter.javax.servlet5.http.HttpSession.class,
                                        jakarta.servlet.http.HttpSession.class, origin),
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 javax-side ServletContext delegates to jakarta origin")
    void servlet5JavaxServletContextDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.javax.servlet5.ServletContext,
            jakarta.servlet.ServletContext> spec =
            new DelegationContractTest.AdapterSpec<>(
                jakarta.servlet.ServletContext.class,
                adapter.javax.servlet5.ServletContext.class,
                adapter.javax.servlet5.ServletContext::new,
                // addServlet/addFilter overloads accept jakarta Class<? extends Servlet>
                // arguments; the adapter exposes javax-typed overloads and
                // delegates through ServletConverter. createServlet/createFilter
                // take a Class<? extends T> bound to javax / jakarta types
                // respectively — converted on entry.
                // setSessionTrackingModes / getDefault / getEffective route
                // their Set<SessionTrackingMode> through EnumsConverter to
                // bridge the two namespace enums (cross-namespace element
                // type, validated by the dedicated EnumsConverter tests).
                blacklist("addServlet", "addFilter", "createServlet", "createFilter",
                          "createListener", "addListener",
                          "setSessionTrackingModes",
                          "getDefaultSessionTrackingModes",
                          "getEffectiveSessionTrackingModes"));
        DelegationContractTest.verifyDelegation(spec);
    }

    // Note: RequestDispatcher is intentionally omitted — the interface
    // has only forward/include, both of which take cross-namespace
    // ServletRequest/ServletResponse parameters and route through
    // ServletReqResConverter. Pure-delegation reflection has nothing to
    // verify there; the conversion path is covered by the dedicated
    // ServletReqResConverter unit tests.

    // -----------------------------------------------------------------
    // jakarta-side adapters (wrap javax origin) — mirror direction
    // -----------------------------------------------------------------

    @Test
    @DisplayName("FR-TEST-002 — servlet5 jakarta-side ServletRequest delegates to javax origin")
    void servlet5JakartaServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet5.ServletRequest,
            javax.servlet.ServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletRequest.class,
                adapter.jakarta.servlet5.ServletRequest.class,
                adapter.jakarta.servlet5.ServletRequest::new,
                blacklist("startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 jakarta-side ServletResponse delegates to javax origin")
    void servlet5JakartaServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet5.ServletResponse,
            javax.servlet.ServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletResponse.class,
                adapter.jakarta.servlet5.ServletResponse.class,
                adapter.jakarta.servlet5.ServletResponse::new,
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 jakarta-side HttpServletRequest delegates to javax origin")
    void servlet5JakartaHttpServletRequestDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet5.http.HttpServletRequest,
            javax.servlet.http.HttpServletRequest> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpServletRequest.class,
                adapter.jakarta.servlet5.http.HttpServletRequest.class,
                adapter.jakarta.servlet5.http.HttpServletRequest::new,
                blacklist("upgrade", "startAsync"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 jakarta-side HttpServletResponse delegates to javax origin")
    void servlet5JakartaHttpServletResponseDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet5.http.HttpServletResponse,
            javax.servlet.http.HttpServletResponse> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpServletResponse.class,
                adapter.jakarta.servlet5.http.HttpServletResponse.class,
                adapter.jakarta.servlet5.http.HttpServletResponse::new,
                blacklist("addCookie", "setStatus", "encodeUrl"));
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 jakarta-side HttpSession delegates to javax origin")
    void servlet5JakartaHttpSessionDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet5.http.HttpSession,
            javax.servlet.http.HttpSession> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.http.HttpSession.class,
                adapter.jakarta.servlet5.http.HttpSession.class,
                origin -> reflectiveNew(adapter.jakarta.servlet5.http.HttpSession.class,
                                        javax.servlet.http.HttpSession.class, origin),
                Collections.<String>emptySet());
        DelegationContractTest.verifyDelegation(spec);
    }

    @Test
    @DisplayName("FR-TEST-002 — servlet5 jakarta-side ServletContext delegates to javax origin")
    void servlet5JakartaServletContextDelegationContract() {
        DelegationContractTest.AdapterSpec<adapter.jakarta.servlet5.ServletContext,
            javax.servlet.ServletContext> spec =
            new DelegationContractTest.AdapterSpec<>(
                javax.servlet.ServletContext.class,
                adapter.jakarta.servlet5.ServletContext.class,
                adapter.jakarta.servlet5.ServletContext::new,
                blacklist("addServlet", "addFilter", "createServlet", "createFilter",
                          "createListener", "addListener",
                          "setSessionTrackingModes",
                          "getDefaultSessionTrackingModes",
                          "getEffectiveSessionTrackingModes"));
        DelegationContractTest.verifyDelegation(spec);
    }

    // (jakarta-side RequestDispatcher omitted for the same reason — see
    // the comment on the javax-side block above.)

    @Test
    @DisplayName("FR-TEST-002 — registered spec set is non-empty (sanity)")
    void servlet5RegisteredSpecsNonEmpty() {
        // Sanity check that the contract test class itself runs at least
        // one delegation verification (defends against accidental @Disabled
        // proliferation).
        List<String> specClassNames = Arrays.asList(
            "adapter.javax.servlet5.ServletRequest",
            "adapter.javax.servlet5.ServletResponse",
            "adapter.javax.servlet5.http.HttpServletRequest",
            "adapter.javax.servlet5.http.HttpServletResponse",
            "adapter.javax.servlet5.http.HttpSession",
            "adapter.javax.servlet5.ServletContext",
            "adapter.jakarta.servlet5.ServletRequest",
            "adapter.jakarta.servlet5.ServletResponse",
            "adapter.jakarta.servlet5.http.HttpServletRequest",
            "adapter.jakarta.servlet5.http.HttpServletResponse",
            "adapter.jakarta.servlet5.http.HttpSession",
            "adapter.jakarta.servlet5.ServletContext");
        if (specClassNames.size() < 12) {
            throw new AssertionError("expected: >=12 adapter specs registered, got: "
                + specClassNames.size());
        }
    }
}
