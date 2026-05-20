#!/usr/bin/env node
// Build plan.md from v03 sidecar
const fs = require('fs');
const crypto = require('crypto');

const ROOT = 'C:/Work/git/_Snoworca/Jakarta_To_Javax_Servlet_Adapter';
const RUN_ID = '2026-05-19-jakarta-adapter-v1-0-0-v03';
const sc = JSON.parse(fs.readFileSync(`${ROOT}/docs/plans/${RUN_ID}.sidecar.json`, 'utf8'));
const inventory = JSON.parse(fs.readFileSync(`${ROOT}/docs/analysis/kiwi-planner-${RUN_ID}/inventory.json`, 'utf8'));

function canonReqs(arr) {
  return [...new Set(arr.map(s=>s.trim()))].sort().join(',');
}
function canonFiles(arr) {
  return [...new Set(arr.map(f => f.line_range ? `${f.path}:${f.line_range}` : f.path))].sort().join(',');
}

const lines = [];
// frontmatter
lines.push('---');
lines.push(`run_id: ${RUN_ID}`);
lines.push(`target: ${sc.target}`);
lines.push(`plan_version: ${sc.plan_version}`);
lines.push(`plan_contract: "${sc.plan_contract}"`);
lines.push(`generated_at: ${sc.generated_at}`);
lines.push('tool_versions:');
lines.push(`  speckiwi: ${sc.tool_versions.speckiwi}`);
lines.push(`  kiwi_planner: ${sc.tool_versions.kiwi_planner}`);
lines.push(`  validator: ${sc.tool_versions.validator}`);
lines.push('stability_summary:');
lines.push('  frozen: 0');
lines.push('  stable: 0');
lines.push(`  evolving: ${sc.coverage.length}`);
lines.push('  draft: 0');
lines.push(`tdd_policy: ${sc.tdd_policy}`);
lines.push(`sidecar_path: ./${RUN_ID}.sidecar.json`);
lines.push(`md_sha256: <computed-after-write>`);
lines.push('---');
lines.push('');
lines.push(`# v1.0.0 잔여 작업 구현 계획 (v03)`);
lines.push('');

// §1 개요
lines.push('## §1 개요');
lines.push('');
lines.push('### 1.1 목표');
lines.push('');
lines.push('Jakarta ↔ Javax Servlet Adapter v1.0.0 의 잔여 작업 (PH-003 잔여 결함 6건 페어 + PH-004 변환 의미론 + PH-005 테스트 인프라 + PH-006 CI + PH-007 Maven Central 게시) 을 v02 의 완료 20 task 위에 이어서 수행한다. 본 v03 은 v02 plan 의 53 잔여 task 를 입력으로 받아 `--draft-policy=exclude-draft` 정책으로 draft-only task 7건을 drop 한 46 task / 5 phase / 32 evolving REQ 의 정형 계획이다.');
lines.push('');
lines.push('### 1.2 범위 (in_scope[])');
lines.push('');
lines.push('- 5 phase: PH-003 (결함 6건 페어), PH-004 (변환 의미론), PH-005 (테스트 인프라), PH-006 (CI 매트릭스), PH-007 (Maven Central publish)');
lines.push('- 46 task: code 38건 (TDD red/green 페어 18건 + green-only 2건 + exempt 2건), review 3건, doc 2건, infra 2건, pr 1건');
lines.push('- 32 evolving REQ (FR-FIX-001~009, FR-CONV-001~014, FR-MOD-007, FR-REL-001, FR-TEST-001/002/004, NFR-CONV-001, NFR-TEST-001, IR-REL-001, MIG-REL-001)');
lines.push('');
lines.push('### 1.3 제외사항 (out_of_scope[], excluded_reqs 포함)');
lines.push('');
lines.push('- **PH-001 + PH-002 + PH-003 (T-PH003-01~08)** — v02 에서 이미 완료 (20 task). v03 은 잔여 phase 만 다룬다.');
lines.push('- **4 draft REQ** (`--draft-policy=exclude-draft`):');
lines.push('  - FR-TEST-003 (jqwik property-based testing, v1.1.0 이연)');
lines.push('  - FR-TEST-005 (fuzz testing, v1.1.0 이연)');
lines.push('  - OPS-TEST-001 (PIT mutation testing per D6, v1.1.0 이연)');
lines.push('  - OPS-REL-001 (Sonatype/GPG/Secrets 외부 인프라, v1.1.0 이연)');
lines.push('- **2 draft REQ carry-over** (v02 sidecar 인계):');
lines.push('  - NFR-MOD-002 (JPMS module-info.java, v1.1.0 per OQ-002)');
lines.push('  - OPS-TEST-002 (추가 mutation testing 항목)');
lines.push('- **7 draft-only task drop**: T-PH005-03, T-PH005-04, T-PH005-05, T-PH006-01, T-PH006-02, T-PH007-05, T-PH007-06');
lines.push('');
lines.push('### 1.4 전제조건 / 가정');
lines.push('');
lines.push('- v02 의 20 completed task 가 git working tree 에 반영되어 있음 (BugMuseumTest 15 case, 3 모듈 토폴로지, FR-FIX-001/002/003 green 완료, 4 jar Automatic-Module-Name + archivesName 적용)');
lines.push('- speckiwi MCP v2.2.2 가용, active_target=v1.0.0');
lines.push('- kiwi-coder state 재개 시 v03 신규 session 으로 시작 (run-id 변경) — v02 state.json 의 completed_task_ids 는 본 계획에서 in_scope 가 아님');
lines.push('- gradle 8.10.2 + Microsoft JDK 8/11/17 toolchain 셋업 완료');
lines.push('');

