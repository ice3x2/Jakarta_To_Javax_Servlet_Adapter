#!/usr/bin/env node
// Append 6 executed add_trace_link entries to sidecar.mcp_call_log
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

// 6 executed calls (T-PH003-09~14, each with Code anchor)
const executed = [
  {task_id: 'T-PH003-09', req: 'FR-FIX-004', ref: 'servlet5/src/test/java/adapter/jakarta/servlet5/ServletContextEncodingDefectTest.java', rel: 'verifies'},
  {task_id: 'T-PH003-10', req: 'FR-FIX-004', ref: 'servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java', rel: 'implements'},
  {task_id: 'T-PH003-11', req: 'FR-FIX-005', ref: 'servlet5/src/test/java/adapter/javax/servlet5/ServletContextEncodingDefectTest.java', rel: 'verifies'},
  {task_id: 'T-PH003-12', req: 'FR-FIX-005', ref: 'servlet5/src/main/java/adapter/javax/servlet5/ServletContext.java', rel: 'implements'},
  {task_id: 'T-PH003-13', req: 'FR-FIX-006', ref: 'servlet5/src/test/java/adapter/jakarta/servlet5/ServletRequestEncodingDefectTest.java', rel: 'verifies'},
  {task_id: 'T-PH003-14', req: 'FR-FIX-006', ref: 'servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java', rel: 'implements'}
];

let seq = 1;
const NOW = '2026-05-19T15:45:00Z';
const entries = executed.map(e => {
  const nested = {
    source: {type: 'Task', id: e.task_id},
    target: {type: 'Code', reference: e.ref},
    relation: e.rel
  };
  return {
    seq: seq++,
    call: 'add_trace_link',
    args: nested,
    args_hash: argsHash('add_trace_link', nested),
    response_hash: null,
    timestamp: NOW,
    ok: true
  };
});

sc.mcp_call_log = entries;
fs.writeFileSync(SIDECAR, JSON.stringify(sc, null, 2));
console.log(`Appended ${entries.length} mcp_call_log entries (Code anchor add_trace_link only).`);
console.log('NOTE: 76 remaining Requirement trace_links + 32 add_verification_evidence are deferred to kiwi-coder per-task mutation.');
