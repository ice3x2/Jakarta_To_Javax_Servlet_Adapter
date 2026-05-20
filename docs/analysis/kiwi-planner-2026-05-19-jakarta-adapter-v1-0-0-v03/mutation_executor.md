# Phase 5 Mutation Execution Plan (v03)

Total calls: 114 (82 add_trace_link + 32 add_verification_evidence)

## Strategy

- Execute sequentially using mcp__speckiwi__add_trace_link and mcp__speckiwi__add_verification_evidence tools
- After each call, append to sidecar.mcp_call_log (nested §9.5 (가) schema)
- Idempotency: speckiwi MCP itself dedupes by (id, type, reference). v02 mutation log was empty so first-write expected.
- After all 114 calls, re-run validator to confirm C15 PASS

## Note on first call

T-PH003-09 → FR-FIX-004 (Code anchor) already executed (test). Will record in sidecar.mcp_call_log.