// §2 Phase 목록
lines.push('## §2 Phase 목록');
lines.push('');
lines.push('| phase_id | title | goal | depends_on | task_count |');
lines.push('|---|---|---|---|---|');
for (const p of sc.phases) {
  const dep = p.depends_on.length ? p.depends_on.join(',') : '-';
  lines.push(`| ${p.id} | ${p.title} | ${p.goal||'-'} | ${dep} | ${p.task_ids.length} |`);
}
lines.push('');

// §3 Task 상세
lines.push('## §3 Task 상세');
lines.push('');
for (const p of sc.phases) {
  lines.push(`### §3.${p.id} ${p.title}`);
  lines.push('');
  lines.push(`**Goal**: ${p.goal||'-'}  `);
  lines.push(`**Depends on**: ${p.depends_on.length?p.depends_on.join(', '):'-'}  `);
  lines.push(`**Task count**: ${p.task_ids.length}`);
  lines.push('');
  for (const tid of p.task_ids) {
    const t = sc.tasks.find(x => x.id === tid);
    lines.push(`#### §3.${p.id}.${tid} ${t.title}`);
    lines.push('');
    lines.push(`- id: ${t.id}`);
    lines.push(`- phase_id: ${t.phase_id}`);
    lines.push(`- title: ${t.title}`);
    lines.push(`- type: ${t.type}`);
    lines.push(`- req_ids: [${canonReqs(t.req_ids)}]`);
    lines.push(`- files: [${canonFiles(t.files||[])}]`);
    lines.push(`- action: ${t.action.replace(/\n/g,' ')}`);
    if (t.covers_ac && t.covers_ac.length) lines.push(`- covers_ac: [${t.covers_ac.join(', ')}]`);
    // acceptance_tests inline
    const atSummary = (t.acceptance_tests||[]).map(at => {
      if (at.kind === 'shell') return `{shell: cmd="${at.cmd}", expected_exit=${at.expected_exit}${at.stdout_regex?', stdout_regex="'+at.stdout_regex+'"':''}}`;
      if (at.kind === 'checklist') return `{checklist: ${(at.items||[]).length} items}`;
      if (at.kind === 'file_state') return `{file_state: ${at.path} exists=${at.exists}}`;
      if (at.kind === 'http') return `{http: ${at.method} ${at.url} -> ${at.expected_status}}`;
      if (at.kind === 'perf') return `{perf: ${at.metric} ${at.threshold} via ${at.tool}}`;
      return JSON.stringify(at);
    }).join(' | ');
    lines.push(`- acceptance_tests: ${atSummary || '[]'}`);
    if (t.verification_cmd) {
      lines.push(`- verification_cmd: {posix: "${t.verification_cmd.posix}", windows: "${t.verification_cmd.windows}"}`);
    } else {
      lines.push(`- verification_cmd: null`);
    }
    lines.push(`- dod:`);
    for (const d of (t.dod||[])) lines.push(`  - ${d}`);
    lines.push(`- rollback: ${(t.rollback||'').replace(/\n/g,' ')}`);
    lines.push(`- estimated_effort: ${t.estimated_effort||'M'}`);
    if (t.depends_on_task && t.depends_on_task.length) lines.push(`- depends_on_task: [${t.depends_on_task.join(', ')}]`);
    // TDD inline
    if (t.tdd) {
      const tcCount = (t.tdd.test_cases||[]).length;
      const tddInline = t.tdd.applicable
        ? `{applicable: true, phase: "${t.tdd.phase}", test_cases_count: ${tcCount}}`
        : `{applicable: false, phase: "n/a", exempt_reason: "${(t.tdd.exempt_reason||'auto-exempt by task type').replace(/"/g,'\\"').slice(0,200)}"}`;
      lines.push(`- tdd: ${tddInline}`);
    }
    lines.push(`- evaluation_topology: ${t.evaluation_topology}`);
    lines.push('');
  }
}

