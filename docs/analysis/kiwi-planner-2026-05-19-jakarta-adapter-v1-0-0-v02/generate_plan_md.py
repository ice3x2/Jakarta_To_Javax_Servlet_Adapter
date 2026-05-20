#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""Generate v02 plan.md from sidecar."""
import json
import hashlib
from pathlib import Path

ROOT = Path(r"C:\Work\git\_Snoworca\Jakarta_To_Javax_Servlet_Adapter")
SIDECAR = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json"
OUTPUT = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.plan.md"

with open(SIDECAR, "r", encoding="utf-8") as f:
    sc = json.load(f)

phases = sc["phases"]
tasks = sc["tasks"]
coverage = sc["coverage"]
risks = sc["risks"]
oqs = sc["open_questions"]
excluded = sc["excluded_reqs"]

# Quick lookup
INV_STABILITY = {c["req_id"]: c["stability"] for c in coverage}
INV_AC_TOTAL = {c["req_id"]: c["ac_total"] for c in coverage}
INV_AC_COVERED = {c["req_id"]: c["ac_covered"] for c in coverage}
TASK_BY_REQ = {}
for t in tasks:
    for r in t["req_ids"]:
        TASK_BY_REQ.setdefault(r, []).append(t["id"])


def render_files(files):
    return "[" + ", ".join(f["path"] for f in files) + "]"


def render_acceptance(ats):
    """Render acceptance_tests as JSON-inline string."""
    return json.dumps(ats, ensure_ascii=False, separators=(", ", ": "))


def render_verification(vc):
    if vc is None:
        return "null"
    return json.dumps(vc, ensure_ascii=False, separators=(", ", ": "))


def render_dod(dod):
    return "\n  - " + "\n  - ".join(dod)


def render_tdd_inline(t):
    tdd = t.get("tdd", {})
    if not tdd:
        return "n/a"
    applicable = tdd.get("applicable", False)
    phase = tdd.get("phase", "n/a")
    tcc = tdd.get("test_cases_count", 0)
    exempt = tdd.get("exempt_reason")
    summary = f"{{applicable: {str(applicable).lower()}, phase: {phase}, test_cases_count: {tcc}"
    if exempt:
        summary += f", exempt_reason: \"{exempt[:60]}...\""
    summary += "}"
    return summary


lines = []
# --- frontmatter ---
lines.append("---")
lines.append("run_id: 2026-05-19-jakarta-adapter-v1-0-0-v02")
lines.append("target: v1.0.0")
lines.append("plan_version: 0.1.0")
lines.append('plan_contract: "1.2.0"')
lines.append("generated_at: 2026-05-19T10:00:00Z")
lines.append("tool_versions:")
lines.append("  speckiwi: 2.2.2")
lines.append("  kiwi_planner: 0.6.0")
lines.append("  validator: 0.6.0")
lines.append("stability_summary:")
lines.append("  frozen: 0")
lines.append("  stable: 0")
lines.append("  evolving: 40")
lines.append("  draft: 4")
lines.append("tdd_policy: relaxed")
lines.append("sidecar_path: ./2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json")
lines.append("md_sha256: TBD")
lines.append("---")
lines.append("")

# --- §1 개요 ---
lines.append("## §1 개요")
lines.append("")
lines.append("### 1.1 목표")
lines.append("")
lines.append("`Jakarta_To_Javax_Servlet_Adapter` 0.0.1 단일 jar 를 `common / servlet5 / servlet6 / servlet61 / bom` 5-모듈 Gradle 토폴로지로 재구조화하고, 0.0.1 Critical 결함 9건을 TDD red-green 페어 분해 방식으로 일괄 수정하며, Tomcat embed 기반 통합 테스트 슈트를 도입하여 Maven Central 에 v1.0.0 으로 게시한다. v02 는 plan_contract 1.2.0 + schema_version 1.1.0 + tdd_policy=relaxed 로 작성되며, PH-003 (10 페어 = 20 task) + PH-004 (11 페어 = 22 task) 의 모든 code task 에 대해 AC 페어 분해를 적용한다.")
lines.append("")
lines.append("### 1.2 범위 (in_scope)")
lines.append("")
lines.append("- 5개 Gradle 서브 모듈 신설 (`common`, `servlet5`, `servlet6`, `servlet61`, `bom`) — FR-MOD-001~007 + CON-MOD-001 + NFR-MOD-001")
lines.append("- 0.0.1 코드의 9개 Critical 결함 수정 — FR-FIX-001~009, 각 결함은 TDD red (test 선행 작성 + 실패 확인) → green (impl 수정 + 통과) 페어로 처리")
lines.append("- 변환 의미론 SPEC-1~16 + 폴백 정책 + strict 모드 + AdapterUnwrap 도입 — FR-CONV-001~014 + NFR-CONV-001, 모든 코드 task TDD 페어 분해")
lines.append("- 5단계 테스트 피라미드 + Bug Museum BM01~BM10 + Tomcat embed IT01~IT15 — FR-TEST-001~005 + NFR-TEST-001")
lines.append("- GitHub Actions CI 매트릭스 + JaCoCo 게이트 — OPS-TEST-001")
lines.append("- Maven 좌표 + README 호환성 매트릭스 + 0.x 마이그레이션 (D8 옵션 B = servlet5 내 deprecated alias) + Sonatype/GPG — IR-REL-001 + FR-REL-001 + MIG-REL-001 + OPS-REL-001")
lines.append("")
lines.append("### 1.3 제외사항 (out_of_scope)")
lines.append("")
lines.append("excluded_reqs:")
for x in excluded:
    lines.append(f"- **{x['req_id']}** — {x['reason']}")
