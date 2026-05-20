# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-05-20

v1.0.0 은 0.0.1 의 결함 일괄 수정과 함께 Servlet 5 / 6 / 6.1 동시 지원을 위한 multi-module 구조로 재편된 메이저 릴리스다. 본 릴리스 이전에 0.0.1 을 사용하던 사용자는 마이그레이션 가이드 (`docs/migration/0.x-to-1.0.md`) 와 호환성 매트릭스 (README) 를 함께 확인해야 한다.

### Added

- **Multi-module 구조 도입.** 0.0.1 의 단일 모듈을 `common` + `servlet5` + `servlet6` + `servlet61` 4 모듈로 분리. 사용자는 Servlet API 버전에 맞는 단일 artifact 만 의존하여 ClassLoader 상에서 jakarta-servlet-api 5.0 / 6.0 / 6.1 충돌을 회피할 수 있다. (FR-MOD-001 ~ FR-MOD-007)

- **Servlet 6.0 및 6.1 동시 지원.** Servlet 5.0 (기존 0.0.1 대상) 외에 jakarta-servlet-api 6.0.x (`servlet6` 모듈, Java 11+) 와 6.1.x (`servlet61` 모듈, Java 17+) 를 신규 지원. 각 모듈은 해당 API 버전에서 도입·제거된 시그니처에 대해 별도 fallback 으로 컴파일·런타임 `AbstractMethodError` 를 회피한다. (FR-MOD-002 ~ FR-MOD-006)

- **신규 진입점 별칭 (`com.snoworca.ServletAdapter5` / `ServletAdapter6` / `ServletAdapter61`).** 기존 `com.snoworca.ServletAdapter` 는 모든 모듈에서 동일 FQCN 으로 유지되어 단일 ClassLoader 환경의 import 호환성을 보존한다. 추가로 사용자가 어떤 모듈을 의존 중인지 코드 상에서 명시하고자 할 때 `ServletAdapter5/6/61` 모듈별 별칭을 사용할 수 있다. (FR-MOD-006)

- **호환성 매트릭스 및 마이그레이션 가이드.** README 에 어댑터 모듈 ↔ jakarta-servlet-api ↔ javax-servlet-api ↔ Java 최소 버전 ↔ Tomcat 권장 버전 매트릭스를 포함. `docs/migration/0.x-to-1.0.md` 에 0.0.1 → 1.0.0 마이그레이션 경로 (import 변경, 의존 좌표 변경, deprecated alias 사용 예) 를 안내한다. (FR-REL-001, MIG-REL-001)

### Changed

- **폴백 정책 변경.** 0.0.1 의 silent 폴백 (정보 손실 시점 미고지) 을 명시적 정책으로 정형화. 변환 의미론별 폴백 동작은 README "정보 손실 정책" 섹션에 5개 항목으로 명문화된다.
  - Cookie attribute API (`Cookie.setAttribute("SameSite", ...)`) 의 정보 손실 처리 (FR-CONV-013).
  - `HttpServletResponse.setStatus(int, String)` 의 reason phrase 폐기 처리 (FR-CONV-010).
  - `HttpSession.getValue/putValue/removeValue/getValueNames` deprecated alias 의 attribute API 자동 폴백 (FR-CONV-010).
  - `strict` 모드 (FR-CONV-011) — 정보 손실 시 silent 대신 예외를 발생시키는 옵트인 활성화 경로.
  - 0.0.1 → 1.0.0 마이그레이션 안내 (import path 변경, deprecated alias 사용 권장).

### Fixed

0.0.1 코드의 결함 9건을 일괄 수정한다. 모든 수정은 `servlet5` 모듈로 이동된 코드에 적용되고, `servlet6` / `servlet61` 모듈에는 사본 기준으로 동일하게 반영된다. 각 결함은 Bug Museum 회귀 테스트 (BM01~BM10) 로 보호된다.

- **FR-FIX-001** — `StreamConverter.convert(...)` 내부 익명 `InputStream.read(byte[], int, int)` 가 원본의 `readLine(byte[], int, int)` 를 호출하여 일반 바이너리 본문에서 LF 경계에서 잘못 멈추던 silent data corruption 결함 수정. 정상적으로 동명 `read(byte[], int, int)` 로 위임한다.
- **FR-FIX-002** — `adapter.{jakarta,javax}.servlet.http.Part#getSubmittedFileName()` 가 자기 자신을 호출해 `StackOverflowError` 후 catch 분기에서 `getName()` 을 반환하던 결함 수정. 멀티파트 업로드의 파일명 손실 회복.
- **FR-FIX-003** — `adapter.{jakarta,javax}.servlet.ServletResponse#setContentLengthLong(long)` 의 `(int)arg0` narrow cast silent overflow 결함 수정. 2GB 이상 응답의 Content-Length 정확 전달 회복.
- **FR-FIX-004** — `adapter.jakarta.servlet.ServletContext#setResponseCharacterEncoding(String)` 가 `setRequestCharacterEncoding` 을 잘못 호출하여 응답 인코딩이 기본값으로 유지되고 요청 인코딩이 오염되던 이중 결함 수정.
- **FR-FIX-005** — `adapter.javax.servlet.ServletContext#setRequestCharacterEncoding(String)` 가 `setResponseCharacterEncoding` 을 호출하던 D4 의 반대 방향 결함 수정. 양방향 어댑터 대칭성 회복.
- **FR-FIX-006** — `adapter.{jakarta,javax}.servlet.ServletRequest#getContentLengthLong()` 의 도달 불가능한 `try/catch` 와 `getContentLength()` int 반환 fallback 제거. 2GB 이상 요청의 Content-Length 정확 반환.
- **FR-FIX-007** — `adapter.jakarta.servlet.ServletRequest#getRealPath(String)` 의 잘못된 `@Override` 제거. jakarta 5.0+ `ServletRequest` 인터페이스에 부재한 메서드를 어댑터에서 제거하여 사용자가 인터페이스 경유로는 호출할 수 없음을 명확히 한다.
- **FR-FIX-008** — `ServletReqResConverter.convert(...)` 의 `instanceof javax.servlet.http.HttpServletResponse` 분기가 항상 false 였던 결함 수정. 4 방향 (javax↔jakarta × Request/Response) 모두에서 HTTP 응답이 일반 `ServletResponse` 로 다운그레이드되지 않고 적절한 `HttpServletResponse` 어댑터로 wrap 된다.
- **FR-FIX-009** — `adapter.{jakarta,javax}.servletN.http.HttpServletRequest#getCookies()` 의 원본 `null` 반환 시 NPE 가 발생하던 결함 수정. 원본이 `null` 이면 어댑터도 `null` 을 반환한다.

[1.0.0]: https://github.com/Snoworca/Jakarta_To_Javax_Servlet_Adapter/releases/tag/v1.0.0
