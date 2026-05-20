# 2026-05-19-jakarta-adapter-v1-0-0-v03 — Phase Checklist

> PM 자동 생성 파일. plan.md 의 보조 뷰. 정규 SSOT 는 pm-state.json.
> 생성: 2026-05-19T17:18:00Z / plan: 2026-05-19-jakarta-adapter-v1-0-0-v03.plan.md

## Phase PH-003: 결함 9건 + Bug Museum (TDD red+green 페어)
- [x] **T-PH003-09** FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding) — TDD red (failing test)
- [x] **T-PH003-10** FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding) — TDD green (impl fix)
- [x] **T-PH003-11** FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭) — TDD red (failing test)
- [x] **T-PH003-12** FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭) — TDD green (impl fix)
- [x] **T-PH003-13** FR-FIX-006 getContentLengthLong fallback 부정확 (try/catch + int fallback) — TDD red (failing test)
- [x] **T-PH003-14** FR-FIX-006 getContentLengthLong fallback 부정확 (try/catch + int fallback) — TDD green (impl fix)
- [x] **T-PH003-15** FR-FIX-007 jakarta ServletRequest.getRealPath @Override 부재 메서드 — TDD red (failing test)
- [x] **T-PH003-16** FR-FIX-007 jakarta ServletRequest.getRealPath @Override 부재 메서드 — TDD green (impl fix)
- [x] **T-PH003-17** FR-FIX-008 ServletReqResConverter instanceof 분기 namespace 오류 4건 — TDD red (failing test)
- [x] **T-PH003-18** FR-FIX-008 ServletReqResConverter instanceof 분기 namespace 오류 4건 — TDD green (impl fix)
- [x] **T-PH003-19** FR-FIX-009 HttpServletRequest.getCookies null 미처리 — TDD red (failing test)
- [x] **T-PH003-20** FR-FIX-009 HttpServletRequest.getCookies null 미처리 — TDD green (impl fix)
- [~] **T-PH003-21** BM01~BM10 회귀 통과 + servlet6/61 사본 동기화 검증

## Phase PH-004: 변환 의미론 + 폴백 정책 (TDD red+green 페어)
- [ ] **T-PH004-01** FR-CONV-003 AdapterUnwrap marker 인터페이스 (common 모듈 신설) — TDD red (failing test)
- [ ] **T-PH004-02** FR-CONV-003 AdapterUnwrap marker 인터페이스 (common 모듈 신설) — TDD green (impl)
- [ ] **T-PH004-03** FR-CONV-003 + FR-CONV-004 모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피 — TDD red (failing test)
- [ ] **T-PH004-04** FR-CONV-003 + FR-CONV-004 모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피 — TDD green (impl)
- [ ] **T-PH004-05** FR-CONV-005 ServletException 양방향 변환 (try/catch + cause chain) — TDD red (failing test)
- [ ] **T-PH004-06** FR-CONV-005 ServletException 양방향 변환 (try/catch + cause chain) — TDD green (impl)
- [ ] **T-PH004-07** FR-CONV-006 + FR-CONV-007 + FR-CONV-008 + FR-CONV-009 컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16 — TDD red (failing test)
- [ ] **T-PH004-08** FR-CONV-006 + FR-CONV-007 + FR-CONV-008 + FR-CONV-009 컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16 — TDD green (impl)
- [ ] **T-PH004-09** FR-CONV-011 strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위 — TDD red (failing test)
- [ ] **T-PH004-10** FR-CONV-011 strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위 — TDD green (impl)
- [ ] **T-PH004-11** FR-CONV-012 폴백 가시성 (java.util.logging dedup + diagnostics API) — TDD red (failing test)
- [ ] **T-PH004-12** FR-CONV-012 폴백 가시성 (java.util.logging dedup + diagnostics API) — TDD green (impl)
- [ ] **T-PH004-13** FR-CONV-010 + FR-CONV-014 FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위 — TDD red (failing test)
- [ ] **T-PH004-14** FR-CONV-010 + FR-CONV-014 FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위 — TDD green (impl)
- [ ] **T-PH004-15** FR-CONV-013 Cookie attribute API 정책 D (no-op + WARN once) — TDD red (failing test)
- [ ] **T-PH004-16** FR-CONV-013 Cookie attribute API 정책 D (no-op + WARN once) — TDD green (impl)
- [ ] **T-PH004-17** FR-CONV-001 jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현 — TDD red (failing test)
- [ ] **T-PH004-18** FR-CONV-001 jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현 — TDD green (impl)
- [ ] **T-PH004-19** FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override — TDD red (failing test)
- [ ] **T-PH004-20** FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override — TDD green (impl)
- [ ] **T-PH004-21** NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10) — TDD red (failing test)
- [ ] **T-PH004-22** NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10) — TDD green (impl)
- [ ] **T-PH004-23** PH-004 폴백 정책 / strict 모드 / AdapterUnwrap / 로깅 / diagnostics 통합 회귀 + 단위 테스트 매트릭스 검증

## Phase PH-005: 테스트 인프라
- [ ] **T-PH005-01** 어댑터 클래스당 단위 테스트 클래스 (Mockito RETURNS_SMART_NULLS) + 모든 위임 메서드 verify + 폴백 정책 분기 검증
- [ ] **T-PH005-02** common/src/testFixtures 에 DelegationContractTest driver 작성 + 각 버전 모듈에서 호출
- [ ] **T-PH005-06** 각 모듈에 JaCoCo 플러그인 + 커버리지 게이트 (라인 95% / 분기 100% for converter, 90% for adapter)
- [ ] **T-PH005-07** PH-005 의 6개 task 결과 통합 검토 — 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과

## Phase PH-006: CI 매트릭스 + JaCoCo
- [ ] **T-PH006-03** JaCoCo 보고서 PR 댓글 + Codecov 업로드 + 게이트 95%/100% 적용 검증

## Phase PH-007: Maven Central 게시
- [ ] **T-PH007-01** 각 모듈 build.gradle 에 maven-publish + signing 플러그인 + publication 정의
- [ ] **T-PH007-02** README.md 전면 개정 — 호환성 매트릭스 + Cookie attribute 손실 + setStatus reason phrase 폐기 + getValue alias + strict 모드 + 0.x 마이그레이션
- [ ] **T-PH007-03** CHANGELOG.md 작성 — 0.0.1 → 1.0.0 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 추가 + 폴백 정책
- [ ] **T-PH007-04** 마이그레이션 가이드 작성 + D8 옵션 B (servlet5 내 deprecated alias 패키지) 구현
- [ ] **T-PH007-07** v1.0.0 git tag + GitHub Release 작성 + 최종 회귀 (BM + IT + cross-version 전수)

