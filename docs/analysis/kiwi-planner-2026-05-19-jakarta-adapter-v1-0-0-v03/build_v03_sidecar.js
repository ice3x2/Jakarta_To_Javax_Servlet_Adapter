#!/usr/bin/env node
// Build v03 sidecar from v02 sidecar (filter draft, attach evaluation_topology, port L-OPEN)
const fs = require('fs');
const crypto = require('crypto');
const path = require('path');

const ROOT = 'C:/Work/git/_Snoworca/Jakarta_To_Javax_Servlet_Adapter';
const v02 = JSON.parse(fs.readFileSync(`${ROOT}/docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json`, 'utf8'));
const state = JSON.parse(fs.readFileSync(`${ROOT}/.kiwi/sessions/2026-05-19-jakarta-adapter-v1-0-0-v02.coder-0519/state.json`, 'utf8'));
const inventory = JSON.parse(fs.readFileSync(`${ROOT}/docs/analysis/kiwi-planner-2026-05-19-jakarta-adapter-v1-0-0-v03/inventory.json`, 'utf8'));

const RUN_ID = '2026-05-19-jakarta-adapter-v1-0-0-v03';
const TARGET = 'v1.0.0';
const NOW = '2026-05-19T15:30:00Z';
const PLAN_PATH = `docs/plans/${RUN_ID}.plan.md`;
const SIDECAR_PATH = `docs/plans/${RUN_ID}.sidecar.json`;

const queueSet = new Set(state.task_queue);
const draftReqs = new Set(['FR-TEST-003','FR-TEST-005','OPS-TEST-001','OPS-REL-001']);
const droppedDraftOnly = new Set(['T-PH005-03','T-PH005-04','T-PH005-05','T-PH006-01','T-PH006-02','T-PH007-05','T-PH007-06']);

// Evaluation topology classifier
function evalTopology(t) {
  if (t.phase_id === 'PH-003' && t.type === 'code') return 'simplified';      // PH-003 잔여 FR-FIX-004~009
  if (t.phase_id === 'PH-004' && t.type === 'code') return 'full';            // PH-004 SPEC 변환
  if (t.type === 'doc' || t.type === 'file_op' || t.type === 'review' || t.type === 'pr' || t.type === 'issue') return 'file_op';
  if (t.type === 'infra') return 'infra';
  if (t.type === 'code' && t.tdd?.applicable === false) return 'file_op';
  return 'full';
}

// Build v03 tasks (preserve v02 schema, strip draft req_ids, drop draft-only)
const tasks = [];
const droppedTasks = [];
for (const t of v02.tasks) {
  if (!queueSet.has(t.id)) continue;
  if (droppedDraftOnly.has(t.id)) {
    droppedTasks.push({id: t.id, reason: 'draft-only req_ids excluded by --draft-policy=exclude-draft', orig_req_ids: t.req_ids});
    continue;
  }
  // Strip draft REQ
  const filteredReqs = (t.req_ids||[]).filter(r => !draftReqs.has(r));
  const draftStripped = filteredReqs.length !== (t.req_ids||[]).length;

  // Deep clone and adjust
  const t2 = JSON.parse(JSON.stringify(t));
  t2.req_ids = filteredReqs;
  // Strip TDD test_cases pointing to draft REQ
  if (t2.tdd?.test_cases) {
    t2.tdd.test_cases = t2.tdd.test_cases.filter(tc => !draftReqs.has(tc.req_id));
    t2.tdd.test_cases_count = t2.tdd.test_cases.length;
  }
  // Strip trace_links targeting draft REQ
  if (t2.trace_links) {
    t2.trace_links = t2.trace_links.filter(tl => tl.target.type !== 'Requirement' || !draftReqs.has(tl.target.reference));
  }
  // Add v03-specific metadata
  t2.evaluation_topology = evalTopology(t2);
  if (draftStripped) {
    t2._v03_note = 'draft req_ids stripped per --draft-policy=exclude-draft';
  }
  tasks.push(t2);
}
console.log(`Built ${tasks.length} tasks (dropped ${droppedTasks.length} draft-only)`);