lines.append("")
lines.append("기타:")
lines.append("- v1.1.0 nightly cron + release-tag PIT 도입은 별도 plan 으로 분리한다.")
lines.append("- 0.x → 1.0 마이그레이션 alias 정책은 D8 에서 옵션 B (servlet5 모듈 내 deprecated alias 패키지) 로 확정되어 OQ-001 은 closed.")
lines.append("")
lines.append("### 1.4 전제조건 / 가정")
lines.append("")
lines.append("- `com.clipsoft` group 은 build.gradle 의 현 값을 그대로 유지한다.")
lines.append("- `javax.servlet:javax.servlet-api` 의존은 4.0.1 로 고정한다.")
lines.append("- Gradle Java Toolchain 으로 JDK 8 / 11 / 17 을 자동 다운로드한다.")
lines.append("- Mockito 5.6.0 + JUnit Jupiter 5.9.2 호환 (현 build.gradle 와 정합).")
lines.append("- 통합 테스트는 Linux 만 실행한다 (Windows 는 단위 + contract).")
lines.append("- 외부 의존: Sonatype 계정 / GPG 키 / `com.clipsoft` namespace 점유 — OQ-003 으로 외부 트래킹.")
lines.append("- TDD 페어 분해는 PH-003 + PH-004 의 모든 code task 에만 적용. 비코딩 task 와 PH-002/PH-005/PH-007 의 빌드/인프라/릴리즈 task 는 tdd.applicable=false (자동 면제 또는 exempt_reason 명시).")
lines.append("")

# --- §2 Phase 목록 ---
lines.append("## §2 Phase 목록")
lines.append("")
lines.append("| phase_id | title | goal | depends_on | task_count |")
lines.append("|---|---|---|---|---|")
for ph in phases:
    deps = ", ".join(ph["depends_on"]) if ph["depends_on"] else "—"
    lines.append(f"| {ph['id']} | {ph['title']} | {ph['goal']} | {deps} | {len(ph['task_ids'])} |")
lines.append("")

# --- §3 Task 상세 ---
lines.append("## §3 Task 상세")
lines.append("")
for ph in phases:
    lines.append(f"### §3.{ph['id']} {ph['title']}")
    lines.append("")
    ph_tasks = [t for t in tasks if t["phase_id"] == ph["id"]]
    for t in ph_tasks:
        lines.append(f"#### §3.{ph['id']}.{t['id']} {t['title']}")
        lines.append("")
        lines.append(f"- id: {t['id']}")
        lines.append(f"- phase_id: {t['phase_id']}")
        lines.append(f"- title: {t['title']}")
        lines.append(f"- type: {t['type']}")
        lines.append(f"- req_ids: [{', '.join(t['req_ids'])}]")
        lines.append(f"- files: {render_files(t['files'])}")
        if t.get("test_files"):
            lines.append(f"- test_files: {render_files(t['test_files'])}")
        lines.append(f"- action: {t['action']}")
        if t.get("covers_ac"):
            lines.append(f"- covers_ac: [{', '.join(t['covers_ac'])}]")
        if t.get("depends_on_task"):
            lines.append(f"- depends_on_task: [{', '.join(t['depends_on_task'])}]")
        lines.append(f"- acceptance_tests: {render_acceptance(t['acceptance_tests'])}")
        lines.append(f"- verification_cmd: {render_verification(t['verification_cmd'])}")
        lines.append(f"- dod:{render_dod(t['dod'])}")
        lines.append(f"- rollback: {t['rollback']}")
        lines.append(f"- estimated_effort: {t['estimated_effort']}")
        lines.append(f"- tdd: {render_tdd_inline(t)}")
        lines.append("")

# --- §4 REQ ↔ Task ---
lines.append("## §4 REQ ↔ Task 역색인")
lines.append("")
lines.append("| req_id | stability | task_ids | ac_covered/ac_total |")
lines.append("|---|---|---|---|")
for c in coverage:
    tids = ", ".join(c["covered_tasks"])
    lines.append(f"| {c['req_id']} | {c['stability'] or 'unset'} | {tids} | {c['ac_covered']}/{c['ac_total']} |")
lines.append("")

