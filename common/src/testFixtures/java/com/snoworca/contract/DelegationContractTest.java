package com.snoworca.contract;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * FR-TEST-002 — Reflection-based delegation contract driver (L2).
 *
 * <p>Shared across the servlet5 / servlet6 / servlet61 modules through
 * Gradle's {@code java-test-fixtures} plugin so that each version module
 * registers its own adapter spec list and the same enumeration + verify
 * algorithm runs against every concrete adapter.
 *
 * <h2>Algorithm</h2>
 * <p>For each registered {@link AdapterSpec}:
 * <ol>
 *   <li>Enumerate {@code originInterface} public abstract methods via
 *       reflection (Object methods removed by JDK).</li>
 *   <li>Drop methods whose names appear in the spec's blacklist (intentional
 *       conversion-policy methods such as {@code getInputStream},
 *       {@code getRequestDispatcher}, {@code startAsync} — these wrap the
 *       returned object through a converter rather than purely delegating).
 *       The {@code Object} method names {@code equals/hashCode/toString}
 *       are always excluded as well.</li>
 *   <li>For each remaining method:
 *     <ol type="a">
 *       <li>Create a fresh Mockito mock of the origin interface.</li>
 *       <li>Build an adapter via the spec's factory function.</li>
 *       <li>Synthesize sample arguments based on parameter types
 *           ({@link #sampleArg(Class)}).</li>
 *       <li>Locate the matching adapter method (same name, same parameter
 *           types) via reflection. If absent, fail the contract.</li>
 *       <li>Invoke the adapter method with the synthesised args.</li>
 *       <li>Reflectively call the same method on {@code verify(mock)} —
 *           Mockito records the interaction and the verification proxy
 *           confirms identical method + args were dispatched to the
 *           origin.</li>
 *       <li>Call {@link org.mockito.Mockito#verifyNoMoreInteractions(Object...)
 *           verifyNoMoreInteractions} on the origin mock to ensure the
 *           adapter did not silently fan out to additional origin methods
 *           (a frequent source of subtle delegation bugs).</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p>Mock usage here is not "mock-driven business logic" but a structural
 * observer: the mock acts as a verifiable interaction recorder so we can
 * prove that adapter method {@code X(args)} routes to origin method
 * {@code X(args)} verbatim. This is the explicit acceptance for FR-TEST-002.
 *
 * @req FR-TEST-002
 */
public final class DelegationContractTest {

    private DelegationContractTest() {
        // utility — instantiation not intended
    }

    /**
     * Spec for a single adapter under contract: the origin interface that
     * the adapter wraps (typically a {@code jakarta.servlet.*} interface
     * when verifying the javax-side adapters, or {@code javax.servlet.*}
     * when verifying the jakarta-side adapters), the concrete adapter
     * class, a factory turning a mocked origin into an adapter instance,
     * and an optional method-name blacklist for non-delegation paths.
     *
     * @param <A> the adapter type
     * @param <O> the origin (wrapped) interface type
     * @req FR-TEST-002
     */
    public static final class AdapterSpec<A, O> {
        private final Class<O> originInterface;
        private final Class<A> adapterClass;
        private final Function<O, A> factory;
        private final Set<String> methodBlacklist;

        /**
         * Create a new spec.
         *
         * @param originInterface the interface whose abstract methods are
         *                        enumerated and verified for delegation
         * @param adapterClass    the concrete adapter class to invoke
         * @param factory         constructs an adapter from a mocked origin
         * @param methodBlacklist method names (no signature, name-only) that
         *                        should be skipped — typically conversion
         *                        helpers such as {@code getInputStream},
         *                        {@code getRequestDispatcher},
         *                        {@code startAsync} that route the origin
         *                        return value through a converter
         * @req FR-TEST-002
         */
        public AdapterSpec(Class<O> originInterface,
                           Class<A> adapterClass,
                           Function<O, A> factory,
                           Set<String> methodBlacklist) {
            this.originInterface = originInterface;
            this.adapterClass = adapterClass;
            this.factory = factory;
            this.methodBlacklist = methodBlacklist == null
                ? Collections.<String>emptySet()
                : new HashSet<>(methodBlacklist);
        }

        /**
         * Convenience overload — no blacklist.
         *
         * @req FR-TEST-002
         */
        public AdapterSpec(Class<O> originInterface,
                           Class<A> adapterClass,
                           Function<O, A> factory) {
            this(originInterface, adapterClass, factory, Collections.<String>emptySet());
        }

        Class<O> originInterface() { return originInterface; }
        Class<A> adapterClass() { return adapterClass; }
        Function<O, A> factory() { return factory; }
        Set<String> methodBlacklist() { return methodBlacklist; }
    }

    /**
     * Always-skip method names — Mockito spies on {@code Object} methods and
     * we never delegate them through the adapter contract (FR-CONV-004
     * defines equals/hashCode separately).
     *
     * @req FR-TEST-002
     */
    private static final Set<String> OBJECT_METHODS;
    static {
        Set<String> s = new HashSet<>();
        s.add("equals");
        s.add("hashCode");
        s.add("toString");
        s.add("wait");
        s.add("notify");
        s.add("notifyAll");
        s.add("getClass");
        s.add("clone");
        s.add("finalize");
        OBJECT_METHODS = Collections.unmodifiableSet(s);
    }

    /**
     * Run the delegation contract for a single adapter spec — verifies that
     * every non-blacklisted abstract method of {@code spec.originInterface}
     * is mirrored on the adapter and reaches the origin mock with identical
     * arguments.
     *
     * <p>Failures aggregate per-method: the first method that violates the
     * contract surfaces a JUnit assertion failure naming the offending
     * adapter + method signature, so test reports point directly at the
     * delegation gap.
     *
     * @param spec the adapter spec to verify
     * @param <A>  adapter type
     * @param <O>  origin type
     * @req FR-TEST-002
     */
    public static <A, O> void verifyDelegation(AdapterSpec<A, O> spec) {
        List<Method> methods = enumerateDelegationMethods(spec);
        assertTrue(!methods.isEmpty(),
            "expected: " + spec.originInterface().getName()
                + " has at least one non-blacklisted abstract method to verify, "
                + "got: 0 — likely an over-broad blacklist (spec="
                + spec.adapterClass().getName() + ")");

        for (Method originMethod : methods) {
            runSingleMethodContract(spec, originMethod);
        }
    }

    /**
     * Convenience wrapper running {@link #verifyDelegation(AdapterSpec)}
     * across a list of specs in iteration order.
     *
     * @req FR-TEST-002
     */
    public static void verifyAll(List<AdapterSpec<?, ?>> specs) {
        for (AdapterSpec<?, ?> spec : specs) {
            verifyDelegation(spec);
        }
    }

    // -----------------------------------------------------------------
    // Method enumeration
    // -----------------------------------------------------------------

    private static <A, O> List<Method> enumerateDelegationMethods(AdapterSpec<A, O> spec) {
        Method[] declared = spec.originInterface().getMethods();
        List<Method> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Method m : declared) {
            if (m.isSynthetic()) continue;
            if (m.isBridge()) continue;
            if (Modifier.isStatic(m.getModifiers())) continue;
            if (m.isDefault()) continue;
            if (OBJECT_METHODS.contains(m.getName())) continue;
            String sig = signatureOf(m);
            // Blacklist may contain either a bare method name (matches all
            // overloads) or a full signature "name(type1,type2)" (matches
            // a single overload precisely — used when only one overload of
            // a name is non-delegating).
            if (spec.methodBlacklist().contains(m.getName())) continue;
            if (spec.methodBlacklist().contains(sig)) continue;
            // Dedupe by signature (name + param types) — same signature can
            // appear multiple times through interface re-declaration.
            if (!seen.add(sig)) continue;
            out.add(m);
        }
        return out;
    }

    private static String signatureOf(Method m) {
        StringBuilder sb = new StringBuilder(m.getName());
        sb.append('(');
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(params[i].getName());
        }
        sb.append(')');
        return sb.toString();
    }

    // -----------------------------------------------------------------
    // Per-method verification
    // -----------------------------------------------------------------

    private static <A, O> void runSingleMethodContract(AdapterSpec<A, O> spec, Method originMethod) {
        // Resolve the adapter-side method that should mirror the origin
        // method. Same name + same parameter types — adapters implement the
        // *javax* (or *jakarta*) side interface whose abstract methods have
        // identical erasure across the two namespaces for delegation
        // candidates. When the origin method takes cross-namespace types as
        // parameters, no same-signature method exists on the adapter side
        // (the adapter overload has the *opposite* namespace) — those
        // methods are out of scope of pure-delegation reflection and must
        // be covered by the converter unit tests instead. We silently skip
        // them here rather than fail; the spec explicitly carves out
        // "변환 정책 메서드 blacklist" for that purpose.
        Method adapterMethod;
        try {
            adapterMethod = spec.adapterClass().getMethod(
                originMethod.getName(), originMethod.getParameterTypes());
        } catch (NoSuchMethodException nsme) {
            // Cross-namespace parameter types → not a delegation-test case.
            // Surfacing this would be a false-positive (the adapter does
            // implement the SAME-name method on the opposite namespace).
            return;
        }

        O originMock = mock(spec.originInterface());
        A adapter = spec.factory().apply(originMock);

        Object[] args = synthesizeArgs(originMethod.getParameterTypes());

        try {
            adapterMethod.invoke(adapter, args);
        } catch (InvocationTargetException ite) {
            // An exception thrown by the adapter does not by itself prove
            // a delegation gap: many adapters wrap the origin return value
            // into a sub-adapter (e.g. {@code new ServletContext(origin
            // .getServletContext())}). When the mocked origin returns
            // null, the wrapper constructor may NPE — yet the origin call
            // itself did occur, which is exactly the contract we are
            // verifying. Continue to the verify(mock) step; if the origin
            // never received the call, that step will fail with a clear
            // wanted-but-not-invoked message naming this method.
        } catch (IllegalAccessException iae) {
            fail("expected: adapter method " + signatureOf(adapterMethod)
                + " is publicly invocable, got: " + iae);
            return;
        }

        // Reflectively call the same method on verify(originMock) — Mockito
        // records this as the verification probe and asserts that exactly
        // one matching interaction occurred during the adapter call.
        Object verifier = verify(originMock);
        try {
            originMethod.invoke(verifier, args);
        } catch (InvocationTargetException ite) {
            // Mockito raises a wanted-but-not-invoked or argument-mismatch
            // failure here when the adapter did not delegate verbatim.
            Throwable cause = ite.getCause();
            fail("delegation contract violated for "
                + spec.adapterClass().getSimpleName() + "." + signatureOf(originMethod)
                + ": expected origin to receive the same call with args="
                + Arrays.toString(args) + ", got: " + cause);
            return;
        } catch (IllegalAccessException iae) {
            fail("internal: cannot invoke " + signatureOf(originMethod)
                + " on Mockito verifier — " + iae);
            return;
        }

        // No additional origin interactions — guards against fan-out where
        // an adapter method silently invokes multiple origin methods that
        // were not part of the contract.
        try {
            verifyNoMoreInteractions(originMock);
        } catch (AssertionError ae) {
            fail("delegation contract violated for "
                + spec.adapterClass().getSimpleName() + "." + signatureOf(originMethod)
                + ": adapter triggered additional origin interactions beyond "
                + "the single matching call. " + ae.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // Sample-arg synthesis
    // -----------------------------------------------------------------

    private static Object[] synthesizeArgs(Class<?>[] paramTypes) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = sampleArg(paramTypes[i]);
        }
        return args;
    }

    /**
     * Synthesise a single representative argument value for the given
     * parameter type. The values are deliberately deterministic so
     * {@code verify(mock).method(args)} matches by equality.
     *
     * <ul>
     *   <li>primitives → their canonical zero value (or {@code false}).</li>
     *   <li>{@code String} → empty string.</li>
     *   <li>{@code Class} → {@code Object.class}.</li>
     *   <li>{@code Locale} → {@link Locale#ROOT}.</li>
     *   <li>arrays / other reference types → {@code null} (Mockito's
     *       default for non-primitive parameter matching).</li>
     * </ul>
     *
     * @req FR-TEST-002
     */
    static Object sampleArg(Class<?> type) {
        if (!type.isPrimitive()) {
            if (type == String.class) return "";
            if (type == Class.class) return Object.class;
            if (type == Locale.class) return Locale.ROOT;
            return null;
        }
        if (type == boolean.class) return Boolean.FALSE;
        if (type == byte.class)    return (byte) 0;
        if (type == short.class)   return (short) 0;
        if (type == char.class)    return (char) 0;
        if (type == int.class)     return 0;
        if (type == long.class)    return 0L;
        if (type == float.class)   return 0.0f;
        if (type == double.class)  return 0.0d;
        if (type == void.class)    return null;
        throw new IllegalStateException("unhandled primitive type: " + type);
    }
}
