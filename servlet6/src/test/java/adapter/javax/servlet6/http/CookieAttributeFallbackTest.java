package adapter.javax.servlet6.http;

import adapter.common.ConverterSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;

/**
 * FR-CONV-013 / SPEC-15 정책 D — javax-side Cookie 의 fallback 정책 검증.
 *
 * jakarta 6.0+ {@code Cookie.setAttribute(String,String)} 는 javax 4.x 에 존재하지 않으므로,
 * 어댑터는 호출을 no-op 으로 처리하고 {@link ConverterSupport#logFallbackOnce} 로 1회만
 * WARN 로그를 emit 한다 (Cookie.java:127-131). attribute Map 은 별도로 보관하지 않으므로
 * {@code getAttribute} 는 항상 null, {@code getAttributes} 는 항상 emptyMap sentinel 을
 * 반환한다 (Cookie.java:138-151).
 *
 * Mock 정책 (§0.6 / FR-TEST-001 AC-3): 원본 jakarta Cookie 만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock 한다.
 *
 * @req FR-TEST-001
 * @req FR-CONV-013
 */
class CookieAttributeFallbackTest {

    private CapturingHandler handler;
    private Logger logger;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        ConverterSupport.resetLoggingDedupForTesting();
        logger = Logger.getLogger(ConverterSupport.LOGGER_NAME);
        previousLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
        handler = new CapturingHandler();
        logger.addHandler(handler);
    }

    @AfterEach
    void tearDown() {
        if (logger != null && handler != null) {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
        }
        ConverterSupport.resetLoggingDedupForTesting();
    }

    // ----------------------------------------------------------------------
    // AC-2 FR-CONV-013 fallback 검증: setAttribute 는 (1) no-op (원본에 위임 0회) +
    // (2) 1회만 WARN log emit + (3) getAttribute 는 null + (4) getAttributes 는 empty.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2 / FR-CONV-013: Cookie.setAttribute 는 no-op + WARN-once 로 동작한다")
    void shouldNoOpSetAttributeWithWarnOnce_AC2() {
        jakarta.servlet.http.Cookie origin =
            mock(jakarta.servlet.http.Cookie.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        Cookie adapter = new Cookie(origin);

        // (1) 두 번 setAttribute 호출 — no-op 이어야 한다.
        adapter.setAttribute("X-Test", "v1");
        adapter.setAttribute("X-Test", "v2");

        // 원본 jakarta cookie 에는 어떤 호출도 발생하지 않는다 (no-op).
        verifyNoInteractions(origin);

        // (2) WARN-once: 동일 methodKey 의 첫 호출만 emit. 두 번째는 dedup.
        List<LogRecord> warnings = new ArrayList<>();
        for (LogRecord r : handler.records) {
            if (r.getLevel() == Level.WARNING) {
                warnings.add(r);
            }
        }
        assertEquals(1, warnings.size(),
            "동일 methodKey 의 fallback 로그는 1회만 emit 된다 (dedup)");
        assertNotNull(warnings.get(0).getMessage(), "WARN 메시지가 존재해야 한다");
        assertTrue(warnings.get(0).getMessage().contains("Cookie.setAttribute"),
            "WARN 메시지에 methodKey (Cookie.setAttribute) 가 포함된다");

        // (3) getAttribute 는 항상 null.
        assertNull(adapter.getAttribute("X-Test"),
            "보관소가 없으므로 getAttribute 는 null");

        // (4) getAttributes 는 emptyMap sentinel.
        assertTrue(adapter.getAttributes().isEmpty(),
            "getAttributes 는 항상 empty Map 을 반환한다");
    }

    /** java.util.logging.Handler 로 LogRecord 를 수집하는 단순 캡처. */
    private static final class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