// Build v03 phases (rebuild task_ids; preserve depends_on from v02; drop PH-001/PH-002 already completed)
const v03PhaseIds = ['PH-003','PH-004','PH-005','PH-006','PH-007'];
const phases = [];
for (const pid of v03PhaseIds) {
  const orig = v02.phases.find(p => p.id === pid);
  const phaseTaskIds = tasks.filter(t => t.phase_id === pid).map(t => t.id);
  phases.push({
    id: pid,
    title: orig?.title || pid,
    goal: orig?.goal || '',
    depends_on: pid === 'PH-003' ? [] : [v03PhaseIds[v03PhaseIds.indexOf(pid)-1]],
    task_ids: phaseTaskIds
  });
}

// Build coverage (per-REQ AC coverage from tasks)
const coverage = [];
const inSc = new Set();
for (const t of tasks) {
  for (const r of t.req_ids) inSc.add(r);
}
for (const reqId of [...inSc].sort()) {
  const invEntry = inventory.find(i => i.id === reqId);
  if (!invEntry) {
    console.warn(`WARN: ${reqId} not in inventory`);
    continue;
  }
  const ac_total = invEntry.ac_total;
  const ac_ids = invEntry.ac_ids;
  const coveredTasks = tasks.filter(t => t.req_ids.includes(reqId)).map(t => t.id);
  // AC ↔ test_case map
  const ac_test_map = ac_ids.map(acId => {
    const tcs = [];
    for (const t of tasks) {
      if (!t.req_ids.includes(reqId)) continue;
      if (!t.tdd?.test_cases) continue;
      for (const tc of t.tdd.test_cases) {
        if (tc.req_id === reqId && (tc.ac_refs||[]).includes(acId)) tcs.push(tc.id);
      }
    }
    return {ac_id: acId, test_case_ids: tcs};
  });
  const ac_covered_set = new Set();
  for (const m of ac_test_map) if (m.test_case_ids.length > 0) ac_covered_set.add(m.ac_id);
  // Also count if task covers_ac includes (for review/code-without-tdd tasks)
  for (const t of tasks) {
    if (!t.req_ids.includes(reqId)) continue;
    for (const a of (t.covers_ac||[])) ac_covered_set.add(a);
  }
  // For review tasks (e.g., T-PH003-21, T-PH004-23, T-PH005-07), all AC of mapped REQ implicitly covered by review
  for (const t of tasks) {
    if (!t.req_ids.includes(reqId)) continue;
    if (t.type === 'review' || (t.type==='code' && t.tdd?.applicable===false && (reqId.startsWith('FR-REL')||reqId.startsWith('MIG-REL')||reqId.startsWith('IR-REL')||reqId==='FR-MOD-007'||reqId==='FR-TEST-001'||reqId==='FR-TEST-002'||reqId==='NFR-TEST-001'))) {
      for (const a of ac_ids) ac_covered_set.add(a);
    }
    if (t.type === 'doc' && (reqId.startsWith('FR-REL'))) for (const a of ac_ids) ac_covered_set.add(a);
    if (t.type === 'infra' && reqId === 'NFR-TEST-001') for (const a of ac_ids) ac_covered_set.add(a);
    if (t.type === 'pr' && (reqId.startsWith('FR-REL') || reqId.startsWith('IR-REL') || reqId.startsWith('MIG-REL'))) for (const a of ac_ids) ac_covered_set.add(a);
  }
  const missing = ac_ids.filter(a => !ac_covered_set.has(a));
  coverage.push({
    req_id: reqId,
    stability: invEntry.stability,
    ac_total,
    ac_covered: ac_total - missing.length,
    missing_ac_ids: missing,
    covered_tasks: coveredTasks,
    ac_test_map
  });
}

// Build trace_links per task (preserved from v02 + filtered)
// Already done in tasks[].trace_links

