package adapter.jakarta.servlet6;

import adapter.common.AdapterUnwrap;

import jakarta.servlet.ServletConnection;

import java.util.UUID;

/**
 * Synthesized fallback for {@link jakarta.servlet.ServletConnection} (Servlet 6.0 신규).
 * javax 측 원본에는 본 인터페이스 자체가 없으므로 어댑터가 합성한다 — SRS §6.2.2 policy D
 * (소거된 메서드 대체) 와 동일한 패턴이며, 본 폴백은 strict 모드에서도 default 유지된다.
 *
 * <p>{@link AdapterUnwrap}{@code <Void>} 마커 구현 (sibling {@code EmptyHttpSessionContext}
 * 와 동일 컨벤션) — 원본이 없으므로 {@code unwrap()} 은 {@code null}.
 *
 * @req FR-CONV-001
 */
final class DummyServletConnection implements ServletConnection, AdapterUnwrap<Void> {

    private final String connectionId;

    // @req FR-CONV-001
    DummyServletConnection() {
        this.connectionId = UUID.randomUUID().toString();
    }

    // @req FR-CONV-003
    @Override public Void unwrap() {
        return null;
    }

    // @req FR-CONV-001
    @Override public String getConnectionId() {
        return this.connectionId;
    }

    // @req FR-CONV-001
    @Override public String getProtocol() {
        return "HTTP/1.1";
    }

    // @req FR-CONV-001
    @Override public String getProtocolConnectionId() {
        return "";
    }

    // @req FR-CONV-001
    @Override public boolean isSecure() {
        return false;
    }
}
