# kiwi-srs-feasibility 보고서 — 2026-05-20.jakarta-adapter.v100.v02

## 1. 메타

| 항목 | 값 |
|---|---|
| run-id | `2026-05-20.jakarta-adapter.v100.v02` |
| target | `v1.0.0` |
| 평가일 | 2026-05-20 |
| 모드 | live (정책 파일 부재 → §0.G6 기본 매핑 + 사용자 결정 보완) |
| 평가 대상 | draft 4건 + 핵심 evolving 4건 = 8 REQ |
| 사용자 의도 | "검증 테스트를 철저히할 계획" — E2E plan 토대 마련 |

## 2. Feasibility 분포 + Target 종합 판정

| Feasibility | 개수 | REQ |
|---|---|---|
| high | 4 | FR-TEST-001, FR-TEST-002, FR-TEST-004, NFR-TEST-001 |
| medium (상단, evolving 승급 권고) | 2 | FR-TEST-003 (76), OPS-TEST-001 (72) |
| medium (draft 유지) | 2 | FR-TEST-005 (68), OPS-REL-001 (62) |
| low / blocked | 0 | — |

**Target 종합 판정**: `conditionally-ready` — v1.0.0 의 검증 테스트 인프라(IT/CI) 가 evolving 으로 진입 가능. E2E plan 작성 후 작업 진행 권고.

## 3. Stability 변경 결과 (실 적용 4건)

| REQ ID | Type | from | to | reason | applied |
|---|---|---|---|---|---|
| FR-TEST-003 | stability | draft | **evolving** | medium 76점, blockers 0, has_verification=true, 사용자 의도 일치 | ✅ |
| OPS-TEST-001 | stability | draft | **evolving** | medium 72점, blockers 0, GitHub Actions 즉시 가능 | ✅ |
| FR-TEST-002 | stability | evolving | **stable** | high 88점, 41 test 통과 + VE 6건, §0.G5 사용자 승인 | ✅ |
| FR-TEST-002 | status | in_progress | **implemented** | DelegationContractTest 330 LOC + Servlet5/6/61ContractTest 41 통과 | ✅ |

**dryRun 사전 검증**: 3건 모두 transition guard PASS (warnings 0). 실 적용 직후 `summarize_target` 으로 sync 확인 — countsByStability `evolving 40→41 / draft 4→2 / stable 0→1`, countsByStatus `in_progress 3→2 / implemented 1→2`.

## 4. Status 충돌 REQ — 없음

본 run 에서 feasibility=blocked + status ∈ {in_progress, implemented, verified} 충돌 0건.

## 5. guard 거부 / 사용자 거부 항목 — 없음

## 6. draft 유지 결정 (의도된 보류)

| REQ ID | 사유 | 재평가 시점 |
|---|---|---|
| FR-TEST-005 cross-version | depends_on FR-TEST-003 + OPS-TEST-001. 본 두 건 완료 후 자동 재평가. has_verification=false | FR-TEST-003 + OPS-TEST-001 implemented 시점 |
| OPS-REL-001 Maven Central 자격 | 외부 의존 (OQ-003 / GitHub Issue #3 Sonatype + GPG + Secrets). 사용자 의도 "검증 테스트" 와 직접 관련 없음 | 외부 트랙 완료 시점 |

## 7. 사용자 결정 사항 SSOT

1. **FR-TEST-003 evolving 승급** — A) Evolving 승급 (권고) ✅
2. **OPS-TEST-001 evolving 승급** — A) Evolving 승급 (권고) ✅
3. **FR-TEST-002 stable + implemented 승급** — A) implemented + stable 승급 ✅
4. **기타 (FR-TEST-005 / OPS-REL-001 / NFR-TEST-001)** — A) 권고대로 진행 ✅

NFR-TEST-001 SRS-코드 정합성 갭 (build.gradle v1.0.0 relaxed 60%/50% vs SRS ≥95%/100%) 은 본 run 에 mutation 없음. 다음 kiwi-srs-sync 또는 kiwi-srs 호출 시 Change Notes 추가 권고 (3지선다: Change Notes / NFR 분리 / build 맞춤).

## 8. 다음 단계 — kiwi-planner E2E plan 입력 권고

### 7 phase / 약 35 task

