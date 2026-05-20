package com.snoworca;

import org.junit.jupiter.api.Test;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-010 (폴백 정책 매트릭스 — SPEC-7) +
 * FR-CONV-014 (모듈별 적용 범위 — SPEC-14) for the servlet61 module.
 *
 * Sibling of servlet6/.../FallbackPolicyTest.java. servlet61 모듈 관점에서
 * 차이점은 FR-CONV-014 AC-3 만 — servlet 6.1 신규 오버로드
 * {@code sendRedirect(String location, int sc, boolean clearBuffer)} 의
 * 명시 override (SRS §6.2.2 표) 가 본 모듈에서만 검증된다. 나머지 5 case
 * (FR-CONV-010 AC1/2/3 + FR-CONV-014 AC1/2) 는 servlet6 sibling 과 동일한
 * 의미론 (jakarta→javax 6+ 원본 비대칭 메서드 폴백) 으로 검증한다.
 *
 * Scope (T-PH004-13 — RED):
 *   FR-CONV-010 AC-1: HttpSession.getValue/putValue policy B.
 *   FR-CONV-010 AC-2: HttpServletResponse.setStatus(int, String) policy E1.
 *   FR-CONV-010 AC-3: HttpSession.getSessionContext policy D
 *                     (EmptyHttpSessionContext dummy).
 *   FR-CONV-014 AC-1: servlet5 dormant + servlet61 신설 의무
 *                     (EmptyHttpSessionContext 신설).
 *   FR-CONV-014 AC-2: servlet61 모듈에서 정책 B 진입 (module restatement).
 *   FR-CONV-014 AC-3: servlet61 sendRedirect(loc, sc, clearBuffer) 명시
 *                     override — 원본의 sendRedirect 위임 금지, 대신
 *                     setStatus(sc) + setHeader("Location", loc) + (옵션)
 *                     resetBuffer + flushBuffer 가 호출된다.
 *
 * Red phase 의도 (sibling 과 동일):
 *   - 현재 시점 servlet61 의 HttpServletResponse.sendRedirect(loc, sc,
 *     clearBuffer) 는 PH-004 placeholder 로 원본의 sendRedirect(loc) 를
 *     호출한다 (setStatus/setHeader/clearBuffer 미수행).
 *   - HttpSession.getValue/putValue/getSessionContext, setStatus(int,String),
 *     ServletContext.log(Exception,String) 는 모두 원본의 deprecated
 *     메서드를 직접 위임.
 *   - EmptyHttpSessionContext 부재.
 *
 * expected_failure_signature (sidecar 6 case 공통):
 *   "expected: 6 asymmetric method groups follow SRS policy matrix,
 *    got: at least one wrong branch"
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-010-AC1-01 → _FRCONV010_AC1
 *   TC-REQ-FR-CONV-010-AC2-01 → _FRCONV010_AC2
 *   TC-REQ-FR-CONV-010-AC3-01 → _FRCONV010_AC3
 *   TC-REQ-FR-CONV-014-AC1-01 → _FRCONV014_AC1
 *   TC-REQ-FR-CONV-014-AC2-01 → _FRCONV014_AC2
 *   TC-REQ-FR-CONV-014-AC3-01 → _FRCONV014_AC3
 *
 * Mock 사용 없음 (§0.6).
 *
 * @req FR-CONV-010
 * @req FR-CONV-014
 */
class FallbackPolicyTest {

    private static final String FAILURE_SIGNATURE =
            "expected: 6 asymmetric method groups follow SRS policy matrix,"
                    + " got: at least one wrong branch";

