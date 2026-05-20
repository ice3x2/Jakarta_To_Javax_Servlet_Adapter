package adapter.common;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConverterSupport {

    /**
     * Strict 모드 시스템 프로퍼티 / ServletContext init-param 공용 키.
     *
     * @req FR-CONV-011
     */
    public static final String STRICT_MODE_KEY = "com.snoworca.adapter.strict";

    /**
     * FR-CONV-012 — java.util.logging Logger 이름. SPEC-8 명세에 따라 고정.
     *
     * @req FR-CONV-012
     */
    public static final String LOGGER_NAME = "com.snoworca.servletadapter";

    /**
     * FR-CONV-012 — diagnostics opt-in 시스템 프로퍼티 키. "true" 일 때만
     * diagnostics map 이 활성화되어 호출 카운트를 누적/노출한다.
     *
     * @req FR-CONV-012
     */
    public static final String DIAGNOSTICS_KEY = "com.snoworca.adapter.diagnostics";

    /**
     * SPEC-9 strict 활성 시에도 default 동작을 유지해야 하는 메서드 이름 set.
     * - getRequestId / getServletConnection: 정책표 F 동일 (예외 금지)
     * - getProtocolRequestId: 정책표 D 동일 (빈 문자열 반환 유지)
     *
     * @req FR-CONV-011
     */
    private static final String[] STRICT_BYPASS_METHODS = {
            "getRequestId",
            "getServletConnection",
            "getProtocolRequestId"
    };

    private static volatile boolean strictMode = false;

    /**
     * FR-CONV-012 — once-per-method dedup set. {@link #logFallbackOnce} 가
     * methodKey 를 putIfAbsent 로 등록하며, 이미 존재하면 LogRecord 발행을
     * skip 한다. ConcurrentHashMap 으로 thread-safe.
     *
     * @req FR-CONV-012
     */
    private static final ConcurrentHashMap<String, Boolean> LOGGING_DEDUP =
            new ConcurrentHashMap<>();

    /**
     * FR-CONV-012 — diagnostics 호출 카운터. opt-in 활성 시 methodKey 별로
     * AtomicLong 을 누적한다. opt-in OFF 시 본 map 은 비어 있고
     * {@link #incrementDiagnostics} 는 no-op.
     *
     * @req FR-CONV-012
     */
    private static final ConcurrentHashMap<String, AtomicLong> DIAGNOSTICS =
            new ConcurrentHashMap<>();

    /**
     * FR-CONV-012 — diagnostics opt-in 상태. {@link #initDiagnosticsFromSystemProperty}
     * 가 시스템 프로퍼티 값을 읽어 결정한다.
     *
     * @req FR-CONV-012
     */
    private static volatile boolean diagnosticsEnabled = false;

    private ConverterSupport() {
    }

    public static Throwable unwrap(final Throwable t) {
        Throwable current = t;
        while (current instanceof InvocationTargetException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    public static RuntimeException rethrow(final Throwable t) {
        if (t == null) {
            return new RuntimeException("null throwable");
        }
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        return new RuntimeException(t);
    }

    /**
     * javax.servlet.ServletException 을 jakarta.servlet.ServletException 으로
     * 변환한다. 원본은 cause 로 보존된다 (initCause via constructor).
     *
     * @req FR-CONV-005
     */
    public static jakarta.servlet.ServletException javaxToJakartaServletException(
            final javax.servlet.ServletException source) {
        if (source == null) {
            return new jakarta.servlet.ServletException();
        }
        return new jakarta.servlet.ServletException(source.getMessage(), source);
    }

    /**
     * jakarta.servlet.ServletException 을 javax.servlet.ServletException 으로
     * 변환한다. 원본은 cause 로 보존된다 (initCause via constructor).
     *
     * @req FR-CONV-005
     */
    public static javax.servlet.ServletException jakartaToJavaxServletException(
            final jakarta.servlet.ServletException source) {
        if (source == null) {
            return new javax.servlet.ServletException();
        }
        return new javax.servlet.ServletException(source.getMessage(), source);
    }

    /**
     * 표준 예외 (IOException / IllegalStateException / RuntimeException 등) 는
     * 동일 인스턴스로 그대로 propagate 한다. ServletException 양방향 변환은 본
     * helper 가 담당하지 않으며, 호출 측이 명시적으로 javaxToJakarta /
     * jakartaToJavax 헬퍼로 변환해야 한다.
     *
     * @req FR-CONV-005
     */
    public static Throwable rethrowOrConvert(final Throwable t) {
        return t;
    }

    /**
     * 시스템 프로퍼티 `com.snoworca.adapter.strict` 가 "true" 면 strict 모드를
     * 활성화한다. 시스템 프로퍼티는 init-param 보다 우선 — strict 가 한 번
     * 활성화되면 이후 init-param 미설정으로 인해 OFF 로 떨어지지 않는다
     * (initStrictModeFromServletContext 참조).
     *
     * @req FR-CONV-011
     */
    public static void initStrictModeFromSystemProperty() {
        String value = System.getProperty(STRICT_MODE_KEY);
        if (value != null && "true".equalsIgnoreCase(value.trim())) {
            strictMode = true;
        }
    }

    /**
     * ServletContext init-param `com.snoworca.adapter.strict=true` 가 설정되어
     * 있으면 strict 모드를 활성화한다. 시스템 프로퍼티가 먼저 ON 으로 설정한
     * 경우 본 메서드는 strictMode 를 false 로 되돌리지 않는다 (시스템 프로퍼티
     * 우선 원칙).
     *
     * @req FR-CONV-011
     */
    public static void initStrictModeFromServletContext(
            final javax.servlet.ServletContext context) {
        if (context == null) {
            return;
        }
        String value = context.getInitParameter(STRICT_MODE_KEY);
        if (value != null && "true".equalsIgnoreCase(value.trim())) {
            strictMode = true;
        }
    }

    /**
     * strict 모드가 활성화되어 있으면 {@link UnsupportedOperationException} 을
     * throw 한다. 비활성 시 (lenient default) 예외 없이 정상 return 하여 호출
     * 측의 폴백 분기 (B/D/E/F) 가 계속 진행되도록 한다.
     *
     * @req FR-CONV-011
     */
    public static void throwIfStrict(final String message) {
        if (strictMode) {
            throw new UnsupportedOperationException(
                    message == null ? "strict mode" : message);
        }
    }

    /**
     * 정책표상 strict 활성 시에도 default 동작을 유지해야 하는 메서드인지
     * 판정한다. getRequestId / getServletConnection (F 동일) 과
     * getProtocolRequestId (D 동일) 는 strict 검사에서 면제된다.
     *
     * @req FR-CONV-011
     */
    public static boolean shouldBypassStrictFor(final String methodName) {
        if (methodName == null) {
            return false;
        }
        for (String bypass : STRICT_BYPASS_METHODS) {
            if (bypass.equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 테스트 격리용 — strict 모드 상태를 false 로 reset 한다. production
     * 코드에서 호출하지 않는다.
     *
     * @req FR-CONV-011
     */
    public static void resetStrictModeForTesting() {
        strictMode = false;
    }

    /**
     * FR-CONV-012 — 폴백 발생 시 1회만 LogRecord 를 emit 한다. 동일 methodKey
     * 의 두 번째 호출부터는 dedup 되어 emit 되지 않는다. ConcurrentHashMap.
     * putIfAbsent 의 atomic 결과로 첫 호출자만 publish 한다.
     *
     * <p>level 매핑: 정책 B/D/F → {@link Level#WARNING}, 정책 E → {@link Level#INFO}
     * 는 호출 측 책임이며, 본 메서드는 전달받은 level 을 그대로 사용한다.
     *
     * <p>methodKey 형식 권장: {@code "<ClassName>.<methodName>@<from>-><to>"}
     * (예: {@code HttpSession.getValue@javax->jakarta}).
     *
     * @param methodKey dedup 키. null 또는 빈 문자열이면 no-op.
     * @param level     {@link Level} (null 이면 {@link Level#WARNING} 으로 fallback).
     * @param message   사람이 읽을 메시지.
     * @req FR-CONV-012
     */
    public static void logFallbackOnce(final String methodKey,
                                       final Level level,
                                       final String message) {
        if (methodKey == null || methodKey.isEmpty()) {
            return;
        }
        if (LOGGING_DEDUP.putIfAbsent(methodKey, Boolean.TRUE) != null) {
            return;
        }
        Logger logger = Logger.getLogger(LOGGER_NAME);
        Level effective = level != null ? level : Level.WARNING;
        String text = message != null
                ? "[" + methodKey + "] " + message
                : "[" + methodKey + "]";
        logger.log(effective, text);
    }

    /**
     * FR-CONV-012 — diagnostics opt-in 시스템 프로퍼티
     * ({@value #DIAGNOSTICS_KEY}) 를 읽어 활성/비활성을 결정한다.
     * 값이 "true" (대소문자 무시) 일 때만 활성화. 그 외 값/null 은 비활성.
     *
     * @req FR-CONV-012
     */
    public static void initDiagnosticsFromSystemProperty() {
        String value = System.getProperty(DIAGNOSTICS_KEY);
        diagnosticsEnabled = value != null && "true".equalsIgnoreCase(value.trim());
    }

    /**
     * FR-CONV-012 — diagnostics 활성 상태일 때만 methodKey 호출 카운트를 +1
     * 한다. {@link ConcurrentHashMap#computeIfAbsent} + {@link AtomicLong} 으로
     * thread-safe 보장. opt-in OFF 시 no-op.
     *
     * @param methodKey 카운터 키. null 또는 빈 문자열이면 no-op.
     * @req FR-CONV-012
     */
    public static void incrementDiagnostics(final String methodKey) {
        if (!diagnosticsEnabled) {
            return;
        }
        if (methodKey == null || methodKey.isEmpty()) {
            return;
        }
        DIAGNOSTICS.computeIfAbsent(methodKey, k -> new AtomicLong(0L))
                .incrementAndGet();
    }

    /**
     * FR-CONV-012 — diagnostics 카운터의 현재 snapshot 을 {@code Map<String, Long>}
     * 형태로 반환한다. opt-in 비활성 또는 누적된 카운트가 없으면 empty Map.
     *
     * <p>반환 Map 은 호출 시점 snapshot 이며 이후 변경이 반영되지 않는다.
     *
     * @return 호출 카운트 snapshot (unmodifiable). 비활성 시 empty.
     * @req FR-CONV-012
     */
    public static Map<String, Long> diagnosticsSnapshot() {
        if (!diagnosticsEnabled) {
            return Collections.emptyMap();
        }
        Map<String, Long> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, AtomicLong> e : DIAGNOSTICS.entrySet()) {
            snapshot.put(e.getKey(), e.getValue().get());
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * 테스트 격리용 — logging dedup set 을 비운다. production 코드에서
     * 호출하지 않는다.
     *
     * @req FR-CONV-012
     */
    public static void resetLoggingDedupForTesting() {
        LOGGING_DEDUP.clear();
    }

    /**
     * 테스트 격리용 — diagnostics 카운터와 opt-in 상태를 초기화한다.
     * production 코드에서 호출하지 않는다.
     *
     * @req FR-CONV-012
     */
    public static void resetDiagnosticsForTesting() {
        DIAGNOSTICS.clear();
        diagnosticsEnabled = false;
    }
}
