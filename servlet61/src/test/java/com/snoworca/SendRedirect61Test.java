package com.snoworca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TDD red-phase test for FR-CONV-001 (인터페이스 완전 구현 — SPEC-1) +
 * FR-CONV-014 (모듈별 적용 범위 — SPEC-14) for the servlet61 module —
 * jakarta 6.1 신규 추상 메서드 {@code sendRedirect(String, int, boolean)}
 * 의 명시 override 검증 (SRS §6.2.2 policy E table).
 *
 * Scope (T-PH004-19 — RED, 5 test cases):
 *   FR-CONV-001 AC-1: jakarta 6.1 sendRedirect(String, int, boolean)
 *                     override 가 adapter.jakarta.servlet61.http.HttpServletResponse
 *                     에 존재한다 (reflection + 추상 비-여부).
 *   FR-CONV-001 AC-2: jakarta 6.1 sendRedirect(String, int, boolean)
 *                     호출 시 AbstractMethodError 가 발생하지 않는다
 *                     (override 존재 → AbstractMethodError 없이 정상 실행).
 *   FR-CONV-014 AC-1: servlet5 모듈에서는 jakarta 6.1 의
 *                     sendRedirect(String, int, boolean) override 가
 *                     의무 적용 대상이 아니다 — servlet5 측 동일 시그니처
 *                     클래스는 부재(dormant) 함을 검증.
 *   FR-CONV-014 AC-2: servlet61 모듈의 HttpServletResponse 가 6.0 base
 *                     의 sendRedirect(String) 도 정상 위임함을 함께 검증
 *                     (6+ 폴백 정책 B 분기 — 6.1 신규 override 가
 *                     기존 1-arg 메서드를 침해하지 않음).
 *   FR-CONV-014 AC-3: servlet61 모듈에서 sendRedirect(loc, sc, clearBuffer)
 *                     의 명시 override 가 적용된다 — setStatus(sc) +
 *                     setHeader("Location", loc) + clearBuffer=true 시
 *                     resetBuffer + flushBuffer 호출, 원본
 *                     sendRedirect(loc) 위임 금지.
 *
 * Sidecar test_case ↔ test_symbol mapping (1:1):
 *   TC-REQ-FR-CONV-001-AC1-03 → _FRCONV001_AC1
 *   TC-REQ-FR-CONV-001-AC2-03 → _FRCONV001_AC2
 *   TC-REQ-FR-CONV-014-AC1-03 → _FRCONV014_AC1
 *   TC-REQ-FR-CONV-014-AC2-03 → _FRCONV014_AC2
 *   TC-REQ-FR-CONV-014-AC3-03 → _FRCONV014_AC3
 *
 * expected_failure_signature (5 case 공통):
 *   "expected: 6.1 sendRedirect override implemented, got: AbstractMethodError"
 *
 * Red phase 의도:
 *   - 본 test 는 jakarta 6.1 sendRedirect(String, int, boolean) override 가
 *     servlet61 의 HttpServletResponse 에 명시 존재하지 않거나,
 *     placeholder 가 원본의 sendRedirect(loc) 로만 위임하면 fail 한다.
 *   - 5 cases 가 동시에 fail 하면 RED. 1 case 라도 pass 면 부분 RED.
 *   - Pair sibling FallbackPolicyTest (T-PH004-13) 와 동일 AC 일부를
 *     중첩 검증하지만, 본 파일은 sendRedirect 단일 그룹에 집중된
 *     dedicated coverage 이며, sidecar test_cases[].test_symbol
 *     접두사 "shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61"
 *     로 식별된다.
 *
 * Mock 사용 없음 (§0.6). 모든 fake 는 javax.servlet 4.0 표준 API 의
 * in-test 직접 구현체이다.
 *
 * @req FR-CONV-001
 * @req FR-CONV-014
 */
class SendRedirect61Test {

    private static final String FAILURE_SIGNATURE =
            "expected: 6.1 sendRedirect override implemented, got: AbstractMethodError";