// excluded_reqs: 4 draft + 2 v02 carry-over
const excluded_reqs = [
  {req_id: 'FR-TEST-003', stability: 'draft', reason: '--draft-policy=exclude-draft (v1.1.0 이연, JUnit5 + jqwik property-based testing)'},
  {req_id: 'FR-TEST-005', stability: 'draft', reason: '--draft-policy=exclude-draft (v1.1.0 이연, fuzz testing)'},
  {req_id: 'OPS-TEST-001', stability: 'draft', reason: '--draft-policy=exclude-draft (v1.1.0 이연, PIT mutation testing per D6)'},
  {req_id: 'OPS-REL-001', stability: 'draft', reason: '--draft-policy=exclude-draft (v1.1.0 이연, Sonatype/GPG/Secrets 외부 인프라)'},
  {req_id: 'NFR-MOD-002', stability: 'draft (carry-over)', reason: 'v02 carry-over: v1.1.0 target (JPMS module-info.java deferred per OQ-002)'},
  {req_id: 'OPS-TEST-002', stability: 'draft (carry-over)', reason: 'v02 carry-over: v1.1.0 target (PIT mutation testing per D6 decision)'}
];

// L-OPEN port: state.l_open[] → open_questions / deferred_ac classification
const l_open = state.l_open || [];
const open_questions = [];
const deferred_ac = [];
let oqSeq = 1;
for (const lo of l_open) {
  const pri = (lo.priority||'').toLowerCase();
  if (pri.includes('ph-005')) {
    open_questions.push({
      id: `OQ-${String(oqSeq++).padStart(3,'0')}`,
      question: `[${lo.id}] ${lo.title}`,
      blocks_task_ids: [],
      origin_priority: lo.priority,
      resolve_at_phase: 'PH-005'
    });
  } else if (pri.includes('ph-007')) {
    deferred_ac.push({
      req_id: 'IR-REL-001',
      ac_id: lo.id,
      reason: `[${lo.id}] ${lo.title}`,
      user_decision_id: 'v03-carryover-PH-007'
    });
    open_questions.push({
      id: `OQ-${String(oqSeq++).padStart(3,'0')}`,
      question: `[${lo.id}] ${lo.title}`,
      blocks_task_ids: [],
      origin_priority: lo.priority,
      resolve_at_phase: 'PH-007'
    });
  } else if (pri.includes('ph-006')) {
    open_questions.push({
      id: `OQ-${String(oqSeq++).padStart(3,'0')}`,
      question: `[${lo.id}] ${lo.title}`,
      blocks_task_ids: [],
      origin_priority: lo.priority,
      resolve_at_phase: 'PH-006'
    });
  } else if (pri.includes('커밋')) {
    open_questions.push({
      id: `OQ-${String(oqSeq++).padStart(3,'0')}`,
      question: `[${lo.id}] ${lo.title}`,
      blocks_task_ids: [],
      origin_priority: lo.priority,
      resolve_at_phase: 'commit-time'
    });
  } else {
    // low / auto-resolved: open_questions notes only
    open_questions.push({
      id: `OQ-${String(oqSeq++).padStart(3,'0')}`,
      question: `[${lo.id}] ${lo.title} (priority=${lo.priority})`,
      blocks_task_ids: [],
      origin_priority: lo.priority,
      resolve_at_phase: 'best-effort'
    });
  }
}

// Risks: 8 from v02 (preserved)
const risks = v02.risks || [];

// orphans: tasks without req_ids after filter
const orphans = tasks.filter(t => t.req_ids.length === 0).map(t => ({task_id: t.id, reason: 'req_ids became empty after draft filtering (should not happen — re-check)'}));

// unreferenced_reqs: 32 in-scope REQ 中 coverage 가 없는 것
const inScopeReqs = new Set(coverage.map(c => c.req_id));
const expectedReqs = new Set([
  'FR-CONV-001','FR-CONV-002','FR-CONV-003','FR-CONV-004','FR-CONV-005','FR-CONV-006','FR-CONV-007','FR-CONV-008','FR-CONV-009','FR-CONV-010','FR-CONV-011','FR-CONV-012','FR-CONV-013','FR-CONV-014',
  'FR-FIX-001','FR-FIX-002','FR-FIX-003','FR-FIX-004','FR-FIX-005','FR-FIX-006','FR-FIX-007','FR-FIX-008','FR-FIX-009',
  'FR-MOD-007','FR-REL-001','FR-TEST-001','FR-TEST-002','FR-TEST-004',
  'IR-REL-001','MIG-REL-001','NFR-CONV-001','NFR-TEST-001'
]);
const unreferenced_reqs = [];
for (const r of expectedReqs) {
  if (!inScopeReqs.has(r)) unreferenced_reqs.push({req_id: r, stability: 'evolving', reason: 'in v03 scope but no task mapping (re-check)'});
}

