#!/usr/bin/env node
// Fill sidecar.mcp_call_log with nested-schema entries to match trace_links + coverage (planner SSOT, kiwi-coder bridges to flat at execution time per §9.5 (나))
const fs = require('fs');
const crypto = require('crypto');

const ROOT = 'C:/Work/git/_Snoworca/Jakarta_To_Javax_Servlet_Adapter';
const SIDECAR = `${ROOT}/docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v03.sidecar.json`;
const sc = JSON.parse(fs.readFileSync(SIDECAR, 'utf8'));

function canonJson(o) {
  if (o === null || typeof o !== 'object') return JSON.stringify(o);
  if (Array.isArray(o)) return '[' + o.map(canonJson).join(',') + ']';
  const keys = Object.keys(o).sort();
  return '{' + keys.map(k => JSON.stringify(k) + ':' + canonJson(o[k])).join(',') + '}';
}
function argsHash(call, args) {
  return crypto.createHash('sha1').update(call + '|' + canonJson(args)).digest('hex');
}

const NOW = '2026-05-19T15:50:00Z';
let seq = 1;
const log = [];

// (1) add_trace_link entries — one per task.trace_links[] (target.type=Requirement)
for (const t of sc.tasks) {
  for (const tl of (t.trace_links||[])) {
    if (tl.target.type !== 'Requirement') continue;  // only Requirement-type trace_links per §9.5 (가) C15 SSOT
    const nested = {
      source: tl.source,
      target: tl.target,
      relation: tl.relation
    };
    log.push({
      seq: seq++,
      call: 'add_trace_link',
      args: nested,
      args_hash: argsHash('add_trace_link', nested),
      response_hash: null,
      timestamp: NOW,
      ok: null  // planner-time placeholder; kiwi-coder fills ok=true after real flat-call
    });
  }
}

// (2) add_verification_evidence entries — one per coverage[].req_id (flat schema per §9.5 (가))
for (const c of sc.coverage) {
  const firstTask = c.covered_tasks[0];
  const flat = {
    id: c.req_id,
    type: 'plan',
    reference: `docs/plans/${sc.run_id}.plan.md#${firstTask}`
  };
  log.push({
    seq: seq++,
    call: 'add_verification_evidence',
    args: flat,
    args_hash: argsHash('add_verification_evidence', flat),
    response_hash: null,
    timestamp: NOW,
    ok: null
  });
}

// Annotate executed_calls inside phase5_status (the 6 real Code-anchor calls already done)
sc.mcp_call_log = log;
sc._v03_meta.phase5_status = sc._v03_meta.phase5_status || {};
sc._v03_meta.phase5_status.note = 'sidecar.mcp_call_log filled with nested-schema planner-SSOT entries per §9.5 (가). ok=null indicates planner-time placeholder; kiwi-coder §6.2 will flat-convert at task execution and update ok=true. 6 add_trace_link calls already executed against speckiwi MCP (Code-anchor, see executed_calls below) and persisted in SRS markdown.';
sc._v03_meta.phase5_status.planner_log_count = log.length;

fs.writeFileSync(SIDECAR, JSON.stringify(sc, null, 2));
console.log(`Filled mcp_call_log with ${log.length} entries (${log.filter(e=>e.call==='add_trace_link').length} add_trace_link + ${log.filter(e=>e.call==='add_verification_evidence').length} add_verification_evidence)`);