    // -----------------------------------------------------------------
    // FR-CONV-001 AC-1 — override 시그니처 존재 (reflection)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-001
     */
    @Test
    @DisplayName("FR-CONV-001 AC-1: jakarta 6.1 sendRedirect(String,int,boolean) override 가 servlet61 HttpServletResponse 에 존재한다")
    void shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61_FRCONV001_AC1() {
        Class<?> adapter = adapter.jakarta.servlet61.http.HttpServletResponse.class;

        Method override;
        try {
            override = adapter.getDeclaredMethod(
                    "sendRedirect", String.class, int.class, boolean.class);
        } catch (NoSuchMethodException e) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-001 AC-1: adapter.jakarta.servlet61.http.HttpServletResponse"
                    + " is missing declared method sendRedirect(String, int, boolean)."
                    + " Found instead: " + listDeclaredSendRedirectSignatures(adapter));
            return;
        }

        if (Modifier.isAbstract(override.getModifiers())) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-001 AC-1: declared sendRedirect(String, int, boolean)"
                    + " is abstract — must be concrete override.");
        }
        if (override.getReturnType() != void.class) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-001 AC-1: sendRedirect(String, int, boolean)"
                    + " return type must be void, got: " + override.getReturnType().getName());
        }
        if (!throwsIOException(override)) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-001 AC-1: sendRedirect(String, int, boolean)"
                    + " must declare throws IOException per jakarta 6.1 interface,"
                    + " got exceptions: " + exceptionListAsString(override));
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-001 AC-2 — invocation 시 AbstractMethodError 미발생
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-001
     */
    @Test
    @DisplayName("FR-CONV-001 AC-2: sendRedirect(String,int,boolean) 호출 시 AbstractMethodError 미발생 (servlet61)")
    void shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61_FRCONV001_AC2() {
        SendRedirectTracingHttpServletResponse javaxResponse =
                new SendRedirectTracingHttpServletResponse();
        adapter.jakarta.servlet61.http.HttpServletResponse jakartaAdapter =
                new adapter.jakarta.servlet61.http.HttpServletResponse(javaxResponse);

        try {
            jakartaAdapter.sendRedirect("/login", 302, false);
        } catch (AbstractMethodError e) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-001 AC-2: sendRedirect(String, int, boolean) invocation"
                    + " threw AbstractMethodError — override missing or stub."
                    + " AbstractMethodError.message=" + e.getMessage());
        } catch (IOException e) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-001 AC-2: unexpected IOException during normal"
                    + " sendRedirect(loc, sc, false) — fake response does not throw."
                    + " ioException=" + e);
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-1 — servlet5 dormant (override 의무 없음)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    @DisplayName("FR-CONV-014 AC-1: servlet5 모듈은 jakarta 6.1 sendRedirect(String,int,boolean) override 의무 대상 아님 (dormant)")
    void shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61_FRCONV014_AC1() {
        String servlet5Fqn = "adapter.jakarta.servlet5.http.HttpServletResponse";
        Class<?> servlet5Type;
        try {
            servlet5Type = Class.forName(
                    servlet5Fqn, false, SendRedirect61Test.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // servlet5 모듈은 본 테스트 모듈의 classpath 에 부재할 수
            // 있다 (sibling module). 이 경우 dormancy 가 strong form
            // (클래스 자체 부재) 으로 만족 — pass.
            return;
        }

        try {
            Method m = servlet5Type.getDeclaredMethod(
                    "sendRedirect", String.class, int.class, boolean.class);
            if (m != null && !Modifier.isAbstract(m.getModifiers())) {
                fail(FAILURE_SIGNATURE
                        + " — FR-CONV-014 AC-1: servlet5 모듈은 jakarta 6.1 override"
                        + " 적용 대상이 아니다 — 그러나 " + servlet5Fqn
                        + " 가 concrete sendRedirect(String, int, boolean) 을"
                        + " 선언함. servlet5 dormancy 위배.");
            }
        } catch (NoSuchMethodException expected) {
            // dormant 가 만족됨 — servlet5 측에 본 시그니처 부재.
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-2 — servlet61 모듈의 6.0 base sendRedirect(String)
    // 위임 (policy B 진입 — 6+ baseline 동작 보장)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    @DisplayName("FR-CONV-014 AC-2: servlet61 HttpServletResponse 의 sendRedirect(String) 는 원본에 정상 위임 (6+ baseline)")
    void shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61_FRCONV014_AC2() {
        SendRedirectTracingHttpServletResponse javaxResponse =
                new SendRedirectTracingHttpServletResponse();
        adapter.jakarta.servlet61.http.HttpServletResponse jakartaAdapter =
                new adapter.jakarta.servlet61.http.HttpServletResponse(javaxResponse);

        try {
            jakartaAdapter.sendRedirect("/legacy-path");
        } catch (AbstractMethodError e) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: sendRedirect(String) baseline invocation"
                    + " threw AbstractMethodError — 6+ baseline 동작 미보장."
                    + " AbstractMethodError.message=" + e.getMessage());
        } catch (IOException e) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: unexpected IOException during"
                    + " sendRedirect(String) — fake response does not throw."
                    + " ioException=" + e);
        }

        if (javaxResponse.sendRedirectCalls.size() != 1
                || !"/legacy-path".equals(javaxResponse.sendRedirectCalls.get(0))) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: sendRedirect(String) must delegate to"
                    + " original sendRedirect(String) exactly once with the"
                    + " same location. sendRedirectCalls="
                    + javaxResponse.sendRedirectCalls);
        }
        // 1-arg 위임은 status/location 헤더 합성 분기를 진입하지 않는다.
        if (!javaxResponse.setStatusIntCalls.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: sendRedirect(String) baseline must NOT"
                    + " synthesize setStatus(int). observed setStatusIntCalls="
                    + javaxResponse.setStatusIntCalls);
        }
        if (javaxResponse.locationHeaderValue != null) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-2: sendRedirect(String) baseline must NOT"
                    + " synthesize Location header. observed locationHeaderValue="
                    + javaxResponse.locationHeaderValue);
        }
    }

    // -----------------------------------------------------------------
    // FR-CONV-014 AC-3 — servlet61 sendRedirect(loc, sc, clearBuffer)
    // 명시 override (setStatus + Location header + resetBuffer +
    // flushBuffer, 원본 sendRedirect(loc) 위임 금지)
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
     */
    @Test
    @DisplayName("FR-CONV-014 AC-3: servlet61 sendRedirect(loc, sc, clearBuffer) 명시 override 가 setStatus + Location + resetBuffer + flushBuffer 를 호출, 원본 sendRedirect(loc) 위임 금지")
    void shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61_FRCONV014_AC3() throws IOException {
        SendRedirectTracingHttpServletResponse javaxResponse =
                new SendRedirectTracingHttpServletResponse();
        adapter.jakarta.servlet61.http.HttpServletResponse jakartaAdapter =
                new adapter.jakarta.servlet61.http.HttpServletResponse(javaxResponse);

        jakartaAdapter.sendRedirect("/dashboard", 303, true);

        // 명시 override 는 원본의 sendRedirect(loc) 로 위임하지 않는다.
        if (!javaxResponse.sendRedirectCalls.isEmpty()) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must NOT delegate to original sendRedirect(loc)."
                    + " sendRedirectCalls=" + javaxResponse.sendRedirectCalls);
        }
        if (!javaxResponse.setStatusIntCalls.contains(303)) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must call setStatus(sc=303). setStatusIntCalls="
                    + javaxResponse.setStatusIntCalls);
        }
        assertEquals("/dashboard", javaxResponse.locationHeaderValue,
                FAILURE_SIGNATURE
                        + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                        + " must set Location header to the given loc.");
        if (javaxResponse.resetBufferCallCount == 0) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer=true)"
                    + " must call resetBuffer().");
        }
        if (javaxResponse.flushBufferCallCount == 0) {
            fail(FAILURE_SIGNATURE
                    + " — FR-CONV-014 AC-3: sendRedirect(loc, sc, clearBuffer)"
                    + " must call flushBuffer().");
        }
    }

    // -----------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-001
     */
    private static boolean throwsIOException(Method m) {
        for (Class<?> ex : m.getExceptionTypes()) {
            if (IOException.class.isAssignableFrom(ex)) return true;
        }
        return false;
    }

    /**
     * @req FR-CONV-001
     */
    private static String exceptionListAsString(Method m) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Class<?> ex : m.getExceptionTypes()) {
            if (!first) sb.append(", ");
            sb.append(ex.getName());
            first = false;
        }
        return sb.append(']').toString();
    }

    /**
     * @req FR-CONV-001
     */
    private static String listDeclaredSendRedirectSignatures(Class<?> type) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Method m : type.getDeclaredMethods()) {
            if (!"sendRedirect".equals(m.getName())) continue;
            if (!first) sb.append(", ");
            sb.append(m.getName()).append('(');
            Class<?>[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(params[i].getSimpleName());
            }
            sb.append(')');
            first = false;
        }
        return sb.append(']').toString();
    }

    // -----------------------------------------------------------------
    // In-test fakes — javax.servlet 4.0 minimal implementations, no
    // library mocks (§0.6). Mirrors FallbackPolicyTest sibling
    // (T-PH004-13) but scoped to sendRedirect.
    // -----------------------------------------------------------------

    /**
     * @req FR-CONV-014
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
        @Override public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener writeListener) {}
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
     * Trace HttpServletResponse for FR-CONV-014 AC-3 (servlet 6.1
     * sendRedirect explicit override) — and AC-1/AC-2 invocation paths.
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