// §4 REQ ↔ Task 역색인
lines.push('## §4 REQ ↔ Task 역색인');
lines.push('');
lines.push('| req_id | stability | task_ids | ac_covered/ac_total |');
lines.push('|---|---|---|---|');
for (const c of sc.coverage) {
  lines.push(`| ${c.req_id} | ${c.stability} | ${c.covered_tasks.join(', ')} | ${c.ac_covered}/${c.ac_total} |`);
}
lines.push('');

// §5 위험 · 미해결
lines.push('## §5 위험 · 미해결');
lines.push('');
lines.push('### 5.1 위험');
lines.push('');
if (sc.risks.length === 0) lines.push('(none)');
else {
  lines.push('| risk_id | severity | description | mitigation | affected_task_ids |');
  lines.push('|---|---|---|---|---|');
  for (const r of sc.risks) {
    lines.push(`| ${r.id} | ${r.severity} | ${(r.description||'').replace(/\|/g,'\\|').slice(0,200)} | ${(r.mitigation||'').replace(/\|/g,'\\|').slice(0,200)} | ${(r.affected_task_ids||[]).join(', ')} |`);
  }
}
lines.push('');

lines.push('### 5.2 Open Questions');
lines.push('');
if (sc.open_questions.length === 0) lines.push('(none)');
else {
  lines.push('| id | question | resolve_at_phase | origin_priority |');
  lines.push('|---|---|---|---|');
  for (const oq of sc.open_questions) {
    lines.push(`| ${oq.id} | ${oq.question.replace(/\|/g,'\\|').slice(0,200)} | ${oq.resolve_at_phase||'-'} | ${oq.origin_priority||'-'} |`);
  }
}
lines.push('');

lines.push('### 5.3 unreferenced_reqs');
lines.push('');
if (sc.unreferenced_reqs.length === 0) lines.push('(none — all 32 in-scope REQ are mapped to ≥1 task)');
else {
  for (const u of sc.unreferenced_reqs) lines.push(`- ${u.req_id} (${u.stability}): ${u.reason}`);
}
lines.push('');

lines.push('### 5.4 deferred_ac');
lines.push('');
if (sc.deferred_ac.length === 0) lines.push('(none)');
else {
  lines.push('| req_id | ac_id | reason | user_decision_id |');
  lines.push('|---|---|---|---|');
  for (const d of sc.deferred_ac) {
    lines.push(`| ${d.req_id} | ${d.ac_id} | ${(d.reason||'').replace(/\|/g,'\\|').slice(0,200)} | ${d.user_decision_id} |`);
  }
}
lines.push('');

lines.push('### 5.5 TDD 결정 (accept-as-exempt / add-test-task)');
lines.push('');
if (sc.tdd_decisions.length === 0) lines.push('(none)');
else {
  lines.push('| task_id | decision | reason | user_decision_id |');
  lines.push('|---|---|---|---|');
  for (const td of sc.tdd_decisions) {
    lines.push(`| ${td.task_id} | ${td.decision} | ${(td.reason||'').replace(/\|/g,'\\|').slice(0,200)} | ${td.user_decision_id} |`);
  }
}
lines.push('');