    // -----------------------------------------------------------------
    // FR-CONV-010 AC-1 — HttpSession.getValue/putValue policy B
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-010
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV010_AC1() {
        AttributeTracingHttpSession javaxSession = new AttributeTracingHttpSession();
        adapter.jakarta.servlet61.http.HttpSession jakartaAdapter =
                newJakartaHttpSession(javaxSession);

        jakartaAdapter.getValue("user");
        if (!javaxSession.attributeReads.contains("user")) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-1: HttpSession.getValue did not route to"
                    + " original.getAttribute (policy B). attributeReads="
                    + javaxSession.attributeReads
                    + ", deprecatedValueReads=" + javaxSession.valueReads);
        }
        if (!javaxSession.valueReads.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-1: HttpSession.getValue still calls"
                    + " deprecated original.getValue. valueReads="
                    + javaxSession.valueReads);
        }

        jakartaAdapter.putValue("count", 42);
        if (!javaxSession.attributeWrites.containsKey("count")
                || !Integer.valueOf(42).equals(javaxSession.attributeWrites.get("count"))) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-1: HttpSession.putValue did not route to"
                    + " original.setAttribute (policy B). attributeWrites="
                    + javaxSession.attributeWrites
                    + ", deprecatedValueWrites=" + javaxSession.valueWrites);
        }
        if (!javaxSession.valueWrites.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-1: HttpSession.putValue still calls"
                    + " deprecated original.putValue. valueWrites="
                    + javaxSession.valueWrites);
        }
        Object back = jakartaAdapter.getValue("count");
        assertEquals(42, back, FAILURE_SIGNATURE
                + " — FR-CONV-010 AC-1: getValue return after putValue must match"
                + " attribute API value");
    }

    // -----------------------------------------------------------------
    // FR-CONV-010 AC-2 — setStatus(int, String) policy E1
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-010
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV010_AC2() {
        StatusTracingHttpServletResponse javaxResponse = new StatusTracingHttpServletResponse();
        adapter.jakarta.servlet61.http.HttpServletResponse jakartaAdapter =
                new adapter.jakarta.servlet61.http.HttpServletResponse(javaxResponse);

        jakartaAdapter.setStatus(404, "Not Found Custom");

        if (!javaxResponse.setStatusReasonCalls.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-2: setStatus(int, String) still delegates"
                    + " to deprecated original.setStatus(int, String). reasonCalls="
                    + javaxResponse.setStatusReasonCalls);
        }
        if (!javaxResponse.setStatusIntCalls.contains(404)) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-2: setStatus(int, String) did not call"
                    + " original.setStatus(int) (policy E1). intCalls="
                    + javaxResponse.setStatusIntCalls);
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-010 AC-3 — getSessionContext policy D
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-010
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV010_AC3() {
        SessionContextTracingHttpSession javaxSession = new SessionContextTracingHttpSession();
        adapter.jakarta.servlet61.http.HttpSession jakartaAdapter =
                newJakartaHttpSession(javaxSession);

        Object ctx = jakartaAdapter.getSessionContext();
        if (javaxSession.getSessionContextCallCount != 0) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-3: getSessionContext still delegates to"
                    + " original.getSessionContext (policy D requires empty dummy)."
                    + " callCount=" + javaxSession.getSessionContextCallCount);
        }
        assertNotNull(ctx, FAILURE_SIGNATURE
                + " — FR-CONV-010 AC-3: getSessionContext must return non-null"
                + " EmptyHttpSessionContext dummy");

        String fqn = "adapter.jakarta.servlet61.http.EmptyHttpSessionContext";
        Class<?> emptyCtxClass = lookupOrNull(fqn);
        if (emptyCtxClass == null) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-010 AC-3: " + fqn + " (policy D dummy)"
                    + " class is missing");
        }
        assertTrue(emptyCtxClass.isInstance(ctx), FAILURE_SIGNATURE
                + " — FR-CONV-010 AC-3: getSessionContext must return an"
                + " instance of " + fqn + ", got " + ctx.getClass().getName());
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-1 — servlet5 dormant + servlet61 신설 의무
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV014_AC1() {
        String servlet5Fqn =
                "adapter.jakarta.servlet5.http.EmptyHttpSessionContext";
        Class<?> servlet5Dormant = lookupOrNull(servlet5Fqn);
        if (servlet5Dormant != null) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-1: servlet5 module must remain dormant,"
                    + " but " + servlet5Fqn + " was found (policy D dummy"
                    + " should only exist in servlet6/servlet61)");
        }

        String servlet61Fqn =
                "adapter.jakarta.servlet61.http.EmptyHttpSessionContext";
        Class<?> servlet61Required = lookupOrNull(servlet61Fqn);
        if (servlet61Required == null) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-1: servlet61 module must own "
                    + servlet61Fqn + " (policy D dummy) — class is missing"
                    + " (RED expected at this phase)");
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-2 — servlet61 정책 B 진입 (module restatement)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV014_AC2() {
        AttributeTracingHttpSession javaxSession = new AttributeTracingHttpSession();
        adapter.jakarta.servlet61.http.HttpSession jakartaAdapter =
                newJakartaHttpSession(javaxSession);

        jakartaAdapter.putValue("session-key", "session-val");
        jakartaAdapter.getValue("session-key");

        if (!javaxSession.valueReads.isEmpty() || !javaxSession.valueWrites.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: servlet61 HttpSession.getValue/putValue"
                    + " still routes through deprecated original.getValue/putValue."
                    + " valueReads=" + javaxSession.valueReads
                    + ", valueWrites=" + javaxSession.valueWrites);
        }
        if (!javaxSession.attributeReads.contains("session-key")
                || !javaxSession.attributeWrites.containsKey("session-key")) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: servlet61 HttpSession.getValue/putValue"
                    + " must route to attribute API (policy B). attributeReads="
                    + javaxSession.attributeReads
                    + ", attributeWrites=" + javaxSession.attributeWrites);
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-3 — servlet61 sendRedirect(loc, sc, clearBuffer)
    // explicit override (SRS §6.2.2)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV014_AC3() throws IOException {
        SendRedirectTracingHttpServletResponse javaxResponse =
                new SendRedirectTracingHttpServletResponse();
        adapter.jakarta.servlet61.http.HttpServletResponse jakartaAdapter =
                new adapter.jakarta.servlet61.http.HttpServletResponse(javaxResponse);

        jakartaAdapter.sendRedirect("/login", 302, true);

        // The explicit override must NOT delegate to original.sendRedirect.
        if (!javaxResponse.sendRedirectCalls.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must NOT delegate to original sendRedirect(loc)."
                    + " sendRedirectCalls=" + javaxResponse.sendRedirectCalls);
        }

        if (!javaxResponse.setStatusIntCalls.contains(302)) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must call setStatus(sc). setStatusIntCalls="
                    + javaxResponse.setStatusIntCalls);
        }
        if (!"/login".equals(javaxResponse.locationHeaderValue)) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must set Location header to the given loc. observed="
                    + javaxResponse.locationHeaderValue);
        }
        if (javaxResponse.resetBufferCallCount == 0) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc,"
                    + " clearBuffer=true) must call resetBuffer().");
        }
        if (javaxResponse.flushBufferCallCount == 0) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must call flushBuffer().");
        }
    }

    // -----------------------------------------------------------------
    // Construction helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-010
     */
    private static adapter.jakarta.servlet61.http.HttpSession newJakartaHttpSession(
            HttpSession javaxSession) {
        try {
            java.lang.reflect.Constructor<adapter.jakarta.servlet61.http.HttpSession> ctor =
                    adapter.jakarta.servlet61.http.HttpSession.class.getDeclaredConstructor(
                            HttpSession.class);
            ctor.setAccessible(true);
            return ctor.newInstance(javaxSession);
        } catch (ReflectiveOperationException e) {
            fail(FAILURE_SIGNATURE
                    + " — unable to construct adapter HttpSession via reflection: "
                    + e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * @req FR-CONV-014
     */
    private static Class<?> lookupOrNull(String fqn) {
        try {
            return Class.forName(fqn, false, FallbackPolicyTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // -----------------------------------------------------------------
    // In-test fakes — javax.servlet 4.0 minimal implementations, no
    // library mocks (§0.6).
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-010
     */
    @SuppressWarnings("deprecation")
    private static class FakeHttpSession implements HttpSession {
        @Override public long getCreationTime() { return 0L; }
        @Override public String getId() { return "fake"; }
        @Override public long getLastAccessedTime() { return 0L; }
        @Override public ServletContext getServletContext() { return null; }
        @Override public void setMaxInactiveInterval(int interval) {}
        @Override public int getMaxInactiveInterval() { return 0; }
        @Override public HttpSessionContext getSessionContext() { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public Object getValue(String name) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
        @Override public String[] getValueNames() { return new String[0]; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void putValue(String name, Object value) {}
        @Override public void removeAttribute(String name) {}
        @Override public void removeValue(String name) {}
        @Override public void invalidate() {}
        @Override public boolean isNew() { return false; }
    }

    /**
     * @req FR-CONV-010
     */
    @SuppressWarnings("deprecation")
    private static final class AttributeTracingHttpSession extends FakeHttpSession {
        final Map<String, Object> attributeStore = new HashMap<>();
        final List<String> attributeReads = new ArrayList<>();
        final Map<String, Object> attributeWrites = new HashMap<>();
        final List<String> valueReads = new ArrayList<>();
        final Map<String, Object> valueWrites = new HashMap<>();

        @Override
        public Object getAttribute(String name) {
            attributeReads.add(name);
            return attributeStore.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributeWrites.put(name, value);
            attributeStore.put(name, value);
        }

        @Override
        public Object getValue(String name) {
            valueReads.add(name);
            return attributeStore.get(name);
        }

        @Override
        public void putValue(String name, Object value) {
            valueWrites.put(name, value);
            attributeStore.put(name, value);
        }
    }

    /**
     * @req FR-CONV-010
     */
    @SuppressWarnings("deprecation")
    private static final class SessionContextTracingHttpSession extends FakeHttpSession {
        int getSessionContextCallCount = 0;

        @Override
        public HttpSessionContext getSessionContext() {
            getSessionContextCallCount++;
            return null;
        }
    }

    /**
     * @req FR-CONV-010
     */
    @SuppressWarnings("deprecation")
    private static class FakeHttpServletResponse implements HttpServletResponse {
        @Override public void addCookie(Cookie cookie) {}
        @Override public boolean containsHeader(String name) { return false; }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public String encodeUrl(String url) { return url; }
        @Override public String encodeRedirectUrl(String url) { return url; }
        @Override public void sendError(int sc, String msg) throws IOException {}
        @Override public void sendError(int sc) throws IOException {}
        @Override public void sendRedirect(String location) throws IOException {}
        @Override public void setDateHeader(String name, long date) {}
        @Override public void addDateHeader(String name, long date) {}
        @Override public void setHeader(String name, String value) {}
        @Override public void addHeader(String name, String value) {}
        @Override public void setIntHeader(String name, int value) {}
        @Override public void addIntHeader(String name, int value) {}
        @Override public void setStatus(int sc) {}
        @Override public void setStatus(int sc, String sm) {}
        @Override public int getStatus() { return 0; }
        @Override public String getHeader(String name) { return null; }
        @Override public Collection<String> getHeaders(String name) { return Collections.emptyList(); }
        @Override public Collection<String> getHeaderNames() { return Collections.emptyList(); }
        @Override public String getCharacterEncoding() { return null; }
        @Override public String getContentType() { return null; }
        @Override public javax.servlet.ServletOutputStream getOutputStream() throws IOException {
            return new javax.servlet.ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(javax.servlet.WriteListener writeListener) {}
                @Override public void write(int b) throws IOException {}
            };
        }
        @Override public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new OutputStream() { @Override public void write(int b) {} });
        }
        @Override public void setCharacterEncoding(String charset) {}
        @Override public void setContentLength(int len) {}
        @Override public void setContentLengthLong(long len) {}
        @Override public void setContentType(String type) {}
        @Override public void setBufferSize(int size) {}
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() throws IOException {}
        @Override public void resetBuffer() {}
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() {}
        @Override public void setLocale(Locale loc) {}
        @Override public Locale getLocale() { return Locale.ROOT; }
    }

    /**
     * @req FR-CONV-010
     */
    @SuppressWarnings("deprecation")
    private static final class StatusTracingHttpServletResponse extends FakeHttpServletResponse {
        final List<Integer> setStatusIntCalls = new ArrayList<>();
        final List<int[]> setStatusReasonCalls = new ArrayList<>();

        @Override
        public void setStatus(int sc) {
            setStatusIntCalls.add(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            setStatusReasonCalls.add(new int[]{sc});
        }
    }

    /**
     * Trace HttpServletResponse for FR-CONV-014 AC-3 (servlet 6.1
     * sendRedirect explicit override).
     *
     * @req FR-CONV-014
     */
    @SuppressWarnings("deprecation")
    private static final class SendRedirectTracingHttpServletResponse
            extends FakeHttpServletResponse {
        final List<String> sendRedirectCalls = new ArrayList<>();
        final List<Integer> setStatusIntCalls = new ArrayList<>();
        String locationHeaderValue = null;
        int resetBufferCallCount = 0;
        int flushBufferCallCount = 0;

        @Override
        public void sendRedirect(String location) throws IOException {
            sendRedirectCalls.add(location);
        }

        @Override
        public void setStatus(int sc) {
            setStatusIntCalls.add(sc);
        }

        @Override
        public void setHeader(String name, String value) {
            if ("Location".equalsIgnoreCase(name)) {
                locationHeaderValue = value;
            }
        }

        @Override
        public void resetBuffer() {
            resetBufferCallCount++;
        }

        @Override
        public void flushBuffer() throws IOException {
            flushBufferCallCount++;
        }
    }
}
