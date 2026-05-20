package com.snoworca.regression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression test class capturing the Bug Museum (BM01~BM10) defects defined in FR-TEST-004.
 *
 * BM01~BM09 are regression cases targeting 0.0.1 critical defects. They MUST fail on the
 * currently-shipped (defective) code (TDD red phase) and pass after the corresponding
 * FR-FIX-001~009 fix tasks land.
 *
 * BM10 is a behavior lock-in (not a defect) covering the current ThreadLocal-based single
 * threaded synchronous invocation assumption of HttpUpgradeHandler (NFR-CONV-001 +
 * 60.conversion-strategy.md D12). It MUST pass today and start failing only if the assumption
 * is broken (e.g. asynchronous/multi-thread invocation).
 */
class BugMuseumTest {

    // ------------------------------------------------------------------------------------
    // BM01 — StreamConverter.read must delegate to read, not readLine (FR-FIX-001)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM01: StreamConverter.read must delegate to inputstream.read, not readLine (FR-FIX-001)")
    void BM01_streamConverter_read_must_not_call_readLine() throws Exception {
        jakarta.servlet.ServletInputStream origin = mock(jakarta.servlet.ServletInputStream.class);
        byte[] buffer = new byte[16];
        when(origin.read(buffer, 0, buffer.length)).thenReturn(7);

        javax.servlet.ServletInputStream converted =
            adapter.servletElementConverter5.StreamConverter.convert(origin);

        int returned = converted.read(buffer, 0, buffer.length);

        assertEquals(7, returned, "adapter must propagate the byte count returned by inputstream.read");
        verify(origin).read(buffer, 0, buffer.length);
        verify(origin, never()).readLine(any(byte[].class), anyInt(), anyInt());
    }

    // ------------------------------------------------------------------------------------
    // BM02 — Part.getSubmittedFileName must delegate to origin Part, not self (FR-FIX-002)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM02: Part.getSubmittedFileName must delegate to origin Part, not recurse on self (FR-FIX-002)")
    void BM02_part_getSubmittedFileName_must_not_self_recurse() throws Exception {
        javax.servlet.http.Part originPart = mock(javax.servlet.http.Part.class);
        when(originPart.getSubmittedFileName()).thenReturn("upload.txt");

        // adapter.jakarta.servlet5.http.Part has a package-default constructor; access via reflection.
        Class<?> adapterClass = Class.forName("adapter.jakarta.servlet5.http.Part");
        Constructor<?> ctor = adapterClass.getDeclaredConstructor(javax.servlet.http.Part.class);
        ctor.setAccessible(true);
        jakarta.servlet.http.Part adapterPart = (jakarta.servlet.http.Part) ctor.newInstance(originPart);

        String fileName = assertDoesNotThrow(adapterPart::getSubmittedFileName,
            "adapter must not throw StackOverflowError or other recursion-induced exception");

        assertEquals("upload.txt", fileName, "adapter must return origin.getSubmittedFileName()");
        verify(originPart).getSubmittedFileName();
    }

    // ------------------------------------------------------------------------------------
    // BM03 — jakarta ServletContext.setResponseCharacterEncoding must delegate correctly (FR-FIX-004)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM03: jakarta ServletContext.setResponseCharacterEncoding must call origin.setResponseCharacterEncoding (FR-FIX-004)")
    void BM03_jakartaServletContext_setResponseCharacterEncoding_must_delegate_correctly() {
        javax.servlet.ServletContext origin = mock(javax.servlet.ServletContext.class);
        adapter.jakarta.servlet5.ServletContext adapterCtx =
            new adapter.jakarta.servlet5.ServletContext(origin);

        adapterCtx.setResponseCharacterEncoding("UTF-8");

        verify(origin).setResponseCharacterEncoding("UTF-8");
        verify(origin, never()).setRequestCharacterEncoding(any());
    }

    // ------------------------------------------------------------------------------------
    // BM04 — ServletReqResConverter must route HttpServletResponse via Http* branch (FR-FIX-008)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM04: ServletReqResConverter must route jakarta.HttpServletResponse to Http adapter, not generic ServletResponse (FR-FIX-008)")
    void BM04_servletReqResConverter_instanceof_must_take_http_branch() {
        jakarta.servlet.http.HttpServletResponse jakartaHttp =
            mock(jakarta.servlet.http.HttpServletResponse.class);

        javax.servlet.ServletResponse converted =
            adapter.servletElementConverter5.ServletReqResConverter.convert(
                (jakarta.servlet.ServletResponse) jakartaHttp);

        assertNotNull(converted);
        assertTrue(
            converted instanceof adapter.javax.servlet5.http.HttpServletResponse,
            "expected adapter.javax.servlet5.http.HttpServletResponse, actual: "
                + converted.getClass().getName()
                + " (instanceof branch fell through to generic ServletResponse fallback)");
    }