// §6 부록
lines.push('## §6 부록');
lines.push('');
lines.push('### 6.1 사이드카 JSON 경로 / md_sha256');
lines.push('');
lines.push(`- sidecar: \`docs/plans/${RUN_ID}.sidecar.json\``);
lines.push(`- md_sha256: \`<computed-after-write>\``);
lines.push('');

lines.push('### 6.2 검증 스크립트 실행');
lines.push('');
lines.push('```bash');
lines.push(`node ~/.claude/skills/kiwi-planner/validator.mjs \\`);
lines.push(`  docs/plans/${RUN_ID}.plan.md \\`);
lines.push(`  docs/plans/${RUN_ID}.sidecar.json \\`);
lines.push(`  --target v1.0.0 \\`);
lines.push(`  --inventory-file docs/analysis/kiwi-planner-${RUN_ID}/inventory.json \\`);
lines.push(`  --out docs/plans/${RUN_ID}.validator.json`);
lines.push('```');
lines.push('');

lines.push('### 6.3 mcp_call_log 요약');
lines.push('');
lines.push('- 호출 수 (계획 단계): 0');
lines.push(`- mutation 예정 (Phase 5):`);
lines.push(`  - 단계 1: add_trace_link × 46 task (각 task 의 trace_links[] 합산 multiset)`);
lines.push(`  - 단계 2: add_verification_evidence × 32 REQ (coverage[].req_id 마다 1건)`);
lines.push('');

// §7 v03 추가 — evaluation_topology 안내
lines.push('## §7 evaluation_topology 메타 (kiwi-coder 호출 토폴로지 힌트)');
lines.push('');
lines.push('각 task 의 `evaluation_topology` 필드는 kiwi-coder 가 호출 시 적용할 검증 깊이를 시그널한다 (validator 는 unknown field 허용).');
lines.push('');
lines.push('| 값 | 적용 task | 호출 토폴로지 | 호출 수/페어 |');
lines.push('|---|---|---|---|');
lines.push('| `simplified` | PH-003 잔여 6 페어 (FR-FIX-004~009 red+green) = 12 task | 시니어 + S4 only + green 시니어 + 정형 Sonnet + 까칠 skip | 5 |');
lines.push('| `full` | PH-004 변환 의미론 페어 22 task + (T-PH004-23 review 1) | 시니어 + S1~S4 + green 시니어 + 정형 Sonnet + 까칠 Opus | 8 |');
lines.push('| `file_op` | review/doc/file_op/pr/issue/TDD-exempt code = 8 task (T-PH003-21, T-PH004-23, T-PH005-01/02, T-PH007-01/02/03/04, T-PH005-07, T-PH007-07) | 시니어 + 정형 sonnet 검증만 | 2 |');
lines.push('| `infra` | T-PH005-06, T-PH006-03 (CI / JaCoCo / cross-JDK matrix) | 시니어 + sonnet 검증 + shell 직접 실행 검증 | 3 |');
lines.push('');

// Write file
const PLAN_OUT = `${ROOT}/docs/plans/${RUN_ID}.plan.md`;
const content = lines.join('\n') + '\n';
fs.writeFileSync(PLAN_OUT, content);

// Compute md_sha256 + patch frontmatter
const sha = crypto.createHash('sha256').update(content, 'utf8').digest('hex');
const patched = content.replace('md_sha256: <computed-after-write>', `md_sha256: "${sha}"`).replace('- md_sha256: `<computed-after-write>`', `- md_sha256: \`${sha}\``);
fs.writeFileSync(PLAN_OUT, patched);

// Patch sidecar md_sha256
const sc2 = JSON.parse(fs.readFileSync(`${ROOT}/docs/plans/${RUN_ID}.sidecar.json`, 'utf8'));
sc2.md_sha256 = sha;
fs.writeFileSync(`${ROOT}/docs/plans/${RUN_ID}.sidecar.json`, JSON.stringify(sc2, null, 2));

console.log(`plan.md written: ${PLAN_OUT}`);
console.log(`md_sha256: ${sha}`);
console.log(`Lines: ${patched.split('\n').length}`);
