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
 * FR-CONV-014 (모듈별 적용 범위 — SPEC-14) for the servlet6 module.
 *
 * Scope (T-PH004-13 — RED):
 *   FR-CONV-010 AC-1: HttpSession.getValue/putValue jakarta→javax 어댑터가
 *                     policy B (attribute API 자동 폴백) 를 실행한다.
 *                     즉 jakarta 어댑터에서 getValue("k") 호출 시 원본의
 *                     getAttribute("k") 가 호출되고, putValue("k", v)
 *                     호출 시 원본의 setAttribute("k", v) 가 호출되어야
 *                     한다 (deprecated getValue/putValue 직접 위임 금지).
 *   FR-CONV-010 AC-2: setStatus(int, String) 호출 시 reason phrase 가
 *                     폐기되고 원본의 setStatus(int) 만 호출된다 (policy E1).
 *                     deprecated setStatus(int, String) 직접 위임 금지.
 *   FR-CONV-010 AC-3: getSessionContext() 가 EmptyHttpSessionContext 더미
 *                     를 반환한다 (policy D — 원본 위임 금지).
 *   FR-CONV-014 AC-1: servlet5 dormant. servlet5 측에는 EmptyHttpSessionContext
 *                     가 존재하지 않으며, servlet6 측에는 신설 의무가 있다.
 *   FR-CONV-014 AC-2: servlet6 모듈에서 HttpSession.getValue/putValue 호출이
 *                     attribute API 로 라우팅 (policy B 진입).
 *   FR-CONV-014 AC-3: ServletContext.log(Exception, String) 가 policy E
 *                     (log(String, Throwable) 로 인자 재배치) 로 동작한다.
 *                     (servlet61 sendRedirect 명시 override 는 sibling
 *                     test 가 담당.)
 *
 * Red phase 의도:
 *   - 현재 시점 (T-PH004-12 까지 완료) HttpSession.getValue/putValue 는
 *     원본의 deprecated getValue/putValue 를 그대로 호출하고 있으며,
 *     setStatus(int, String) 도 원본 setStatus(int, String) 을 직접
 *     호출한다. getSessionContext 도 원본 위임으로 EmptyHttpSessionContext
 *     dummy 가 아니다. EmptyHttpSessionContext 클래스 자체가 부재.
 *     log(Exception, String) 은 원본의 deprecated log(Exception, String)
 *     로 그대로 위임.
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
 * Mock 사용 없음 (§0.6) — 라이브러리 mock 대신 본 파일 내부의 minimal
 * fake (javax.servlet 4.0 인터페이스 직접 구현) 로 어댑터 위임 경로를
 * 관찰한다.
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
        adapter.jakarta.servlet6.http.HttpSession jakartaAdapter =
                newJakartaHttpSession(javaxSession);

        // policy B — getValue 어댑터 호출은 원본의 getAttribute 로 라우팅돼야 한다.
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

        // policy B — putValue 어댑터 호출은 원본의 setAttribute 로 라우팅돼야 한다.
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
        // getValue 의 반환값이 attribute 영역과 일치해야 한다 (set 후 read 일치성).
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
        adapter.jakarta.servlet6.http.HttpServletResponse jakartaAdapter =
                new adapter.jakarta.servlet6.http.HttpServletResponse(javaxResponse);

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
        adapter.jakarta.servlet6.http.HttpSession jakartaAdapter =
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

        // EmptyHttpSessionContext 신설 클래스 확인.
        String fqn = "adapter.jakarta.servlet6.http.EmptyHttpSessionContext";
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
    // FR-CONV-014 AC-1 — servlet5 dormant + servlet6 신설 의무
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

        String servlet6Fqn =
                "adapter.jakarta.servlet6.http.EmptyHttpSessionContext";
        Class<?> servlet6Required = lookupOrNull(servlet6Fqn);
        if (servlet6Required == null) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-1: servlet6 module must own "
                    + servlet6Fqn + " (policy D dummy) — class is missing"
                    + " (RED expected at this phase)");
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-2 — servlet6 정책 B 진입 (module restatement)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV014_AC2() {
        AttributeTracingHttpSession javaxSession = new AttributeTracingHttpSession();
        adapter.jakarta.servlet6.http.HttpSession jakartaAdapter =
                newJakartaHttpSession(javaxSession);

        jakartaAdapter.putValue("session-key", "session-val");
        jakartaAdapter.getValue("session-key");

        if (!javaxSession.valueReads.isEmpty() || !javaxSession.valueWrites.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: servlet6 HttpSession.getValue/putValue"
                    + " still routes through deprecated original.getValue/putValue."
                    + " valueReads=" + javaxSession.valueReads
                    + ", valueWrites=" + javaxSession.valueWrites);
        }
        if (!javaxSession.attributeReads.contains("session-key")
                || !javaxSession.attributeWrites.containsKey("session-key")) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: servlet6 HttpSession.getValue/putValue"
                    + " must route to attribute API (policy B). attributeReads="
                    + javaxSession.attributeReads
                    + ", attributeWrites=" + javaxSession.attributeWrites);
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-3 — ServletContext.log(Exception, String) policy E
    // (sibling test in servlet61 covers sendRedirect)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    void shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant_FRCONV014_AC3() {
        LogTracingServletContext javaxCtx = new LogTracingServletContext();
        adapter.jakarta.servlet6.ServletContext jakartaAdapter =
                new adapter.jakarta.servlet6.ServletContext(javaxCtx);

        RuntimeException error = new RuntimeException("boom");
        jakartaAdapter.log(error, "context-event");

        if (!javaxCtx.exceptionLogCalls.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: ServletContext.log(Exception, String)"
                    + " still delegates to deprecated original.log(Exception, String)."
                    + " exceptionLogCalls=" + javaxCtx.exceptionLogCalls);
        }
        if (javaxCtx.throwableLogCalls.size() != 1
                || !"context-event".equals(javaxCtx.throwableLogCalls.get(0).message)
                || javaxCtx.throwableLogCalls.get(0).cause != error) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: ServletContext.log(Exception, String)"
                    + " must rewrite to original.log(String, Throwable) (policy E)."
                    + " throwableLogCalls=" + javaxCtx.throwableLogCalls);
        }
    }

    // -----------------------------------------------------------------
    // Construction helpers — adapter HttpSession constructor is
    // package-private; use reflection.
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-010
     */
    private static adapter.jakarta.servlet6.http.HttpSession newJakartaHttpSession(
            HttpSession javaxSession) {
        try {
            java.lang.reflect.Constructor<adapter.jakarta.servlet6.http.HttpSession> ctor =
                    adapter.jakarta.servlet6.http.HttpSession.class.getDeclaredConstructor(
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
    // In-test fakes — minimal javax.servlet 4.0 interface implementations,
    // no library mocks (§0.6).
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
     * @req FR-CONV-014
     */
    @SuppressWarnings("deprecation")
    private static class FakeServletContext implements ServletContext {
        @Override public String getContextPath() { return ""; }
        @Override public ServletContext getContext(String uripath) { return null; }
        @Override public int getMajorVersion() { return 4; }
        @Override public int getMinorVersion() { return 0; }
        @Override public int getEffectiveMajorVersion() { return 4; }
        @Override public int getEffectiveMinorVersion() { return 0; }
        @Override public String getMimeType(String file) { return null; }
        @Override public Set<String> getResourcePaths(String path) { return Collections.emptySet(); }
        @Override public URL getResource(String path) throws MalformedURLException { return null; }
        @Override public InputStream getResourceAsStream(String path) { return null; }
        @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public RequestDispatcher getNamedDispatcher(String name) { return null; }
        @Override public Servlet getServlet(String name) { return null; }
        @Override public Enumeration<Servlet> getServlets() { return Collections.emptyEnumeration(); }
        @Override public Enumeration<String> getServletNames() { return Collections.emptyEnumeration(); }
        @Override public void log(String msg) {}
        @Override public void log(Exception exception, String msg) {}
        @Override public void log(String message, Throwable throwable) {}
        @Override public String getRealPath(String path) { return null; }
        @Override public String getServerInfo() { return "fake"; }
        @Override public String getInitParameter(String name) { return null; }
        @Override public Enumeration<String> getInitParameterNames() { return Collections.emptyEnumeration(); }
        @Override public boolean setInitParameter(String name, String value) { return false; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
        @Override public void setAttribute(String name, Object object) {}
        @Override public void removeAttribute(String name) {}
        @Override public String getServletContextName() { return "fake"; }
        @Override public ServletRegistration.Dynamic addServlet(String servletName, String className) { return null; }
        @Override public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) { return null; }
        @Override public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) { return null; }
        @Override public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) { return null; }
        @Override public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException { return null; }
        @Override public ServletRegistration getServletRegistration(String servletName) { return null; }
        @Override public Map<String, ? extends ServletRegistration> getServletRegistrations() { return Collections.emptyMap(); }
        @Override public FilterRegistration.Dynamic addFilter(String filterName, String className) { return null; }
        @Override public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) { return null; }
        @Override public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) { return null; }
        @Override public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException { return null; }
        @Override public FilterRegistration getFilterRegistration(String filterName) { return null; }
        @Override public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return Collections.emptyMap(); }
        @Override public SessionCookieConfig getSessionCookieConfig() { return null; }
        @Override public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {}
        @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() { return EnumSet.noneOf(SessionTrackingMode.class); }
        @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() { return EnumSet.noneOf(SessionTrackingMode.class); }
        @Override public void addListener(String className) {}
        @Override public <T extends EventListener> void addListener(T t) {}
        @Override public void addListener(Class<? extends EventListener> listenerClass) {}
        @Override public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException { return null; }
        @Override public JspConfigDescriptor getJspConfigDescriptor() { return null; }
        @Override public ClassLoader getClassLoader() { return getClass().getClassLoader(); }
        @Override public void declareRoles(String... roleNames) {}
        @Override public String getVirtualServerName() { return "fake"; }
        @Override public int getSessionTimeout() { return 0; }
        @Override public void setSessionTimeout(int sessionTimeout) {}
        @Override public String getRequestCharacterEncoding() { return null; }
        @Override public void setRequestCharacterEncoding(String encoding) {}
        @Override public String getResponseCharacterEncoding() { return null; }
        @Override public void setResponseCharacterEncoding(String encoding) {}
    }

    /**
     * @req FR-CONV-014
     */
    @SuppressWarnings("deprecation")
    private static final class LogTracingServletContext extends FakeServletContext {
        static final class LogEntry {
            final String message;
            final Throwable cause;

            LogEntry(String message, Throwable cause) {
                this.message = message;
                this.cause = cause;
            }

            @Override
            public String toString() {
                return "LogEntry{msg=" + message + ", cause=" + cause + "}";
            }
        }

        final List<LogEntry> exceptionLogCalls = new ArrayList<>();
        final List<LogEntry> throwableLogCalls = new ArrayList<>();

        @Override
        public void log(Exception exception, String msg) {
            exceptionLogCalls.add(new LogEntry(msg, exception));
        }

        @Override
        public void log(String message, Throwable throwable) {
            throwableLogCalls.add(new LogEntry(message, throwable));
        }
    }
}