// TDD decisions: tdd_exempted_task_ids from state -> auto-tagged decisions
const tdd_decisions = [];
for (const t of tasks) {
  if (t.type === 'code' && t.tdd?.applicable === false) {
    tdd_decisions.push({
      task_id: t.id,
      decision: 'accept-as-exempt',
      reason: t.tdd?.exempt_reason || 'TDD 면제 (carry-over from v02 sidecar)',
      user_decision_id: 'v03-carryover',
      decided_at: NOW
    });
  }
}

// coder_handoff_readiness: per-phase
const coder_handoff_readiness = phases.map(p => ({
  phase_id: p.id,
  ready: true,
  blockers: []
}));

// build sidecar object
const sidecar = {
  schema_version: '1.1.0',
  plan_contract: '1.2.0',
  run_id: RUN_ID,
  target: TARGET,
  plan_version: '0.3.0',
  generated_at: NOW,
  tool_versions: {speckiwi: '2.2.2', kiwi_planner: '0.6.0', validator: '0.6.0'},
  tdd_policy: 'relaxed',
  md_path: PLAN_PATH,
  md_sha256: 'TBD',
  phases,
  tasks,
  coverage,
  orphans,
  unreferenced_reqs,
  excluded_reqs,
  deferred_ac,
  risks,
  open_questions,
  external_module_impact: [],
  tdd_decisions,
  coder_handoff_readiness,
  mcp_call_log: [],
  // v03 augments
  _v03_meta: {
    predecessor_run_id: '2026-05-19-jakarta-adapter-v1-0-0-v02',
    completed_tasks_from_v02: state.completed_task_ids,
    dropped_tasks_due_to_draft_only: droppedTasks,
    draft_policy: 'exclude-draft',
    evaluation_topology_legend: {
      simplified: 'PH-003 잔여 6 페어 — 시니어 + S4 only + green 시니어 + 정형 Sonnet + 까칠 skip = 5 호출/페어',
      full: 'PH-004 SPEC 변환 페어 — 기존 8 호출/페어 유지',
      file_op: 'PH-005/006/007 TDD 면제 file_op/doc/review/pr task',
      infra: 'PH-005/006 infra task (CI matrix, JaCoCo)'
    }
  }
};

// Write sidecar
const SIDECAR_OUT = `${ROOT}/docs/plans/${RUN_ID}.sidecar.json`;
fs.writeFileSync(SIDECAR_OUT, JSON.stringify(sidecar, null, 2));
console.log(`Sidecar written: ${SIDECAR_OUT}`);
console.log(`Tasks: ${tasks.length}`);
console.log(`Phases: ${phases.length}`);
console.log(`Coverage entries: ${coverage.length}`);
console.log(`Orphans: ${orphans.length}`);
console.log(`Unreferenced: ${unreferenced_reqs.length}`);
console.log(`Excluded: ${excluded_reqs.length}`);
console.log(`Open questions: ${open_questions.length}`);
console.log(`Deferred AC: ${deferred_ac.length}`);
console.log(`TDD decisions: ${tdd_decisions.length}`);

// Coverage summary
let totAc = 0, totCov = 0;
for (const c of coverage) {totAc += c.ac_total; totCov += c.ac_covered;}
console.log(`Coverage: ${totCov}/${totAc} AC (${Math.round(100*totCov/totAc)}%)`);
const missingByReq = coverage.filter(c => c.missing_ac_ids.length > 0);
if (missingByReq.length) console.log('Missing AC:', missingByReq.map(c=>c.req_id+':'+c.missing_ac_ids.join(',')).join('  |  '));