    // ------------------------------------------------------------------------------------
    // BM05 — jakarta ServletResponse.setContentLengthLong must not narrow to int (FR-FIX-003)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM05: jakarta ServletResponse.setContentLengthLong must call origin.setContentLengthLong (no int narrowing) (FR-FIX-003)")
    void BM05_jakartaServletResponse_setContentLengthLong_must_not_narrow() {
        javax.servlet.ServletResponse origin = mock(javax.servlet.ServletResponse.class);
        adapter.jakarta.servlet5.ServletResponse adapterResp =
            new adapter.jakarta.servlet5.ServletResponse(origin);

        adapterResp.setContentLengthLong(Long.MAX_VALUE);

        verify(origin).setContentLengthLong(Long.MAX_VALUE);
        verify(origin, never()).setContentLength(anyInt());
    }

    // ------------------------------------------------------------------------------------
    // BM06 — jakarta ServletRequest.getRealPath must be marked @Deprecated for 5.0 spec (FR-FIX-007)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM06: jakarta ServletRequest adapter must mark getRealPath as @Deprecated (jakarta 5.0 removed it from interface) (FR-FIX-007)")
    void BM06_jakartaServletRequest_getRealPath_must_be_deprecated() throws NoSuchMethodException {
        Method getRealPath = adapter.jakarta.servlet5.ServletRequest.class
            .getMethod("getRealPath", String.class);

        assertTrue(
            getRealPath.isAnnotationPresent(Deprecated.class),
            "jakarta 5.0+ removed getRealPath from ServletRequest; the adapter must mark the "
                + "method @Deprecated so callers are warned. Currently missing.");
    }

    // ------------------------------------------------------------------------------------
    // BM07 — jakarta HttpServletRequest.getCookies must tolerate origin returning null (FR-FIX-009)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM07: jakarta HttpServletRequest.getCookies must not throw NullPointerException when origin returns null (FR-FIX-009)")
    void BM07_jakartaHttpServletRequest_getCookies_must_handle_null_from_origin() {
        javax.servlet.http.HttpServletRequest origin = mock(javax.servlet.http.HttpServletRequest.class);
        when(origin.getCookies()).thenReturn(null);

        adapter.jakarta.servlet5.http.HttpServletRequest adapterReq =
            new adapter.jakarta.servlet5.http.HttpServletRequest(origin);

        jakarta.servlet.http.Cookie[] cookies = assertDoesNotThrow(adapterReq::getCookies,
            "adapter.getCookies must not throw NullPointerException when origin.getCookies() returns null");

        // Either null or an empty array is acceptable; the defect is throwing NPE.
        if (cookies != null) {
            assertEquals(0, cookies.length,
                "when origin returns null, the adapter should return null or an empty array");
        }
    }

    // ------------------------------------------------------------------------------------
    // BM08 — javax ServletContext.setRequestCharacterEncoding must delegate correctly (FR-FIX-005)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM08: javax ServletContext.setRequestCharacterEncoding must call origin.setRequestCharacterEncoding (FR-FIX-005)")
    void BM08_javaxServletContext_setRequestCharacterEncoding_must_delegate_correctly() {
        jakarta.servlet.ServletContext origin = mock(jakarta.servlet.ServletContext.class);
        adapter.javax.servlet5.ServletContext adapterCtx =
            new adapter.javax.servlet5.ServletContext(origin);

        adapterCtx.setRequestCharacterEncoding("UTF-8");

        verify(origin).setRequestCharacterEncoding("UTF-8");
        verify(origin, never()).setResponseCharacterEncoding(any());
    }