| Phase | Task 수 | scope_summary | depends_on |
|---|---|---|---|
| **PH-IT-INFRA** | 4 | servlet5/6/61 build.gradle 에 tomcat-embed-core 10.0.27/10.1.30/11.0.0 + awaitility 4.2.1 testImplementation. integrationTest task 분리 (@Tag("integration")). TomcatRunner 헬퍼 3 모듈 작성. | FR-MOD-001 (완료) |
| **PH-IT-SCENARIO-P0** | 10 | IT01 GET, IT02 POST F1, IT03 Cookie, IT04 SameSite jakarta→javax, IT05 Session, IT06 Filter chain, IT07 AsyncContext, IT08 MultiPart F2, IT09 sendRedirect(308) 6.1 only, IT10 Charset UTF-8 F3 | PH-IT-INFRA |
| **PH-IT-SCENARIO-P1** | 5 | IT11 setStatus 폴백, IT12 setContentLengthLong F5/F6, IT13 ErrorPage, IT14 getServletConnection 6.0 신규, IT15 WebSocket Upgrade | PH-IT-SCENARIO-P0 |
| **PH-CROSSVER** | 3 | common/src/testFixtures 에 shared IT01/03/05 fixture. servlet5/6/61 동일 fixture 실행 + 결과 비교. CI 매트릭스 통합 보고서 도구 결정 | PH-IT-SCENARIO-P0, PH-CI |
| **PH-CI** | 4 | .github/workflows/ci.yml 작성 (build-matrix OS×servlet-line, integration-tests Linux only, coverage). Gradle Toolchain (JDK 8/11/17). actions/cache@v4 + actions/setup-java@v4 | FR-MOD-001, FR-TEST-001, FR-TEST-003 |
| **PH-UNIT-BACKFILL** | 8 | servlet6 adapter unit 0→43, servlet61 0→43, servlet5 잔여 33→42. BugMuseumTest VE 표 보강 (FR-TEST-004). JaCoCo 게이트 v1.0.0 relaxed vs SRS 정합성 결정 | FR-TEST-001 |
| PH-MUTATION (옵션, v1.1.0) | 1 | OPS-TEST-002 PIT mutation testing prep | NFR-TEST-001 tightened |

**critical path**: FR-TEST-001 backfill → FR-TEST-003 IT → OPS-TEST-001 CI → FR-TEST-005 cross-version → FR-TEST-004 VE → NFR-TEST-001 게이트

## 9. v1.0.0 출시 조건 재평가

| 조건 | 상태 | 결정 |
|---|---|---|
| 단위 테스트 167 PASS + Contract 41 PASS | ✅ | 완료 |
| Tomcat embed IT01~IT15 (FR-TEST-003) | 🔄 evolving | E2E plan 작성 후 작업 |
| CI matrix (OPS-TEST-001) | 🔄 evolving | E2E plan 작성 후 작업 |
| cross-version 동등성 (FR-TEST-005) | ⏸️ draft 유지 | FR-TEST-003 완료 후 |
| Maven Central publish (OPS-REL-001) | ⏸️ draft 유지 | 사용자 직접 처리 결정 |
| 어댑터 단위 테스트 servlet6/61 0→43 (FR-TEST-001) | 🔄 evolving | PH-UNIT-BACKFILL 진행 |

**v1.0.0 출시 전 권고 작업**: PH-IT-INFRA + PH-IT-SCENARIO-P0 + PH-CI + PH-UNIT-BACKFILL (servlet6/61 critical). 약 22-25 task.

## 10. 산출물 위치

| 산출물 | 경로 |
|---|---|
| 본 보고서 (SSOT) | `docs/analysis/kiwi-srs-feasibility-2026-05-20.jakarta-adapter.v100.v02/report.md` |
| SRS mutation 결과 | speckiwi MCP 적용 완료 (4건 written: true) |
| Phase 1+2+4 통합 평가 | Agent 1회 (Opus) — 위 §2/§3/§8 데이터 SSOT |
| 정책 파일 | 부재 → §0.G6 기본 매핑 + 사용자 결정 보완 |

## 11. 다음 명령 안내

```bash
# E2E plan 작성
/kiwi-planner --target=v1.0.0 --tdd-policy=relaxed

# 작성된 plan 으로 PM 진행
/kiwi-pm PLAN_PATH=docs/plans/{new-e2e-plan}.plan.md
```

향후 NFR-TEST-001 게이트 정합성 결정 (Change Notes / NFR 분리 / build 맞춤) 은 kiwi-srs-sync 또는 kiwi-srs 호출로 별도 처리. OPS-REL-001 은 사용자 외부 트랙 완료 후 재평가.
