#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""17-item self-check per Phase 2 spec section O."""
import json
import re
from pathlib import Path

ROOT = Path(r"C:\Work\git\_Snoworca\Jakarta_To_Javax_Servlet_Adapter")
SIDECAR = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json"
PLAN = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.plan.md"
INVENTORY = ROOT / "docs/analysis/kiwi-planner-2026-05-19-jakarta-adapter-v1-0-0-v02/inventory.json"

with open(SIDECAR, "r", encoding="utf-8") as f:
    sc = json.load(f)
with open(INVENTORY, "r", encoding="utf-8") as f:
    inventory = {r["id"]: r for r in json.load(f)}
with open(PLAN, "r", encoding="utf-8") as f:
    plan_text = f.read()

results = {}
issues = []

# 1. frontmatter: plan_contract="1.2.0", tdd_policy="relaxed"
ok1 = ('plan_contract: "1.2.0"' in plan_text) and ('tdd_policy: relaxed' in plan_text)
results["1_frontmatter_contract_policy"] = ok1
if not ok1: issues.append("frontmatter missing plan_contract 1.2.0 or tdd_policy relaxed")

# 2. sidecar schema 1.1.0, plan_contract 1.2.0
ok2 = sc["schema_version"] == "1.1.0" and sc["plan_contract"] == "1.2.0"
results["2_sidecar_schema_contract"] = ok2

# 3. phases.length / tasks.length 일치 (phase task_ids sum == tasks length)
sum_task_ids = sum(len(p["task_ids"]) for p in sc["phases"])
ok3 = sum_task_ids == len(sc["tasks"])
results["3_phase_task_count_match"] = ok3
if not ok3: issues.append(f"phase task_ids sum {sum_task_ids} != tasks {len(sc['tasks'])}")

# 4. plan.md §2 phase row count == sidecar.phases.length
ph2_pattern = re.compile(r"^\| (PH-\d{3}) \|", re.MULTILINE)
phase_rows = ph2_pattern.findall(plan_text)
ok4 = len(phase_rows) == len(sc["phases"])
results["4_plan_phase_rows"] = ok4

# 5. plan.md §3 h4 task heading count == sidecar.tasks.length
h4_pattern = re.compile(r"^#### §3\.PH-\d{3}\.(T-PH\d{3}-\d{2}) ", re.MULTILINE)
h4_ids = h4_pattern.findall(plan_text)
ok5 = len(h4_ids) == len(sc["tasks"])
results["5_plan_h4_count"] = ok5
if not ok5: issues.append(f"h4 headings {len(h4_ids)} != tasks {len(sc['tasks'])}")

# 6. All type=code tasks have tdd field
code_tasks = [t for t in sc["tasks"] if t["type"] == "code"]
ok6 = all("tdd" in t for t in code_tasks)
results["6_all_code_has_tdd"] = ok6

# 7. tdd.applicable=true: test_cases.length >= 1 AND phase in {red, green, refactor}
red_green_tasks = [t for t in sc["tasks"] if t.get("tdd", {}).get("applicable") is True]
ok7_phase = all(t["tdd"]["phase"] in ("red", "green", "refactor") for t in red_green_tasks)
ok7_red_tc = all(len(t["tdd"]["test_cases"]) >= 1 for t in red_green_tasks if t["tdd"]["phase"] == "red")
ok7 = ok7_phase and ok7_red_tc
results["7_tdd_applicable_constraint"] = ok7
if not ok7: issues.append("tdd.applicable=true constraints not met")

# 8. tdd.applicable=false (type=code): exempt_reason >= 20 chars, phase = "n/a"
exempt_code = [t for t in code_tasks if t.get("tdd", {}).get("applicable") is False]
ok8 = all(t["tdd"]["phase"] == "n/a" and len(t["tdd"].get("exempt_reason", "")) >= 20 for t in exempt_code)
results["8_exempt_code_reason"] = ok8
if not ok8:
    for t in exempt_code:
        if t["tdd"]["phase"] != "n/a" or len(t["tdd"].get("exempt_reason", "")) < 20:
            issues.append(f"Task {t['id']} exempt_reason inadequate: {t['tdd']}")

# 9. test_case.id regex
tc_pat = re.compile(r"^TC-REQ-[A-Z][A-Z0-9-]*-AC\d+-\d{2}$")
all_tcs = []
for t in sc["tasks"]:
    for tc in t.get("tdd", {}).get("test_cases", []):
        all_tcs.append(tc)