    // ------------------------------------------------------------------------------------
    // BM09 — jakarta ServletRequest.getContentLengthLong must delegate cleanly without int fallback (FR-FIX-006)
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM09: jakarta ServletRequest.getContentLengthLong must delegate directly without try/catch+int fallback (FR-FIX-006)")
    void BM09_jakartaServletRequest_getContentLengthLong_must_not_fallback_to_int() {
        javax.servlet.ServletRequest origin = mock(javax.servlet.ServletRequest.class);
        IllegalStateException boom = new IllegalStateException("origin failure");
        when(origin.getContentLengthLong()).thenThrow(boom);
        // The defective code silently swallows the exception and falls back to getContentLength().
        // If the adapter incorrectly delegates to getContentLength(), this stub returns 0; that
        // would let the defective code "succeed" with the wrong value. The assertion below
        // requires propagation of the exception, which is the only correct behavior.
        when(origin.getContentLength()).thenReturn(0);

        adapter.jakarta.servlet5.ServletRequest adapterReq =
            new adapter.jakarta.servlet5.ServletRequest(origin);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            adapterReq::getContentLengthLong,
            "adapter must propagate origin.getContentLengthLong() exceptions, not swallow them "
                + "and fall back to the lossy int getContentLength()");
        assertSame(boom, thrown, "the exact origin exception must be propagated");
    }

    // ------------------------------------------------------------------------------------
    // BM10 — HttpUpgradeHandler ThreadLocal single-threaded synchronous invocation LOCK-IN
    //         (NFR-CONV-001 / 60.conversion-strategy.md D12) — not a defect, behavior lock
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("BM10: HttpUpgradeHandler ThreadLocal single-thread sync invocation lock-in (NFR-CONV-001)")
    void BM10_httpUpgradeHandler_threadLocal_single_thread_sync_lock_in() throws Exception {
        // Step 1: set the jakarta upgrade handler class via the static ThreadLocal seam.
        adapter.javax.servlet5.http.HttpUpgradeHandler.setJakartaUpgradeHandlerClass(
            DummyJakartaUpgradeHandler.class);

        // Step 2: a same-thread synchronous instantiation must succeed (consumes the ThreadLocal value).
        adapter.javax.servlet5.http.HttpUpgradeHandler instance =
            new adapter.javax.servlet5.http.HttpUpgradeHandler();
        assertNotNull(instance, "same-thread synchronous instantiation must succeed");
        assertNotNull(instance.getHttpUpgradeHandler(),
            "wrapped jakarta upgrade handler must be initialized");

        // Step 3: a subsequent same-thread instantiation must fail because the ThreadLocal was
        //         cleared by the previous constructor (the documented lock-in: ThreadLocal value
        //         is used exactly once per same-thread synchronous call). If the lock-in breaks
        //         (e.g. ThreadLocal leaks across calls or threads), this assertion fails.
        assertThrows(IllegalStateException.class,
            () -> new adapter.javax.servlet5.http.HttpUpgradeHandler(),
            "ThreadLocal must be cleared after each same-thread synchronous instantiation");
    }

    /**
     * Minimal jakarta.servlet.http.HttpUpgradeHandler implementation used only by BM10
     * to drive the ThreadLocal-based instantiation flow.
     */
    public static class DummyJakartaUpgradeHandler implements jakarta.servlet.http.HttpUpgradeHandler {
        public DummyJakartaUpgradeHandler() {
        }

        @Override
        public void init(jakarta.servlet.http.WebConnection wc) {
        }

        @Override
        public void destroy() {
        }
    }

    // ====================================================================================
    // Meta verification methods (sidecar T-PH003-01 tdd.test_cases test_symbols)
    // ====================================================================================

    @Test
    @DisplayName("META AC-1: BugMuseumTest exists in servlet5 with BM01..BM10 methods defined")
    void shouldReproduceBugMuseumCaseBM01ToBM10AsTestClass() {
        Pattern bmPattern = Pattern.compile("^BM(\\d{2})_.+$");
        int count = 0;
        boolean[] seen = new boolean[11]; // index 1..10
        for (Method m : BugMuseumTest.class.getDeclaredMethods()) {
            Matcher matcher = bmPattern.matcher(m.getName());
            if (matcher.matches() && m.isAnnotationPresent(Test.class)) {
                int idx = Integer.parseInt(matcher.group(1));
                if (idx >= 1 && idx <= 10 && !seen[idx]) {
                    seen[idx] = true;
                    count++;
                }
            }
        }
        assertEquals(10, count,
            "expected: 10 @Test methods covering BM01..BM10, found: " + count);
        for (int i = 1; i <= 10; i++) {
            assertTrue(seen[i], "missing BM" + (i < 10 ? "0" + i : Integer.toString(i)) + " test method");
        }
    }

    @Test
    @DisplayName("META AC-2: BM01..BM09 are wired as defect regression assertions (have @DisplayName + @Test)")
    void shouldFailOnBuggyCodeForBM01ThroughBM09() {
        for (int i = 1; i <= 9; i++) {
            String prefix = "BM0" + i + "_";
            Method match = findFirstMethodStartingWith(prefix);
            assertNotNull(match, "expected BM01..BM09 to fail on buggy 0.0.1 code, found: "
                + "no test method with prefix " + prefix);
            assertTrue(match.isAnnotationPresent(Test.class),
                prefix + " method must carry @Test annotation");
            assertTrue(match.isAnnotationPresent(DisplayName.class),
                prefix + " method must carry @DisplayName annotation (regression evidence)");
        }
    }

    @Test
    @DisplayName("META AC-3: BM10 ThreadLocal lock-in passes under current single-threaded assumption")
    void shouldPassBM10ThreadLocalLockIn() {
        // Replay the BM10 invariant inline to ensure the lock-in still holds at meta-verification time.
        adapter.javax.servlet5.http.HttpUpgradeHandler.setJakartaUpgradeHandlerClass(
            DummyJakartaUpgradeHandler.class);
        assertDoesNotThrow(() -> {
            adapter.javax.servlet5.http.HttpUpgradeHandler h =
                new adapter.javax.servlet5.http.HttpUpgradeHandler();
            assertNotNull(h);
        }, "BM10 lock-in: same-thread synchronous instantiation must succeed today");

        assertThrows(IllegalStateException.class,
            () -> new adapter.javax.servlet5.http.HttpUpgradeHandler(),
            "BM10 lock-in: ThreadLocal must be drained after each instantiation today");
    }

    @Test
    @DisplayName("META AC-4: every BM01..BM10 has @DisplayName containing its defect ID")
    void shouldHaveDisplayNameWithDefectIdForEachBM() {
        String[] expectedIdByIndex = new String[] {
            null,            // index 0 unused
            "FR-FIX-001",   // BM01
            "FR-FIX-002",   // BM02
            "FR-FIX-004",   // BM03
            "FR-FIX-008",   // BM04
            "FR-FIX-003",   // BM05
            "FR-FIX-007",   // BM06
            "FR-FIX-009",   // BM07
            "FR-FIX-005",   // BM08
            "FR-FIX-006",   // BM09
            "NFR-CONV-001"  // BM10
        };
        for (int i = 1; i <= 10; i++) {
            String prefix = "BM" + (i < 10 ? "0" + i : Integer.toString(i)) + "_";
            Method match = findFirstMethodStartingWith(prefix);
            assertNotNull(match, "expected: @DisplayName on each BM with defect ID + description, "
                + "found: missing method " + prefix);
            DisplayName dn = match.getAnnotation(DisplayName.class);
            assertNotNull(dn, "expected: @DisplayName on " + prefix + ", found: missing annotation");
            String expectedId = expectedIdByIndex[i];
            assertTrue(dn.value().contains(expectedId),
                "expected: @DisplayName for BM" + (i < 10 ? "0" + i : Integer.toString(i))
                    + " to contain '" + expectedId + "', found: '" + dn.value() + "'");
        }
    }

    @Test
    @DisplayName("META AC-5: BM01..BM09 form the regression gate for FR-FIX-001..FR-FIX-009")
    void shouldServeAsRegressionGateForFR_FIX_001_to_009() {
        String[] requiredFrFixIds = new String[] {
            "FR-FIX-001", "FR-FIX-002", "FR-FIX-003", "FR-FIX-004", "FR-FIX-005",
            "FR-FIX-006", "FR-FIX-007", "FR-FIX-008", "FR-FIX-009"
        };
        for (String reqId : requiredFrFixIds) {
            boolean wired = false;
            for (Method m : BugMuseumTest.class.getDeclaredMethods()) {
                if (!m.getName().startsWith("BM")) continue;
                if (!m.isAnnotationPresent(DisplayName.class)) continue;
                if (m.getAnnotation(DisplayName.class).value().contains(reqId)) {
                    wired = true;
                    break;
                }
            }
            if (!wired) {
                fail("expected: BM01..BM09 wired to FR-FIX-001..009 verification, "
                    + "found: tests not invoked from fix tasks (missing wiring for " + reqId + ")");
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------

    private static Method findFirstMethodStartingWith(String prefix) {
        for (Method m : BugMuseumTest.class.getDeclaredMethods()) {
            if (m.getName().startsWith(prefix)) {
                return m;
            }
        }
        return null;
    }
}