# --- §5 위험·미해결 ---
lines.append("## §5 위험 · 미해결")
lines.append("")
lines.append("### 5.1 위험")
lines.append("")
lines.append("| risk_id | severity | mitigation | affected_task_ids |")
lines.append("|---|---|---|---|")
for r in risks:
    aff = ", ".join(r["affected_task_ids"][:8]) + (", ..." if len(r["affected_task_ids"]) > 8 else "")
    lines.append(f"| {r['risk_id']} | {r['severity']} | {r['mitigation']} | {aff} |")
lines.append("")
lines.append("### 5.2 Open Questions")
lines.append("")
lines.append("| id | question | blocks_task_ids |")
lines.append("|---|---|---|")
for oq in oqs:
    bt = ", ".join(oq["blocks_task_ids"]) if oq["blocks_task_ids"] else "(none — deferred 확인만)"
    lines.append(f"| {oq['id']} | {oq['question']} | {bt} |")
lines.append("")
lines.append("### 5.3 unreferenced_reqs")
lines.append("")
lines.append("(비어있음 — 44 REQ 모두 cover)")
lines.append("")
lines.append("### 5.4 deferred_ac")
lines.append("")
lines.append("(비어있음 — 모든 AC 가 Task 커버리지에 포함됨. 153/153 AC 전수 cover.)")
lines.append("")
lines.append("### 5.5 TDD 결정 (§0.G7 — v02 는 자동 면제 적용, 사용자 결정 0건)")
lines.append("")
lines.append("v02 의 모든 TDD 면제는 자동 면제 규칙(§0.G7 + §0.G8)으로 적용되었으며 AskUserQuestion 미발동:")
lines.append("")
lines.append("- 비코딩 task (issue/review/doc/file_op/pr) — type 기반 auto-exempt (validator C22)")
lines.append("- PH-002 의 4건 Gradle build code task — exempt_reason=\"" + "Gradle build script + settings authoring; build correctness validated by `./gradlew build` exit codes and module graph assertions, not unit-level TDD red-green cycles.\"")
lines.append("- PH-005 의 4건 IT/fixture code task — exempt_reason=\"Integration test harness/fixture authoring; the test fixtures themselves constitute the TDD acceptance for FR-FIX/FR-CONV implementation phases. Meta-TDD on test infrastructure is impractical and not value-adding.\"")
lines.append("- PH-007 의 2건 release artifact code task — exempt_reason=\"Release artifact authoring (CHANGELOG/README badges/version bump); validated by file_state checks and Maven Central staging verification, not behavior tests.\"")
lines.append("- PH-005/PH-006/PH-007 의 6건 infra task — exempt_reason=\"CI matrix configuration; correctness validated by GHA workflow successful run, not unit tests.\"")
lines.append("")
lines.append("tdd_decisions 배열: 빈 배열 (사용자 결정 0건).")
lines.append("")

# --- §6 부록 ---
lines.append("## §6 부록")
lines.append("")
lines.append("### 6.1 사이드카 JSON 경로 / md_sha256")
lines.append("")
lines.append("- 사이드카 경로: `./2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json` (본 plan.md 와 동일 디렉토리)")
lines.append("- md_sha256: `TBD` (Phase 4 validator 가 자동 갱신)")
lines.append("")
lines.append("### 6.2 검증 스크립트 실행 방법")
lines.append("")
lines.append("- 빌드 전수: `./gradlew clean build` (5개 모듈)")
lines.append("- 단위 + contract: `./gradlew test` (전 모듈)")
lines.append("- 통합: `./gradlew integrationTest` (Linux only)")
lines.append("- 커버리지 게이트: `./gradlew jacocoTestCoverageVerification`")
lines.append("- Bug Museum 회귀: `./gradlew test --tests com.snoworca.regression.BugMuseumTest`")
lines.append("- Maven Local 게시 dry-run: `./gradlew publishToMavenLocal`")
lines.append("- 모듈 의존 그래프 검증: `./gradlew validateNoVersionModuleCrossDeps`")
lines.append("- Phase 4 validator (외부): `node tools/kiwi-planner/validator.mjs --plan docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.plan.md --sidecar docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json`")
lines.append("")
lines.append("### 6.3 mcp_call_log 요약")
lines.append("")
lines.append("본 Phase (Phase 2 plan drafting) 에서는 SRS mutation MCP 호출 금지 (§0.G1 황금률). `add_trace_link` / `add_verification_evidence` 는 Phase 5 (Mutation + Report) 에서만 실행. 본 plan 의 sidecar.json `mcp_call_log` 는 빈 배열로 초기화됨 (호출 0건).")
lines.append("")

# Write
content = "\n".join(lines)
with open(OUTPUT, "w", encoding="utf-8") as f:
    f.write(content)

# Compute sha256 of file contents
sha = hashlib.sha256(content.encode("utf-8")).hexdigest()
print(f"plan.md written: {len(lines)} lines, sha256={sha}")