ok9 = all(tc_pat.match(tc["id"]) for tc in all_tcs)
results["9_test_case_id_regex"] = ok9
if not ok9:
    for tc in all_tcs:
        if not tc_pat.match(tc["id"]):
            issues.append(f"Bad test_case id: {tc['id']}")

# 10. test_case.req_id in task.req_ids
ok10 = True
for t in sc["tasks"]:
    for tc in t.get("tdd", {}).get("test_cases", []):
        if tc["req_id"] not in t["req_ids"]:
            ok10 = False
            issues.append(f"tc {tc['id']} req_id {tc['req_id']} not in task {t['id']} req_ids {t['req_ids']}")
results["10_test_case_req_in_task"] = ok10

# 11. test_case.ac_refs subset of inventory(req_id).ac_ids
ok11 = True
for tc in all_tcs:
    inv = inventory.get(tc["req_id"])
    if not inv:
        ok11 = False
        issues.append(f"tc {tc['id']} req_id {tc['req_id']} not in inventory")
        continue
    for ac in tc["ac_refs"]:
        if ac not in inv["ac_ids"]:
            ok11 = False
            issues.append(f"tc {tc['id']} ac_ref {ac} not in inventory ac_ids {inv['ac_ids']}")
results["11_ac_refs_subset_inventory"] = ok11

# 12. depends_on_task DAG (no cycles)
def has_cycle(graph):
    color = {}
    def visit(node):
        c = color.get(node)
        if c == 1: return True
        if c == 2: return False
        color[node] = 1
        for d in graph.get(node, []):
            if visit(d): return True
        color[node] = 2
        return False
    for n in graph:
        if visit(n): return True
    return False

graph = {t["id"]: t.get("depends_on_task", []) for t in sc["tasks"]}
ok12 = not has_cycle(graph)
results["12_dag_no_cycle"] = ok12

# 13. depends_on_task referential integrity
task_ids_set = set(t["id"] for t in sc["tasks"])
ok13 = True
for t in sc["tasks"]:
    for d in t.get("depends_on_task", []):
        if d not in task_ids_set:
            ok13 = False
            issues.append(f"Task {t['id']} depends_on_task {d} missing")
results["13_depends_referential_integrity"] = ok13

# 14. green task: depends_on_task contains same-pair red task id with same covers_ac (§0.G8)
ok14 = True
for t in sc["tasks"]:
    if t.get("tdd", {}).get("phase") == "green":
        deps = t.get("depends_on_task", [])
        if not deps:
            ok14 = False
            issues.append(f"green task {t['id']} has no depends_on_task")
            continue
        # The first dep should be red task with matching covers_ac
        red_id = deps[0]
        red_task = next((rt for rt in sc["tasks"] if rt["id"] == red_id), None)
        if not red_task or red_task.get("tdd", {}).get("phase") != "red":
            ok14 = False
            issues.append(f"green task {t['id']} dep {red_id} is not a red task")
            continue
        if set(red_task.get("covers_ac", [])) != set(t.get("covers_ac", [])):
            ok14 = False
            issues.append(f"green task {t['id']} covers_ac {t.get('covers_ac', [])} mismatch with red {red_task.get('covers_ac', [])}")
results["14_green_red_pair_covers_ac"] = ok14

# 15. coverage.ac_test_map for all 44 REQs
ok15 = len(sc["coverage"]) == 44 and all(c.get("ac_test_map") is not None for c in sc["coverage"])
results["15_coverage_44_reqs"] = ok15

# 16. orphans=0, unreferenced_reqs=0
ok16 = len(sc["orphans"]) == 0 and len(sc["unreferenced_reqs"]) == 0
results["16_no_orphans_or_unreferenced"] = ok16

# 17. trace_link.link_id sidecar-wide unique
all_links = []
for t in sc["tasks"]:
    for tl in t.get("trace_links", []):
        all_links.append(tl["link_id"])
ok17 = len(all_links) == len(set(all_links))
results["17_trace_link_unique"] = ok17

# Summary
all_pass = all(results.values())
print(json.dumps({
    "all_pass": all_pass,
    "results": results,
    "issues": issues
}, indent=2, ensure_ascii=False))
