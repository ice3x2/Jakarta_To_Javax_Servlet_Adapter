package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter61.ServletConverter;

/**
 * IT04 — SameSite / Partitioned Cookie attribute lossy-policy regression
 * integration test (servlet61 module).
 *
 * <p>FR-CONV-013 SPEC-15 정책 D 의 lossy 정책 (자체 attribute Map 미보관 +
 * Set-Cookie 헤더 합성 책임 없음) 이 통합 환경에서도 회귀 없이 유지됨을
 * 보장한다. 어댑터의 javax-side {@code adapter.javax.servlet61.http.Cookie}
 * 에 {@code setAttribute("SameSite", "Strict|Lax|None")} 또는
 * {@code setAttribute("Partitioned", "")} 를 호출한 뒤 Tomcat 11.0.x 가
 * 발행하는 실제 {@code Set-Cookie} 응답 헤더에 {@code SameSite} /
 * {@code Partitioned} 토큰이 출력되지 않음을 검증한다 (= 정보 손실 정책
 * 회귀 보장, SRS FR-CONV-013 AC-4 라인 727).
 *
 * <p>servlet5 모듈은 본 IT04 의 대상이 아니다. 사유: jakarta 5.0 (Tomcat 10.0.x)
 * 의 jakarta.servlet.http.Cookie 에는 {@code setAttribute} API 자체가 없어
 * SameSite/Partitioned 의 lossy 회귀 시나리오가 성립하지 않는다.
 *
 * @req FR-CONV-013
 * @req FR-TEST-003
 */
@Tag("integration")
class IT04SameSiteTest {

    private static final String COOKIE_NAME = "sid";
    private static final String COOKIE_VALUE = "abc123";

    /**
     * @req FR-CONV-013
     */
    @Test
    void shouldEmitSameSiteStrict() throws Exception {
        runLossyCase("SameSite", "Strict");
    }

    /**
     * @req FR-CONV-013
     */
    @Test
    void shouldEmitSameSiteLax() throws Exception {
        runLossyCase("SameSite", "Lax");
    }

    /**
     * @req FR-CONV-013
     */
    @Test
    void shouldEmitSameSiteNone() throws Exception {
        runLossyCase("SameSite", "None");
    }

    /**
     * @req FR-CONV-013
     */
    @Test
    void shouldEmitPartitioned() throws Exception {
        runLossyCase("Partitioned", "");
    }

    /**
     * SPEC-15 정책 D lossy 회귀 검증 공통 핸들러. 어댑터 javax-side Cookie 에
     * {@code setAttribute(attrName, attrValue)} 를 호출한 뒤 실제 HTTP 응답의
     * {@code Set-Cookie} 헤더에 해당 attribute 가 누락됨을 검증한다.
     *
     * @req FR-CONV-013
     */
    private void runLossyCase(String attrName, String attrValue) throws Exception {
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new SameSiteServlet(attrName, attrValue);
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            runner.addServlet("samesite", "/samesite", jakartaServlet);
            runner.start();

            HttpURLConnection conn = (HttpURLConnection) new URL(runner.baseUrl() + "/samesite").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            assertEquals(200, status, "status");

            String setCookie = findSetCookieHeader(conn.getHeaderFields(), COOKIE_NAME);
            assertNotNull(setCookie, "Set-Cookie header for " + COOKIE_NAME + " present");

            // 기본 name=value 는 여전히 출력되어야 함 (lossy 는 SameSite/Partitioned 만)
            assertTrue(setCookie.startsWith(COOKIE_NAME + "=" + COOKIE_VALUE),
                    "Set-Cookie starts with name=value, was: " + setCookie);

            String setCookieLower = setCookie.toLowerCase(Locale.ROOT);
            String attrLower = attrName.toLowerCase(Locale.ROOT);

            // SPEC-15 정책 D: setAttribute no-op → Set-Cookie 헤더에 attribute 미출력 (lossy)
            assertFalse(setCookieLower.contains(attrLower),
                    "Set-Cookie must NOT contain '" + attrName + "' (SPEC-15 정책 D lossy 회귀 위반), was: " + setCookie);

            readAll(conn.getInputStream());
        }
    }

    private static String findSetCookieHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            if (!"Set-Cookie".equalsIgnoreCase(key)) {
                continue;
            }
            for (String value : e.getValue()) {
                if (value != null && value.startsWith(name + "=")) {
                    return value;
                }
            }
        }
        return null;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * 어댑터 javax-side Cookie 인스턴스 ({@code adapter.javax.servlet61.http.Cookie})
     * 에 SameSite/Partitioned attribute 를 setAttribute 로 주입한 뒤 응답에 add 한다.
     * 어댑터의 {@code setAttribute} 는 SPEC-15 정책 D 에 따라 no-op + WARN once 이므로
     * Set-Cookie 헤더에 해당 attribute 가 출력되지 않아야 한다.
     *
     * @req FR-CONV-013
     */
    static final class SameSiteServlet extends HttpServlet {
        private final String attrName;
        private final String attrValue;

        SameSiteServlet(String attrName, String attrValue) {
            this.attrName = attrName;
            this.attrValue = attrValue;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // 어댑터 javax-side Cookie 직접 생성 — 사용자가 jakarta 6.0+ 의 setAttribute 의도를
            // 어댑터 경유로 전달하는 유일한 경로 (javax.servlet.http.Cookie 표준 API 에는 setAttribute 부재).
            adapter.javax.servlet61.http.Cookie c =
                    new adapter.javax.servlet61.http.Cookie(new jakarta.servlet.http.Cookie(COOKIE_NAME, COOKIE_VALUE));
            c.setPath("/");
            c.setAttribute(attrName, attrValue);

            resp.addCookie(c);
            resp.setStatus(200);
            resp.setContentType("text/plain; charset=UTF-8");
            byte[] payload = "ok".getBytes(StandardCharsets.UTF_8);
            resp.setContentLength(payload.length);
            resp.getOutputStream().write(payload);
        }
    }
}
