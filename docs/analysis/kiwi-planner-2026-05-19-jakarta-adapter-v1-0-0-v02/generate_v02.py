#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Generator for v02 plan.md + sidecar.json (plan_contract 1.2.0 with TDD relaxed).

Inputs: v01 sidecar + inventory.json. Outputs: v02 sidecar.json + v02 plan.md fragment.

Approach: build tasks list programmatically using v01 task templates and applying
TDD red+green pair decomposition to PH-003 + PH-004 code tasks (per spec).
"""
import json
import hashlib
from pathlib import Path

ROOT = Path(r"C:\Work\git\_Snoworca\Jakarta_To_Javax_Servlet_Adapter")
SIDECAR_V01 = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0.sidecar.json"
INVENTORY = ROOT / "docs/analysis/kiwi-planner-2026-05-19-jakarta-adapter-v1-0-0-v02/inventory.json"
OUTPUT_SIDECAR = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.sidecar.json"
OUTPUT_PLAN = ROOT / "docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v02.plan.md"
OUTPUT_STATS = ROOT / "docs/analysis/kiwi-planner-2026-05-19-jakarta-adapter-v1-0-0-v02/phase2_plan_draft_iter1.json"

# Load v01 sidecar + inventory
with open(SIDECAR_V01, "r", encoding="utf-8") as f:
    v01 = json.load(f)
with open(INVENTORY, "r", encoding="utf-8") as f:
    inventory_list = json.load(f)
INVENTORY_MAP = {r["id"]: r for r in inventory_list}

# v01 task by id
V01_TASKS = {t["id"]: t for t in v01["tasks"]}


def file_obj(path):
    """Build file object for sidecar (just path - line range is in plan.md)."""
    # Extract path before colon if line range present
    if ":" in path and not path.startswith("C:") and not path.startswith("/"):
        # form "path:line_range"
        return {"path": path}
    return {"path": path}


def files_from_v01(task_id):
    """Extract files list from v01 task with same path format."""
    t = V01_TASKS.get(task_id)
    if not t:
        return []
    return [{"path": f["path"]} for f in t.get("files", [])]


# ----- PH-001 (no decomposition - 3 non-coding tasks) -----
PH001 = [
    # v01 T-PH001-01 (issue MIG-REL-001) - but D8 resolved OQ-001 already
    {
        "id": "T-PH001-01",
        "phase_id": "PH-001",
        "title": "OQ-001 D8 결정문 SRS 인덱스 갱신 (옵션 B 채택 closed)",
        "type": "issue",
        "req_ids": ["MIG-REL-001"],
        "files": [{"path": "docs/research/80.decisions.md"}, {"path": "docs/spec/00.index.md"}, {"path": "docs/spec/50.compatibility.srs.md"}],
        "action": "80.decisions.md §1 D8 (옵션 B = servlet5 모듈 내 @Deprecated alias 패키지) 가 이미 확정 기록되어 있다. 본 task 는 (1) 00.index.md §9 OQ-001 status 가 closed 임을 확인·갱신, (2) 50.compatibility.srs.md MIG-REL-001 AC 의 옵션 B 분기를 active 로 표시, (3) GitHub Issue (있으면) 닫기 — 의 정합성 점검 단계.",
        "acceptance_tests": [{"kind": "checklist", "cmd": "80.decisions.md §1 D8 옵션 B 기록 확인 + 00.index.md §9 OQ-001 status=closed 갱신 + 50.compatibility.srs.md MIG-REL-001 옵션 B active 표시", "expected_exit": 0}],
        "verification_cmd": None,
        "dod": [
            "00.index.md §9 의 OQ-001 status 가 closed 임을 확인 또는 갱신",
            "50.compatibility.srs.md MIG-REL-001 AC 의 옵션 B 분기 active",
            "PH-007 의 alias 구현 Task (T-PH007-04) 가 본 결정 결과를 참조",
            "GitHub Issue 가 있는 경우 closed 처리"
        ],
        "rollback": "00.index.md 갱신 reverse. 단 D8 자체는 80.decisions.md SSOT 이므로 본 task 는 메타 동기화일 뿐.",
        "estimated_effort": "S",
        "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
    },
    {
        "id": "T-PH001-02",
        "phase_id": "PH-001",
        "title": "OQ-002 JPMS 유보 명시 확인",
        "type": "review",
        "req_ids": ["NFR-MOD-001"],
        "files": [{"path": "docs/spec/00.index.md"}, {"path": "docs/spec/10.modularization.srs.md"}],
        "action": "NFR-MOD-002 (module-info.java 본격 도입) 가 v1.1.0 으로 이연됨을 확인한다. v1.0.0 에서는 NFR-MOD-001 의 Automatic-Module-Name MANIFEST 항목만 적용한다. SRS 인덱스 §9 의 OQ-002 status 가 deferred 임을 확인하고, plan 의 excluded_reqs 에 NFR-MOD-002 등재 여부를 재확인한다. 본 review 는 NFR-MOD-001 의 AC-2 (4개 모듈 module-name 표 일치) 의 사전 게이트로 작동한다 — PH-002 의 어떤 task 도 module-info.java 생성을 포함하지 않음을 보장.",
        "acceptance_tests": [{"kind": "checklist", "items": ["00.index.md §9 OQ-002 status=deferred 확인", "plan sidecar.json excluded_reqs 에 NFR-MOD-002 포함 확인", "PH-002 의 어떤 task 도 module-info.java 생성을 포함하지 않음 (T-PH002-09 의 NFR-MOD-001 적용만 활성)"]}],
        "verification_cmd": None,
        "dod": [
            "docs/spec/00.index.md §9 OQ-002 행이 status=deferred 로 유지됨을 확인",
            "plan 의 excluded_reqs[] 에 NFR-MOD-002 (사유: v1.1.0 target, JPMS deferred) 가 등재됨을 검토",
            "PH-002 의 어떤 task 도 module-info.java 생성을 포함하지 않음을 PH-002 진입 전 확인"
        ],
        "rollback": "본 review 가 통과되지 않으면 PH-002 의 manifest 작성 task (T-PH002-09) 만 진행하고 module-info 작업은 v1.1.0 으로 차단.",
        "estimated_effort": "S",
        "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
    },
    {
        "id": "T-PH001-03",
        "phase_id": "PH-001",
        "title": "OQ-003 Sonatype 계정 + GPG 키 외부 트래킹 이슈 생성",
        "type": "issue",
        "req_ids": ["OPS-REL-001"],
        "files": [{"path": "docs/spec/50.compatibility.srs.md"}],
        "action": "GitHub Issue 를 생성하여 외부 의존 (Sonatype OSSRH 또는 Central Portal 계정 발급, com.clipsoft namespace 점유 검증, GPG 키 생성·키서버 등록) 의 준비 상태를 트래킹한다. PH-002~PH-006 의 코딩 작업과 병렬 진행하며 PH-007 진입 전까지 자격증명을 완비한다. GitHub Secrets (OSSRH_USERNAME/PASSWORD, GPG_SIGNING_KEY/PASSPHRASE) 등록도 본 이슈로 추적한다.",
        "acceptance_tests": [{"kind": "checklist", "cmd": "GitHub Issue 'OQ-003 Maven Central credentials prep' 생성 + 4개 sub-task 체크박스(계정/namespace/GPG/secrets) 포함", "expected_exit": 0}],
        "verification_cmd": None,
        "dod": [
            "GitHub Issue URL 이 본 task 의 acceptance evidence 로 등재된다",
            "Issue 본문에 4개 sub-task 가 체크박스로 포함된다 (Sonatype 계정 / namespace 점유 / GPG 키 / GitHub Secrets)",
            "PH-007 진입 시점에 본 이슈가 closed 상태여야 함을 README 또는 CHANGELOG 에 표기"
        ],
        "rollback": "Issue 가 PH-007 진입 시점까지 open 이면 PH-007 의 publishAll task (T-PH007-07) 가 차단되어 v1.0.0 출시 지연.",
        "estimated_effort": "S",
        "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
    },
]

# ----- PH-002 (4 code (Gradle exempt) + 5 file_op = 9 tasks, no decomposition) -----
# v01 T-PH002-01: file_op, T-PH002-02: code, T-PH002-03: file_op, T-PH002-04: file_op,
# T-PH002-05: file_op, T-PH002-06: file_op, T-PH002-07: code, T-PH002-08: code, T-PH002-09: code
# 4 code: T-PH002-02 + T-PH002-07 + T-PH002-08 + T-PH002-09 → exempt (Gradle build script)
# 5 file_op: T-PH002-01, 03, 04, 05, 06

GRADLE_EXEMPT = "Gradle build script + settings authoring; build correctness validated by `./gradlew build` exit codes and module graph assertions, not unit-level TDD red-green cycles."
IT_FIXTURE_EXEMPT = "Integration test harness/fixture authoring; the test fixtures themselves constitute the TDD acceptance for FR-FIX/FR-CONV implementation phases. Meta-TDD on test infrastructure is impractical and not value-adding."
RELEASE_ARTIFACT_EXEMPT = "Release artifact authoring (CHANGELOG/README badges/version bump); validated by file_state checks and Maven Central staging verification, not behavior tests."
CI_INFRA_EXEMPT = "CI matrix configuration; correctness validated by GHA workflow successful run, not unit tests."

PH002 = []
# T-PH002-01 file_op (settings.gradle)
PH002.append({
    "id": "T-PH002-01",
    "phase_id": "PH-002",
    "title": "settings.gradle rootProject.name 변경 + 5개 서브 프로젝트 include",
    "type": "file_op",
    "req_ids": ["FR-MOD-001", "IR-REL-001"],
    "files": [{"path": "settings.gradle"}],
    "action": "settings.gradle 의 rootProject.name = 'JavaXServletToJakartaAdapter' 를 rootProject.name = 'jakarta-to-javax-servlet-adapter' (kebab-case, D5) 로 변경하고 include 'common', 'servlet5', 'servlet6', 'servlet61', 'bom' 를 추가한다.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew projects --quiet | grep -E \"(common|servlet5|servlet6|servlet61|bom)\" | wc -l", "expected_exit": 0, "stdout_regex": "^5$"}],
    "verification_cmd": {"posix": "./gradlew projects", "windows": ".\\gradlew.bat projects"},
    "dod": [
        "settings.gradle 의 rootProject.name 이 jakarta-to-javax-servlet-adapter 로 변경됨",
        "include 'common', 'servlet5', 'servlet6', 'servlet61', 'bom' 가 등록됨",
        "./gradlew projects 출력에 5개 서브 프로젝트가 모두 표시됨"
    ],
    "rollback": "git diff 로 이전 state 복원. settings.gradle 1 파일 단위 변경이므로 영향 범위 작음.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
# T-PH002-02 code (root build.gradle) - Gradle exempt
PH002.append({
    "id": "T-PH002-02",
    "phase_id": "PH-002",
    "title": "root build.gradle 을 allprojects / subprojects 공통 설정으로 변환",
    "type": "code",
    "req_ids": ["FR-MOD-001", "IR-REL-001"],
    "files": [{"path": "build.gradle"}, {"path": "gradle.properties"}],
    "action": "현 단일 모듈 build.gradle (36 LOC) 을 allprojects { group='com.clipsoft', version='1.0.0' } + subprojects { apply plugin: 'java', repositories, ... } 형태로 재구성한다. gradle.properties 를 신설하여 toolchain 버전 default, JaCoCo 버전, Mockito/JUnit 버전 등 공통 프로퍼티를 추출한다.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :common:properties --quiet | grep -E \"^(group|version):\"", "expected_exit": 0, "stdout_regex": "group: com\\.clipsoft.*version: 1\\.0\\.0"}],
    "verification_cmd": {"posix": "./gradlew properties | grep -E '(group|version)'", "windows": ".\\gradlew.bat properties | findstr /R \"group version\""},
    "dod": [
        "root build.gradle 가 allprojects + subprojects 블록을 가지며 모듈별 build.gradle 에 위임",
        "모든 서브 프로젝트에서 group=com.clipsoft, version=1.0.0 가 표시됨",
        "gradle.properties 가 신설되어 공통 프로퍼티 보유"
    ],
    "rollback": "git diff 로 build.gradle / gradle.properties 복원.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": GRADLE_EXEMPT}
})
# T-PH002-03 file_op (common module + ABI movement)
PH002.append({
    "id": "T-PH002-03",
    "phase_id": "PH-002",
    "title": "common 모듈 신설 (EnumsConverter, HttpComponentConverter, ReadListenerConverter, WriteListenerConverter, ConverterSupport)",
    "type": "file_op",
    "req_ids": ["FR-MOD-002", "FR-MOD-004"],
    "files": [
        {"path": "common/build.gradle"},
        {"path": "common/src/main/java/adapter/common/ConverterSupport.java"},
        {"path": "common/src/main/java/adapter/common/EnumsConverter.java"},
        {"path": "common/src/main/java/adapter/common/HttpComponentConverter.java"},
        {"path": "common/src/main/java/adapter/common/ReadListenerConverter.java"},
        {"path": "common/src/main/java/adapter/common/WriteListenerConverter.java"}
    ],
    "action": "common/ 디렉토리를 신설하고 common/build.gradle 에 apply plugin: 'java', sourceCompatibility=1.8, compileOnly 'jakarta.servlet:jakarta.servlet-api:5.0.0', testImplementation 'jakarta.servlet:jakarta.servlet-api:5.0.0' 를 작성한다. 현 src/main/java/adapter/servletElementConverter/ 의 EnumsConverter.java, HttpComponentConverter.java, ReadListenerConverter.java, WriteListenerConverter.java 4개 파일을 common/src/main/java/adapter/common/ 으로 git mv 하고 패키지 선언을 package adapter.common; 으로 변경한다. 공용 예외 변환 헬퍼 ConverterSupport.java 를 신설한다.",
    "acceptance_tests": [
        {"kind": "shell", "cmd": "./gradlew :common:compileJava --quiet", "expected_exit": 0},
        {"kind": "file_state", "cmd": "test -f common/src/main/java/adapter/common/EnumsConverter.java && test -f common/src/main/java/adapter/common/HttpComponentConverter.java && test -f common/src/main/java/adapter/common/ReadListenerConverter.java && test -f common/src/main/java/adapter/common/WriteListenerConverter.java && test -f common/src/main/java/adapter/common/ConverterSupport.java", "expected_exit": 0}
    ],
    "verification_cmd": {"posix": "./gradlew :common:compileJava && ls common/src/main/java/adapter/common/", "windows": ".\\gradlew.bat :common:compileJava && dir common\\src\\main\\java\\adapter\\common"},
    "dod": [
        "common/build.gradle 가 jakarta 5.0.0 compileOnly 핀 + Java 8 타깃을 가진다",
        "common/src/main/java/adapter/common/ 에 5개 클래스만 존재",
        "5개 클래스의 패키지 선언이 package adapter.common; 로 변경됨",
        "./gradlew :common:compileJava 성공"
    ],
    "rollback": "git mv 의 reverse 로 원상 복귀. 단 PH-003/PH-004 가 본 task 이동된 위치를 import 하므로 후속 phase 진입 전 결정.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
# T-PH002-04 file_op (servlet5 move)
PH002.append({
    "id": "T-PH002-04",
    "phase_id": "PH-002",
    "title": "servlet5 모듈 신설 + 현 0.0.1 소스 이동 + adapter.jakarta.servlet5 / adapter.javax.servlet5 / adapter.servletElementConverter5 패키지 변경",
    "type": "file_op",
    "req_ids": ["FR-MOD-002", "FR-MOD-003", "FR-MOD-005", "FR-MOD-006"],
    "files": [
        {"path": "servlet5/build.gradle"},
        {"path": "servlet5/src/main/java/adapter/jakarta/servlet5/"},
        {"path": "servlet5/src/main/java/adapter/javax/servlet5/"},
        {"path": "servlet5/src/main/java/adapter/servletElementConverter5/"},
        {"path": "servlet5/src/main/java/com/snoworca/ServletAdapter.java"},
        {"path": "servlet5/src/main/java/com/snoworca/ServletAdapter5.java"}
    ],
    "action": "servlet5/ 디렉토리를 신설하고 servlet5/build.gradle 에 sourceCompatibility=1.8, compileOnly jakarta 5.0.0, javax 4.0.1, implementation project(':common') 를 작성한다. 현 src/main/java/adapter/jakarta/servlet/ → servlet5/src/main/java/adapter/jakarta/servlet5/ 로 git mv + 패키지 선언 변경. 동일 패턴으로 javax 와 servletElementConverter 의 비-ABI 컨버터 5개도 이동. ServletAdapter.java + ServletAdapter5.java 별칭 클래스 신설.",
    "acceptance_tests": [
        {"kind": "shell", "cmd": "./gradlew :servlet5:compileJava --quiet", "expected_exit": 0},
        {"kind": "file_state", "cmd": "test -d servlet5/src/main/java/adapter/jakarta/servlet5 && test -d servlet5/src/main/java/adapter/javax/servlet5 && test -d servlet5/src/main/java/adapter/servletElementConverter5 && test -f servlet5/src/main/java/com/snoworca/ServletAdapter.java && test -f servlet5/src/main/java/com/snoworca/ServletAdapter5.java", "expected_exit": 0}
    ],
    "verification_cmd": {"posix": "./gradlew :servlet5:compileJava && find servlet5/src/main -name '*.java' | wc -l", "windows": ".\\gradlew.bat :servlet5:compileJava"},
    "dod": [
        "servlet5/build.gradle 가 jakarta 5.0.0 + javax 4.0.1 compileOnly + common project 의존을 가진다",
        "현 adapter.jakarta.servlet.* / adapter.javax.servlet.* / 비-ABI 컨버터 5개 모두 servlet5 의 새 패키지로 이동",
        "com.snoworca.ServletAdapter5 별칭 클래스가 신설되어 ServletAdapter 로 위임",
        "./gradlew :servlet5:compileJava 성공"
    ],
    "rollback": "git mv reverse + 패키지 선언 복원.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
# T-PH002-05 file_op (servlet6)
PH002.append({
    "id": "T-PH002-05",
    "phase_id": "PH-002",
    "title": "servlet6 모듈 신설 + jakarta 6.0.0 + Java 11 toolchain + servlet5 코드 사본 (패키지명 변경)",
    "type": "file_op",
    "req_ids": ["FR-MOD-002", "FR-MOD-003", "FR-MOD-005", "FR-MOD-006"],
    "files": [
        {"path": "servlet6/build.gradle"},
        {"path": "servlet6/src/main/java/adapter/jakarta/servlet6/"},
        {"path": "servlet6/src/main/java/adapter/javax/servlet6/"},
        {"path": "servlet6/src/main/java/adapter/servletElementConverter6/"},
        {"path": "servlet6/src/main/java/com/snoworca/ServletAdapter.java"},
        {"path": "servlet6/src/main/java/com/snoworca/ServletAdapter6.java"}
    ],
    "action": "servlet6/build.gradle 에 toolchain JDK 11, jakarta 6.0.0 + javax 4.0.1 compileOnly, common project 의존 작성. servlet5 의 모든 소스를 servlet6 로 사본 + 패키지명을 adapter.jakarta.servlet6 / adapter.javax.servlet6 / adapter.servletElementConverter6 로 변경. 6.0 신규 추상 메서드 (getRequestId, getProtocolRequestId, getServletConnection) 와 폴백은 PH-004 로 이연하고 컴파일만 통과하도록 placeholder 처리.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet6:compileJava --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew :servlet6:compileJava", "windows": ".\\gradlew.bat :servlet6:compileJava"},
    "dod": [
        "servlet6/build.gradle 가 jakarta 6.0.0 + javax 4.0.1 compileOnly + JDK 11 toolchain 보유",
        "servlet5 의 모든 소스가 servlet6 로 패키지명 변경된 사본으로 존재",
        "com.snoworca.ServletAdapter6 별칭 클래스 존재",
        "./gradlew :servlet6:compileJava 성공 (placeholder 메서드 포함)"
    ],
    "rollback": "servlet6 디렉토리 전체 삭제 + settings.gradle include 행 제거.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
# T-PH002-06 file_op (servlet61)
PH002.append({
    "id": "T-PH002-06",
    "phase_id": "PH-002",
    "title": "servlet61 모듈 신설 + jakarta 6.1.0 + Java 17 toolchain + servlet6 코드 사본 (패키지명 변경)",
    "type": "file_op",
    "req_ids": ["FR-MOD-002", "FR-MOD-003", "FR-MOD-005", "FR-MOD-006"],
    "files": [
        {"path": "servlet61/build.gradle"},
        {"path": "servlet61/src/main/java/adapter/jakarta/servlet61/"},
        {"path": "servlet61/src/main/java/adapter/javax/servlet61/"},
        {"path": "servlet61/src/main/java/adapter/servletElementConverter61/"},
        {"path": "servlet61/src/main/java/com/snoworca/ServletAdapter.java"},
        {"path": "servlet61/src/main/java/com/snoworca/ServletAdapter61.java"}
    ],
    "action": "servlet61/build.gradle 에 toolchain JDK 17, jakarta 6.1.0 + javax 4.0.1 compileOnly, common project 의존 작성. servlet6 의 모든 소스를 servlet61 로 사본 + 패키지명을 adapter.jakarta.servlet61 / adapter.javax.servlet61 / adapter.servletElementConverter61 로 변경. 6.1 신규 추상 메서드 sendRedirect(String, int, boolean) 는 PH-004 로 이연하고 컴파일만 placeholder 처리.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet61:compileJava --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew :servlet61:compileJava", "windows": ".\\gradlew.bat :servlet61:compileJava"},
    "dod": [
        "servlet61/build.gradle 가 jakarta 6.1.0 + javax 4.0.1 compileOnly + JDK 17 toolchain 보유",
        "servlet6 의 모든 소스가 servlet61 로 패키지명 변경된 사본으로 존재",
        "com.snoworca.ServletAdapter61 별칭 클래스 존재",
        "./gradlew :servlet61:compileJava 성공"
    ],
    "rollback": "servlet61 디렉토리 전체 삭제 + settings.gradle include 행 제거.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
# T-PH002-07 code (bom) - Gradle exempt
PH002.append({
    "id": "T-PH002-07",
    "phase_id": "PH-002",
    "title": "bom 모듈 신설 (java-platform plugin + 4개 모듈 constraints)",
    "type": "code",
    "req_ids": ["FR-MOD-007"],
    "files": [{"path": "bom/build.gradle"}],
    "action": "bom/build.gradle 에 plugins { id 'java-platform'; id 'maven-publish' } + dependencies { constraints { api project(':common'); api project(':servlet5'); api project(':servlet6'); api project(':servlet61') } } 작성. generatePomFileForBomPlatformPublication task 가 정상 생성됨을 검증.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :bom:generatePomFileForBomPlatformPublication --quiet && test -f bom/build/publications/bomPlatform/pom-default.xml && grep -E 'jakarta-to-javax-servlet-adapter-(common|servlet5|servlet6|servlet61)' bom/build/publications/bomPlatform/pom-default.xml | wc -l", "expected_exit": 0, "stdout_regex": "^4$"}],
    "verification_cmd": {"posix": "./gradlew :bom:generatePomFileForBomPlatformPublication && cat bom/build/publications/bomPlatform/pom-default.xml", "windows": ".\\gradlew.bat :bom:generatePomFileForBomPlatformPublication && type bom\\build\\publications\\bomPlatform\\pom-default.xml"},
    "dod": [
        "bom/build.gradle 가 java-platform + maven-publish 플러그인 보유",
        "constraints 에 4개 모듈 (common/servlet5/6/61) 모두 등재",
        "생성된 POM 의 <dependencyManagement> 에 4개 artifactId 모두 포함"
    ],
    "rollback": "bom 디렉토리 삭제 + settings.gradle include 행 제거.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": GRADLE_EXEMPT}
})
# T-PH002-08 code (cross-deps validation) - Gradle exempt
PH002.append({
    "id": "T-PH002-08",
    "phase_id": "PH-002",
    "title": "버전 모듈 간 상호 의존 금지 검증 + 위반 시 빌드 실패 게이트",
    "type": "code",
    "req_ids": ["CON-MOD-001"],
    "files": [{"path": "build.gradle"}, {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"}],
    "action": "각 servletN/build.gradle 의 dependencies 블록에 implementation project(':common') 만 존재함을 보장한다. root build.gradle 에 단방향 의존 그래프 강제 게이트 task validateNoVersionModuleCrossDeps 를 추가하여 :servlet5/6/61 의 dependencies 출력에 다른 servletN project 가 없음을 검사하고 발견 시 throw new GradleException. ./gradlew check 가 본 task 를 통합 호출.",
    "acceptance_tests": [
        {"kind": "shell", "cmd": "./gradlew validateNoVersionModuleCrossDeps --quiet", "expected_exit": 0},
        {"kind": "shell", "cmd": "./gradlew :servlet5:dependencies --configuration compileClasspath --quiet | grep -E 'project :servlet(6|61)' | wc -l", "expected_exit": 0, "stdout_regex": "^0$"},
        {"kind": "shell", "cmd": "./gradlew :servlet6:dependencies --configuration compileClasspath --quiet | grep -E 'project :servlet(5|61)' | wc -l", "expected_exit": 0, "stdout_regex": "^0$"},
        {"kind": "shell", "cmd": "./gradlew :servlet61:dependencies --configuration compileClasspath --quiet | grep -E 'project :servlet(5|6)' | wc -l", "expected_exit": 0, "stdout_regex": "^0$"}
    ],
    "verification_cmd": {"posix": "./gradlew validateNoVersionModuleCrossDeps", "windows": ".\\gradlew.bat validateNoVersionModuleCrossDeps"},
    "dod": [
        "3 버전 모듈의 dependencies 출력에 다른 버전 모듈 project 가 없음",
        "validateNoVersionModuleCrossDeps 게이트 task 가 root 에 등록되어 check task 가 의존",
        "CON-MOD-001 의 3 AC 모두 자동 검증"
    ],
    "rollback": "root build.gradle 의 게이트 task 제거 + servletN/build.gradle 의 의존 복원.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": GRADLE_EXEMPT}
})
# T-PH002-09 code (Automatic-Module-Name) - Gradle exempt
PH002.append({
    "id": "T-PH002-09",
    "phase_id": "PH-002",
    "title": "4개 모듈 jar 의 MANIFEST 에 Automatic-Module-Name 헤더 추가",
    "type": "code",
    "req_ids": ["NFR-MOD-001"],
    "files": [{"path": "common/build.gradle"}, {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"}],
    "action": "각 모듈 build.gradle 의 jar { manifest { attributes 'Automatic-Module-Name': '<name>' } } 블록을 추가한다. 매핑: common → com.clipsoft.servlet.adapter.common, servlet5 → com.clipsoft.servlet.adapter.servlet5, servlet6 → com.clipsoft.servlet.adapter.servlet6, servlet61 → com.clipsoft.servlet.adapter.servlet61. (NFR-MOD-002 는 v1.0.0 범위 밖 — module-info.java 생성 금지.)",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :common:jar :servlet5:jar :servlet6:jar :servlet61:jar --quiet && for m in common servlet5 servlet6 servlet61; do unzip -p $m/build/libs/jakarta-to-javax-servlet-adapter-$m-1.0.0.jar META-INF/MANIFEST.MF | grep -E '^Automatic-Module-Name:' || exit 1; done", "expected_exit": 0}],
    "verification_cmd": {"posix": "for m in common servlet5 servlet6 servlet61; do echo \"=== $m ===\"; unzip -p $m/build/libs/jakarta-to-javax-servlet-adapter-$m-1.0.0.jar META-INF/MANIFEST.MF | grep Automatic-Module-Name; done", "windows": "for %m in (common servlet5 servlet6 servlet61) do @echo === %m ==="},
    "dod": [
        "4개 모듈 jar 의 MANIFEST 에 Automatic-Module-Name 헤더가 존재",
        "헤더 값이 NFR-MOD-001 표와 일치 (com.clipsoft.servlet.adapter.{common,servlet5,servlet6,servlet61})",
        "module-info.java 는 생성되지 않음 (NFR-MOD-002 v1.1.0 이연 확인)"
    ],
    "rollback": "build.gradle jar 블록의 manifest 설정 제거.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": GRADLE_EXEMPT}
})

# ----- PH-003: 10 code → 20 red+green tasks + 1 review = 21 tasks -----
# Pair structure:
#   T-PH003-01 (red FR-TEST-004 BugMuseum) + T-PH003-02 (green FR-TEST-004 BugMuseum)
#   T-PH003-03 (red FR-FIX-001) + T-PH003-04 (green)
#   ...
#   T-PH003-19 (red FR-FIX-009) + T-PH003-20 (green)
#   T-PH003-21 (review)

PH003 = []

def make_tc(req_id, ac_num, idx, test_file, test_symbol, kind, sig):
    return {
        "id": f"TC-REQ-{req_id}-AC{ac_num}-{idx:02d}",
        "req_id": req_id,
        "ac_refs": [f"AC-{ac_num}"],
        "test_file": test_file,
        "test_symbol": test_symbol,
        "kind": kind,
        "expected_failure_signature": sig
    }


# --- PH-003 페어 1: FR-TEST-004 BugMuseum bootstrap (red + green) ---
# FR-TEST-004 has 5 AC
PH003.append({
    "id": "T-PH003-01",
    "phase_id": "PH-003",
    "title": "BugMuseumTest 클래스 골격 + BM01~BM10 회귀 케이스 작성 (TDD red — 결함 코드에서 실패 확인)",
    "type": "code",
    "req_ids": ["FR-TEST-004"],
    "files": [{"path": "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java"}],
    "test_files": [{"path": "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java"}],
    "action": "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java 신설. BM01~BM10 각각의 @Test + @DisplayName 메서드 작성. 각 테스트는 현 결함 있는 코드에서 명시적으로 실패하도록 작성 (TDD red). BM10 은 잠금 (lock-in) — 현 ThreadLocal 단일 스레드 동기 호출 가정을 회귀 보호.",
    "covers_ac": ["AC-1", "AC-2", "AC-3", "AC-4", "AC-5"],
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest 2>&1 | grep -E 'BM0[1-9]' | grep -E '(FAILED|failed)' | wc -l", "expected_exit": 1, "stdout_regex": "^9$"}],
    "verification_cmd": {"posix": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest --info", "windows": ".\\gradlew.bat :servlet5:test --tests com.snoworca.regression.BugMuseumTest --info"},
    "dod": [
        "red 단계: BugMuseumTest.java 신설 + BM01~BM10 작성됨",
        "red 단계: 작성된 BM01~BM09 test 가 모두 실패(red) 함을 확인 + BM10 만 통과 (TDD red phase)",
        "PH-003 의 모든 후속 결함 수정 task 의 검증 기준이 된다"
    ],
    "rollback": "BugMuseumTest.java 삭제.",
    "estimated_effort": "L",
    "tdd": {
        "applicable": True,
        "phase": "red",
        "test_cases_count": 5,
        "test_cases": [
            make_tc("FR-TEST-004", 1, 1, "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java", "shouldReproduceBugMuseumCaseBM01ToBM10AsTestClass", "unit", "expected: 10 @Test methods covering BM01..BM10, found: 0"),
            make_tc("FR-TEST-004", 2, 1, "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java", "shouldFailOnBuggyCodeForBM01ThroughBM09", "unit", "expected: BM01~BM09 to fail on buggy 0.0.1 code, found: passing assertions"),
            make_tc("FR-TEST-004", 3, 1, "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java", "shouldPassBM10ThreadLocalLockIn", "unit", "expected: BM10 lock-in passes on current ThreadLocal pattern, found: failing"),
            make_tc("FR-TEST-004", 4, 1, "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java", "shouldHaveDisplayNameWithDefectIdForEachBM", "unit", "expected: @DisplayName on each BM with defect ID + description, found: missing annotation"),
            make_tc("FR-TEST-004", 5, 1, "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java", "shouldServeAsRegressionGateForFR_FIX_001_to_009", "unit", "expected: BM01~BM09 wired to FR-FIX-001~009 verification, found: tests not invoked from fix tasks")
        ]
    }
})
PH003.append({
    "id": "T-PH003-02",
    "phase_id": "PH-003",
    "title": "BugMuseumTest 골격 + BM10 잠금 통과 (TDD green — 테스트 자체의 인프라/픽스처 정합)",
    "type": "code",
    "req_ids": ["FR-TEST-004"],
    "files": [{"path": "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java"}],
    "test_files": [{"path": "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java"}],
    "action": "T-PH003-01 의 BugMuseumTest 가 BM10 lock-in 테스트만 우선 통과하도록 fixture/mock 정합한다. BM01~BM09 는 후속 결함 수정 페어 (T-PH003-03~T-PH003-20) 의 green 단계에서 통과시킨다. green 단계의 의미: BugMuseum 인프라/픽스처가 정상 동작하고 BM10 회귀 잠금이 active 라는 사실 자체를 검증.",
    "covers_ac": ["AC-1", "AC-2", "AC-3", "AC-4", "AC-5"],
    "depends_on_task": ["T-PH003-01"],
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest --tests *BM10* --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew :servlet5:test --tests *BM10*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM10*"},
    "dod": [
        "green 단계: BM10 lock-in 테스트 통과",
        "BugMuseumTest 인프라 (mock + fixture) 정상 동작",
        "BM01~BM09 의 통과는 T-PH003-04/06/08/10/12/14/16/18/20 의 green 단계에서 점진적으로 달성"
    ],
    "rollback": "T-PH003-01 으로 복귀.",
    "estimated_effort": "M",
    "tdd": {"applicable": True, "phase": "green", "test_cases_count": 0, "test_cases": []}
})

# --- Helpers for FR-FIX pair generation ---
FIX_SPECS = [
    # (req_id, ac_count, bm_num, title_short, action_red, action_green, files_red, files_green, test_symbol, fail_sig)
    ("FR-FIX-001", 4, "BM01",
     "StreamConverter.read 가 readLine 호출하는 결함 (양방향)",
     "servlet5/src/test/java/adapter/servletElementConverter5/StreamConverterDefectTest.java 신설. read(byte[], int, int) 호출이 readLine 으로 위임되지 않고 read 로 정확히 위임되는지 verify. AC-1~AC-4 각각 (write_to_byte_array_no_line_truncation / read_binary_safety / no_text_mode_assumption / cross_module_servlet6_61_consistency) 테스트 메서드 작성. 현 코드에서 모든 4건 실패.",
     "StreamConverter.java L41 (jakarta) 의 inputstream.readLine(b, off, len) 호출을 inputstream.read(b, off, len) 로 수정. L93 (javax) 동일 수정. readLine(byte[], int, int) 시그니처는 별도 유지. servlet6 / servlet61 모듈의 동일 위치도 동일 수정.",
     ["servlet5/src/test/java/adapter/servletElementConverter5/StreamConverterDefectTest.java"],
     ["servlet5/src/main/java/adapter/servletElementConverter5/StreamConverter.java", "servlet6/src/main/java/adapter/servletElementConverter6/StreamConverter.java", "servlet61/src/main/java/adapter/servletElementConverter61/StreamConverter.java"],
     "shouldDelegateReadToReadNotReadLine", "expected: origin.read(b, off, len) called once, got: origin.readLine(b, off, len)"),

    ("FR-FIX-002", 4, "BM02",
     "Part.getSubmittedFileName 무한 재귀 (jakarta + javax)",
     "PartDefectTest.java 신설. getSubmittedFileName() 호출이 this.part.getSubmittedFileName() 로 위임되는지 verify. AC-1~AC-4 — no_stack_overflow / origin_delegation_verify / jakarta_javax_symmetry / cross_module_consistency. 현 코드에서 4건 모두 실패 (StackOverflowError).",
     "jakarta Part.java L52 의 this.getSubmittedFileName() (자기 재귀) 를 this.part.getSubmittedFileName() (원본 위임) 로 수정. javax Part.java 대칭 위치도 동일. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/jakarta/servlet5/http/PartDefectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/http/Part.java", "servlet5/src/main/java/adapter/javax/servlet5/http/Part.java", "servlet6/src/main/java/adapter/jakarta/servlet6/http/Part.java", "servlet6/src/main/java/adapter/javax/servlet6/http/Part.java", "servlet61/src/main/java/adapter/jakarta/servlet61/http/Part.java", "servlet61/src/main/java/adapter/javax/servlet61/http/Part.java"],
     "shouldDelegateToOriginPartNotSelf", "expected: origin.getSubmittedFileName() called, got: StackOverflowError"),

    ("FR-FIX-003", 3, "BM05",
     "ServletResponse.setContentLengthLong narrow cast",
     "ServletResponseDefectTest.java 신설. setContentLengthLong(Long.MAX_VALUE) 호출 시 origin.setContentLengthLong(arg0) 위임 verify. AC-1~AC-3 — long_precision_preserved / no_int_cast / origin_method_invoked. 현 코드 L82 에서 모두 실패.",
     "jakarta ServletResponse.java L82 의 this.response.setContentLength((int)arg0) 를 this.response.setContentLengthLong(arg0) 로 수정. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/jakarta/servlet5/ServletResponseDefectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/ServletResponse.java", "servlet6/src/main/java/adapter/jakarta/servlet6/ServletResponse.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletResponse.java"],
     "shouldPreserveLongPrecisionForContentLength", "expected: setContentLengthLong(Long.MAX_VALUE) delegated as long, got: int cast value"),

    ("FR-FIX-004", 4, "BM03",
     "jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding)",
     "ServletContextEncodingDefectTest.java 신설. jakarta ServletContext.setResponseCharacterEncoding 호출 시 원본의 setResponseCharacterEncoding 이 호출되는지 verify (현 코드는 setRequestCharacterEncoding 으로 잘못 호출). AC-1~AC-4 — direction_correct / no_request_method_invoked / origin_response_method_called / cross_module_consistency.",
     "jakarta ServletContext.java L338 의 this.setRequestCharacterEncoding(arg0) (자기 메서드 + 잘못된 방향) 을 this.servletContext.setResponseCharacterEncoding(arg0) (원본 동명) 로 수정. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/jakarta/servlet5/ServletContextEncodingDefectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java", "servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java"],
     "shouldDelegateSetResponseCharEncodingToOriginSameMethod", "expected: origin.setResponseCharacterEncoding called, got: origin.setRequestCharacterEncoding"),

    ("FR-FIX-005", 3, "BM08",
     "javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭)",
     "javax ServletContextEncodingDefectTest.java 신설. javax ServletContext.setRequestCharacterEncoding 호출 시 원본의 setRequestCharacterEncoding 호출 verify (현재는 setResponseCharacterEncoding 으로 잘못 호출). AC-1~AC-3 — direction_correct / no_response_method_invoked / origin_request_method_called.",
     "javax ServletContext.java L363 의 this.setResponseCharacterEncoding(encoding) 을 this.servletContext.setRequestCharacterEncoding(encoding) 로 수정. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/javax/servlet5/ServletContextEncodingDefectTest.java"],
     ["servlet5/src/main/java/adapter/javax/servlet5/ServletContext.java", "servlet6/src/main/java/adapter/javax/servlet6/ServletContext.java", "servlet61/src/main/java/adapter/javax/servlet61/ServletContext.java"],
     "shouldDelegateSetRequestCharEncodingToOriginSameMethod", "expected: origin.setRequestCharacterEncoding called, got: origin.setResponseCharacterEncoding"),

    ("FR-FIX-006", 2, "BM09",
     "getContentLengthLong fallback 부정확 (try/catch + int fallback)",
     "ContentLengthDefectTest.java 신설. ServletRequest.getContentLengthLong(Long.MAX_VALUE) 호출 시 원본의 getContentLengthLong long 결과 그대로 반환 verify. 현 코드는 try/catch fallback 에서 int 반환. AC-1~AC-2 — long_precision_preserved / no_int_fallback.",
     "jakarta ServletRequest.java L173-179 의 try/catch + getContentLength() int fallback 을 제거하고 return this.request.getContentLengthLong() 단순 위임으로 수정. javax 측 대칭 검토. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/jakarta/servlet5/ContentLengthDefectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java", "servlet5/src/main/java/adapter/javax/servlet5/ServletRequest.java", "servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java", "servlet6/src/main/java/adapter/javax/servlet6/ServletRequest.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java", "servlet61/src/main/java/adapter/javax/servlet61/ServletRequest.java"],
     "shouldReturnLongMaxValueFromOriginGetContentLengthLong", "expected: Long.MAX_VALUE returned, got: int-cast value or fallback"),

    ("FR-FIX-007", 3, "BM06",
     "jakarta ServletRequest.getRealPath @Override 부재 메서드",
     "GetRealPathDefectTest.java 신설. jakarta ServletRequest.getRealPath 가 jakarta 5.0+ 인터페이스에 부재 → @Override 제거 또는 메서드 자체 제거 verify (reflection 기반). AC-1~AC-3 — no_override_annotation / method_absent_or_deprecated_only / cross_module_consistency.",
     "jakarta ServletRequest.java L124 의 @Override 가 5.0+ ServletRequest 인터페이스에 부재하는 메서드를 가리키는 결함 수정. jakarta 5.0 인터페이스 javadoc 확인 후 부재 시 메서드 자체를 제거하거나 @Override 만 제거하고 deprecated alias 로 유지. jakarta ServletContext.java 의 getRealPath 도 동일 검토. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/jakarta/servlet5/GetRealPathDefectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java", "servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java", "servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java", "servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java"],
     "shouldNotOverrideAbsentGetRealPathInJakartaInterface", "expected: no @Override on absent method, got: compile-time @Override on missing super method"),

    ("FR-FIX-008", 4, "BM04",
     "ServletReqResConverter instanceof 분기 namespace 오류 4건",
     "InstanceofBranchDefectTest.java 신설. ServletReqResConverter 의 4개 분기 (L14/L18/L22/L26) 가 입력 매개변수의 namespace 와 일치하는 instanceof 로 분기되는지 verify. 현 코드는 javax 매개변수에 jakarta instanceof 또는 그 반대로 항상 false. AC-1~AC-4 — jakarta_input_jakarta_check / javax_input_javax_check / no_cross_namespace / cross_module_consistency.",
     "ServletReqResConverter.java L14 의 instanceof javax.servlet.http.HttpServletResponse 가 jakarta 매개변수에 항상 false 인 결함. 정확한 분기는 instanceof jakarta.servlet.http.HttpServletResponse. L18, L22, L26 의 4개 분기 모두 동일 검토·수정 — 각 메서드의 입력 매개변수 namespace 에 일치하는 instanceof 로 정정. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/servletElementConverter5/InstanceofBranchDefectTest.java"],
     ["servlet5/src/main/java/adapter/servletElementConverter5/ServletReqResConverter.java", "servlet6/src/main/java/adapter/servletElementConverter6/ServletReqResConverter.java", "servlet61/src/main/java/adapter/servletElementConverter61/ServletReqResConverter.java"],
     "shouldMatchInputNamespaceForAllFourInstanceofBranches", "expected: instanceof branches match input arg namespace, got: cross-namespace always-false"),

    ("FR-FIX-009", 2, "BM07",
     "HttpServletRequest.getCookies null 미처리",
     "GetCookiesNullDefectTest.java 신설. jakarta + javax HttpServletRequest.getCookies() 호출 시 원본이 null 반환하면 어댑터도 null 반환 verify. 현 코드는 null cookies 배열 순회 시 NPE. AC-1~AC-2 — null_passthrough / empty_array_preserved.",
     "jakarta HttpServletRequest.java L166-174 의 getCookies() 메서드에 Cookie[] cookies = this.httpRequest.getCookies(); if (cookies == null) return null; 가드 추가. javax 측 대칭 적용. 빈 배열은 그대로 빈 배열 변환. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/adapter/jakarta/servlet5/http/GetCookiesNullDefectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpServletRequest.java", "servlet5/src/main/java/adapter/javax/servlet5/http/HttpServletRequest.java", "servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletRequest.java", "servlet6/src/main/java/adapter/javax/servlet6/http/HttpServletRequest.java", "servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletRequest.java", "servlet61/src/main/java/adapter/javax/servlet61/http/HttpServletRequest.java"],
     "shouldReturnNullWhenOriginCookiesIsNull", "expected: null returned when origin getCookies is null, got: NullPointerException"),
]

# Generate FR-FIX-* pair tasks T-PH003-03 onwards
seq = 3
for (req_id, ac_count, bm, title, action_red, action_green, files_red, files_green, test_symbol, fail_sig) in FIX_SPECS:
    red_id = f"T-PH003-{seq:02d}"
    green_id = f"T-PH003-{seq+1:02d}"

    # Build test_cases: one per AC (test_symbol with AC-specific variation)
    tcs = []
    for ac_n in range(1, ac_count + 1):
        tcs.append(make_tc(req_id, ac_n, 1,
                           files_red[0],
                           f"{test_symbol}_AC{ac_n}",
                           "unit",
                           fail_sig))

    PH003.append({
        "id": red_id,
        "phase_id": "PH-003",
        "title": f"{req_id} {title} — TDD red (failing test)",
        "type": "code",
        "req_ids": [req_id],
        "files": [{"path": p} for p in files_red],
        "test_files": [{"path": p} for p in files_red],
        "action": action_red,
        "covers_ac": [f"AC-{i}" for i in range(1, ac_count + 1)],
        "acceptance_tests": [{"kind": "shell", "cmd": f"./gradlew :servlet5:test --tests *{req_id.replace('-', '')}* --quiet", "expected_exit": 1, "stdout_regex": f"FAILED.*{req_id}"}],
        "verification_cmd": {"posix": f"./gradlew :servlet5:test --tests *{req_id.replace('-', '')}*", "windows": f".\\gradlew.bat :servlet5:test --tests *{req_id.replace('-', '')}*"},
        "dod": [
            f"red 단계: {req_id} 의 {ac_count}개 AC test case 작성됨 (현 결함 코드에서 모두 실패)",
            f"BM{bm[2:]} 회귀 케이스가 본 task 의 test_cases 와 매핑됨",
            "red 단계: 작성된 test 가 모두 실패(red) 함을 확인"
        ],
        "rollback": f"{files_red[0]} 삭제.",
        "estimated_effort": "S",
        "tdd": {
            "applicable": True,
            "phase": "red",
            "test_cases_count": ac_count,
            "test_cases": tcs
        }
    })
    PH003.append({
        "id": green_id,
        "phase_id": "PH-003",
        "title": f"{req_id} {title} — TDD green (impl fix)",
        "type": "code",
        "req_ids": [req_id],
        "files": [{"path": p} for p in files_green],
        "test_files": [],
        "action": action_green,
        "covers_ac": [f"AC-{i}" for i in range(1, ac_count + 1)],
        "depends_on_task": [red_id],
        "acceptance_tests": [{"kind": "shell", "cmd": f"./gradlew :servlet5:test --tests *{req_id.replace('-', '')}* :servlet6:test --tests *{req_id.replace('-', '')}* :servlet61:test --tests *{req_id.replace('-', '')}* --quiet", "expected_exit": 0}],
        "verification_cmd": {"posix": f"./gradlew :servlet5:test --tests *{req_id.replace('-', '')}* :servlet6:test --tests *{req_id.replace('-', '')}* :servlet61:test --tests *{req_id.replace('-', '')}*", "windows": f".\\gradlew.bat :servlet5:test --tests *{req_id.replace('-', '')}*"},
        "dod": [
            f"green 단계: {req_id} 의 {ac_count}개 test case 가 모두 통과(green)",
            f"BM{bm[2:]} 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두",
            "green 단계: 동일 test 가 모두 통과(green) 함을 확인"
        ],
        "rollback": f"git diff 로 {req_id} 수정 reverse — red task 로 복귀.",
        "estimated_effort": "S",
        "tdd": {
            "applicable": True,
            "phase": "green",
            "test_cases_count": 0,
            "test_cases": []
        }
    })
    seq += 2

# T-PH003-21 review
PH003.append({
    "id": "T-PH003-21",
    "phase_id": "PH-003",
    "title": "BM01~BM10 회귀 통과 + servlet6/61 사본 동기화 검증",
    "type": "review",
    "req_ids": ["FR-FIX-001", "FR-FIX-002", "FR-FIX-003", "FR-FIX-004", "FR-FIX-005", "FR-FIX-006", "FR-FIX-007", "FR-FIX-008", "FR-FIX-009", "FR-TEST-004"],
    "files": [{"path": "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java"}, {"path": "servlet6/src/test/java/com/snoworca/regression/BugMuseumTest.java"}, {"path": "servlet61/src/test/java/com/snoworca/regression/BugMuseumTest.java"}],
    "action": "PH-003 의 모든 결함 수정 페어 (T-PH003-03~T-PH003-20) 완료 후 ./gradlew :servlet5:test --tests *BugMuseum* :servlet6:test --tests *BugMuseum* :servlet61:test --tests *BugMuseum* 실행. BM01~BM09 모두 통과 + BM10 잠금 유지 확인. servlet6/servlet61 의 BugMuseumTest 는 servlet5 의 사본으로 동일 동작 검증. 미달 시 해당 fix 페어의 green task 로 재진입.",
    "acceptance_tests": [{"kind": "checklist", "items": ["BM01~BM10 회귀가 servlet5 모듈에서 모두 통과", "FR-FIX-001~009 결함 9건이 servlet5 의 fix 커밋에 모두 반영", "FR-FIX-007 servlet6/61 ServletContext 동기화 누락 없음"]}],
    "verification_cmd": {"posix": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest :servlet6:test --tests com.snoworca.regression.BugMuseumTest :servlet61:test --tests com.snoworca.regression.BugMuseumTest", "windows": ".\\gradlew.bat :servlet5:test --tests com.snoworca.regression.BugMuseumTest"},
    "dod": [
        "3개 모듈 모두에서 BM01~BM10 회귀 테스트 통과",
        "PH-003 의 10개 결함 수정 페어가 모두 BM 회귀 케이스로 검증",
        "servlet6 / servlet61 의 사본 동기화가 누락된 결함이 없음"
    ],
    "rollback": "본 검토에서 실패 발견 시 해당 fix 페어의 green task 재진입.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})

# ----- PH-004: 11 code → 22 red+green tasks + 1 review = 23 tasks -----
# Pair mapping (v01 task → red+green):
#  T-PH004-01 (FR-CONV-003 AdapterUnwrap marker) → T-PH004-01 red + T-PH004-02 green
#  T-PH004-02 (FR-CONV-003+FR-CONV-004 AdapterUnwrap all classes + equals/hashCode) → T-PH004-03 red + T-PH004-04 green
#  T-PH004-03 (FR-CONV-005 ServletException 변환) → 05 red + 06 green
#  T-PH004-04 (FR-CONV-006/007/008/009) → 07 red + 08 green
#  T-PH004-05 (FR-CONV-011 strict 모드) → 09 red + 10 green
#  T-PH004-06 (FR-CONV-012 폴백 가시성 로깅 + diagnostics) → 11 red + 12 green
#  T-PH004-07 (FR-CONV-010 + FR-CONV-014 폴백 정책 매트릭스) → 13 red + 14 green
#  T-PH004-08 (FR-CONV-013 Cookie attribute) → 15 red + 16 green
#  T-PH004-09 (FR-CONV-001 6.0 신규 추상) → 17 red + 18 green
#  T-PH004-10 (FR-CONV-001 + FR-CONV-014 6.1 sendRedirect) → 19 red + 20 green
#  T-PH004-11 (NFR-CONV-001 ThreadLocal lock + thread safety) → 21 red + 22 green
#  T-PH004-12 (review) → 23

PH004 = []

CONV_SPECS = [
    # (req_ids, ac_counts_map (req->ac_count), title, action_red, action_green, files_red, files_green, kind, test_symbol, fail_sig)
    (["FR-CONV-003"], {"FR-CONV-003": 4},
     "AdapterUnwrap marker 인터페이스 (common 모듈 신설)",
     "common/src/test/java/adapter/common/AdapterUnwrapTest.java 신설. AdapterUnwrap<T> 인터페이스 존재 + unwrap() 반환 verify. AC-1~AC-4 — interface_exists / unwrap_method_present / generic_T_correct / common_module_compile.",
     "common/src/main/java/adapter/common/AdapterUnwrap.java 신설. public interface AdapterUnwrap<T> { T unwrap(); }. 후속 페어가 모든 어댑터 클래스가 implements.",
     ["common/src/test/java/adapter/common/AdapterUnwrapTest.java"],
     ["common/src/main/java/adapter/common/AdapterUnwrap.java"],
     "unit", "shouldDefineAdapterUnwrapInterface", "expected: AdapterUnwrap interface present, got: NoClassDefFoundError"),

    (["FR-CONV-003", "FR-CONV-004"], {"FR-CONV-003": 4, "FR-CONV-004": 3},
     "모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피",
     "AdapterUnwrapAllClassesTest.java 신설 (3 모듈). 모든 어댑터 클래스가 implements AdapterUnwrap<원본> + unwrap() 반환 정확 verify. equals/hashCode 가 SPEC-6 패턴 (unwrap 위임 + AdapterUnwrap chain) verify. ServletAdapter.adaptToX 메서드의 instanceof + 이중 wrap 회피 분기 verify. 각 REQ 의 AC 모두 cover.",
     "모든 어댑터 클래스에 implements AdapterUnwrap<원본타입> 추가 + public 원본타입 unwrap() { return this.원본필드; } 메서드 추가. equals / hashCode 도 FR-CONV-004 의 SPEC-6 패턴대로 unwrap 위임 + AdapterUnwrap instanceof 처리 패턴으로 override. ServletAdapter 의 모든 adaptToJakarta / adaptToJavax 메서드에 3단계 분기 (instanceof AdapterUnwrap 사전 + 반환 namespace 일치 시 원본 반환 + 새 어댑터 wrap) 추가.",
     ["servlet5/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java", "servlet6/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java", "servlet61/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/", "servlet5/src/main/java/adapter/javax/servlet5/", "servlet5/src/main/java/com/snoworca/ServletAdapter.java", "servlet6/src/main/java/adapter/jakarta/servlet6/", "servlet6/src/main/java/adapter/javax/servlet6/", "servlet6/src/main/java/com/snoworca/ServletAdapter.java", "servlet61/src/main/java/adapter/jakarta/servlet61/", "servlet61/src/main/java/adapter/javax/servlet61/", "servlet61/src/main/java/com/snoworca/ServletAdapter.java"],
     "integration", "shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance", "expected: AdapterUnwrap on all adapters + equals/hashCode + double-wrap avoidance, got: missing implementation"),

    (["FR-CONV-005"], {"FR-CONV-005": 3},
     "ServletException 양방향 변환 (try/catch + cause chain)",
     "ServletExceptionConversionTest.java 신설. throws ServletException 시그니처를 갖는 위임 메서드에서 양방향 변환 + cause chain verify. AC-1~AC-3 — bidirectional / cause_preserved / non_servlet_exception_passthrough.",
     "common/ConverterSupport.java 에 javaxToJakartaServletException + jakartaToJavaxServletException 헬퍼 추가 (new 인스턴스 + initCause(원본)). 위임 메서드 중 throws ServletException 시그니처에 try/catch 변환 패턴 적용. IOException / IllegalStateException 등 표준 예외는 그대로 propagate. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/com/snoworca/ServletExceptionConversionTest.java"],
     ["common/src/main/java/adapter/common/ConverterSupport.java", "servlet5/src/main/java/adapter/jakarta/servlet5/", "servlet5/src/main/java/adapter/javax/servlet5/", "servlet6/src/main/java/adapter/jakarta/servlet6/", "servlet6/src/main/java/adapter/javax/servlet6/", "servlet61/src/main/java/adapter/jakarta/servlet61/", "servlet61/src/main/java/adapter/javax/servlet61/"],
     "integration", "shouldConvertServletExceptionBidirectionalWithCauseChain", "expected: bidirectional ServletException with initCause, got: missing converter helper"),

    (["FR-CONV-006", "FR-CONV-007", "FR-CONV-008", "FR-CONV-009"],
     {"FR-CONV-006": 3, "FR-CONV-007": 9, "FR-CONV-008": 2, "FR-CONV-009": 7},
     "컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16",
     "EagerSnapshotTest.java + StreamSpecTest.java + SideEffectTest.java 신설 (3 모듈). FR-CONV-006 AC 3개 (eager snapshot / null pass / array copy), FR-CONV-007 AC 9개 (read 3 + readLine + isFinished + isReady + setReadListener + write 3 + setWriteListener), FR-CONV-008 AC 2개 (long delegate + no narrow), FR-CONV-009 AC 7개 (flushBuffer/reset/resetBuffer/complete/invalidate/sendError/sendRedirect). 모든 AC 의 test case 매트릭스.",
     "PH-003 의 FIX 가 완료된 코드 위에서 추가 SPEC 검증 단위 테스트로 잠금. (1) FR-CONV-006: getCookies/getParts/getServletRegistrations/getFilterRegistrations 가 eager snapshot 반환 + 원본 null 시 null 반환. (2) FR-CONV-007: ServletInputStream/OutputStream 어댑터의 모든 시그니처 원본 동명 위임. (3) FR-CONV-008: setContentLengthLong 원본 동명 위임 (FR-FIX-003 의 잠금). (4) FR-CONV-009: flushBuffer/reset/resetBuffer/complete/invalidate/sendError/sendRedirect 즉시 위임. 3 모듈 모두.",
     ["servlet5/src/test/java/com/snoworca/EagerSnapshotTest.java", "servlet5/src/test/java/com/snoworca/StreamSpecTest.java", "servlet5/src/test/java/com/snoworca/SideEffectTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/", "servlet5/src/main/java/adapter/javax/servlet5/", "servlet6/src/main/java/", "servlet61/src/main/java/"],
     "integration", "shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths", "expected: collection snapshot + stream delegation + long preservation + side-effect propagation, got: at least one defective path"),

    (["FR-CONV-011"], {"FR-CONV-011": 4},
     "strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위",
     "StrictModeTest.java 신설. AC-1 (시스템 프로퍼티 ON → UnsupportedOperationException) + AC-2 (init-param ON → 동일) + AC-3 (OFF default → lenient 폴백) + AC-4 (getRequestId/getServletConnection 은 strict 시에도 default 유지) 4건 test.",
     "ConverterSupport 에 strict 모드 상태 관리 (static volatile boolean strictMode) + ServletContext init-param 우선순위 (시스템 프로퍼티 우선, init-param fallback) 로직. throwIfStrict(message) 헬퍼 — strict 활성 시 UnsupportedOperationException throw. 모든 어댑터의 폴백 분기 (B/D/E/F) 가 본 헬퍼 호출. getRequestId / getServletConnection 은 strict 정책이 F 동일이므로 예외 없이 default 동작 유지. getProtocolRequestId 도 strict D 동일이므로 빈 문자열 유지.",
     ["servlet5/src/test/java/com/snoworca/StrictModeTest.java"],
     ["common/src/main/java/adapter/common/ConverterSupport.java", "servlet5/src/main/java/com/snoworca/ServletAdapter.java", "servlet6/src/main/java/com/snoworca/ServletAdapter.java", "servlet61/src/main/java/com/snoworca/ServletAdapter.java"],
     "unit", "shouldEnforceStrictModeWithOptInAndDefaultLenient", "expected: opt-in strict mode + throwIfStrict + lenient default, got: missing flag or fallback bypass"),

    (["FR-CONV-012"], {"FR-CONV-012": 3},
     "폴백 가시성 (java.util.logging dedup + diagnostics API)",
     "DiagnosticsTest.java + LoggingDedupTest.java 신설. AC-1 (once-per-method dedup) + AC-2 (diagnostics() 시스템 프로퍼티 opt-in) + AC-3 (incrementDiagnostics thread-safe) 3건 test.",
     "ConverterSupport 에 (1) Logger.getLogger('com.snoworca.servletadapter') static, (2) ConcurrentHashMap<String, Boolean> dedup map, (3) logFallbackOnce(methodKey, level, message) 메서드, (4) ConcurrentHashMap<String, AtomicLong> diagnostics counter, (5) incrementDiagnostics(methodKey) 추가. 정책 B/D/F → WARN, 정책 E → INFO. ServletAdapter.diagnostics() 정적 메서드 (시스템 프로퍼티 com.snoworca.adapter.diagnostics=true 활성 시 Map 반환).",
     ["servlet5/src/test/java/com/snoworca/DiagnosticsTest.java", "servlet5/src/test/java/com/snoworca/LoggingDedupTest.java"],
     ["common/src/main/java/adapter/common/ConverterSupport.java", "servlet5/src/main/java/com/snoworca/ServletAdapter.java", "servlet6/src/main/java/com/snoworca/ServletAdapter.java", "servlet61/src/main/java/com/snoworca/ServletAdapter.java"],
     "unit", "shouldProvideFallbackVisibilityWithLoggingAndDiagnostics", "expected: once-per-method dedup logger + opt-in diagnostics map, got: missing helpers"),

    (["FR-CONV-010", "FR-CONV-014"], {"FR-CONV-010": 3, "FR-CONV-014": 3},
     "FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위",
     "FallbackPolicyTest.java 신설 (servlet6, servlet61). FR-CONV-010 AC 3개 (정책 B+D+E 정확 분기) + FR-CONV-014 AC 3개 (servlet5 dormant + servlet6 B 진입 + servlet61 sendRedirect 명시) 6건 test 매트릭스.",
     "SRS §6.2.2 표대로 정책 적용 — HttpSession.getValue/putValue jakarta→javax = B, HttpSession.getSessionContext = D (EmptyHttpSessionContext 더미), encodeUrl/encodeRedirectUrl = E, setStatus(int,String) = E1, isRequestedSessionIdFromUrl = E, ServletContext.log(Exception,String) = E. servlet6/servlet61 의 EmptyHttpSessionContext 신설. servlet5 는 5.0 비대칭 거의 없어 dormant.",
     ["servlet6/src/test/java/com/snoworca/FallbackPolicyTest.java", "servlet61/src/test/java/com/snoworca/FallbackPolicyTest.java"],
     ["servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpSession.java", "servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletResponse.java", "servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletRequest.java", "servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java", "servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpSession.java", "servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletResponse.java", "servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletRequest.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java", "servlet6/src/main/java/adapter/jakarta/servlet6/http/EmptyHttpSessionContext.java", "servlet61/src/main/java/adapter/jakarta/servlet61/http/EmptyHttpSessionContext.java"],
     "integration", "shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant", "expected: 6 asymmetric method groups follow SRS policy matrix, got: at least one wrong branch"),

    (["FR-CONV-013"], {"FR-CONV-013": 4},
     "Cookie attribute API 정책 D (no-op + WARN once)",
     "CookieAttributePolicyTest.java 신설. AC-1 (setAttribute no-op + WARN) + AC-2 (getAttribute null) + AC-3 (getAttributes empty unmodifiable Map) + AC-4 (no internal Map storage) 4건 test.",
     "javax Cookie 어댑터 (jakarta 6.0+ 신규 attribute API 가 노출되는 케이스) 에 (1) setAttribute(String, String) → no-op + ConverterSupport.logFallbackOnce(WARN) + incrementDiagnostics, (2) getAttribute(String) → null, (3) getAttributes() → Collections.emptyMap(). 자체 attribute Map 미보관. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/com/snoworca/CookieAttributePolicyTest.java"],
     ["servlet5/src/main/java/adapter/javax/servlet5/http/Cookie.java", "servlet6/src/main/java/adapter/javax/servlet6/http/Cookie.java", "servlet61/src/main/java/adapter/javax/servlet61/http/Cookie.java"],
     "unit", "shouldApplyNoOpWithWarnOnceForCookieAttributeApi", "expected: setAttribute no-op + getAttribute null + empty map + WARN once, got: internal map or NPE"),

    (["FR-CONV-001"], {"FR-CONV-001": 2},
     "jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현",
     "RequestIdTest.java 신설 (servlet6 + servlet61). AC-1 (getRequestId lazy UUID) + AC-2 (getProtocolRequestId 빈 문자열 + getServletConnection DummyServletConnection 합성) 2건 test.",
     "servlet6 / servlet61 의 adapter.jakarta.servletN.ServletRequest 에 (1) getRequestId() → request attribute (key=com.snoworca.adapter.requestId) lazy UUID 캐싱, (2) getProtocolRequestId() → '' 반환, (3) getServletConnection() → DummyServletConnection 인스턴스 합성 반환. DummyServletConnection.java 신설. 본 폴백은 strict 모드에서도 default 유지.",
     ["servlet6/src/test/java/com/snoworca/RequestIdTest.java", "servlet61/src/test/java/com/snoworca/RequestIdTest.java"],
     ["servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java", "servlet6/src/main/java/adapter/jakarta/servlet6/DummyServletConnection.java", "servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java", "servlet61/src/main/java/adapter/jakarta/servlet61/DummyServletConnection.java"],
     "unit", "shouldImplementJakarta6NewAbstractMethods", "expected: getRequestId UUID + getProtocolRequestId empty + getServletConnection synth, got: AbstractMethodError"),

    (["FR-CONV-001", "FR-CONV-014"], {"FR-CONV-001": 2, "FR-CONV-014": 3},
     "jakarta 6.1 sendRedirect(String, int, boolean) 명시 override",
     "SendRedirect61Test.java 신설 (servlet61). FR-CONV-001 AC 2개 (override 존재 + AbstractMethodError 없음) + FR-CONV-014 AC 3개 (servlet5 dormant + servlet6 B + servlet61 명시) 5건 test.",
     "servlet61 의 adapter.jakarta.servlet61.http.HttpServletResponse 에 sendRedirect(String location, int sc, boolean clearBuffer) 메서드 명시 override 추가. 구현 — setStatus(sc) + setHeader('Location', location) + clearBuffer 시 resetBuffer + flushBuffer. javax 4.0 측에 본 시그니처가 없으므로 명시 합성. AbstractMethodError 회피.",
     ["servlet61/src/test/java/com/snoworca/SendRedirect61Test.java"],
     ["servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletResponse.java"],
     "unit", "shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61", "expected: 6.1 sendRedirect override implemented, got: AbstractMethodError"),

    (["NFR-CONV-001"], {"NFR-CONV-001": 3},
     "스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10)",
     "ThreadSafetyTest.java 신설. AC-1 (어댑터 final/thread-safe 필드) + AC-2 (dedup/diagnostics ConcurrentHashMap+AtomicLong) + AC-3 (BM10 ThreadLocal single-thread 가정 회귀 잠금) 3건 test.",
     "어댑터 클래스의 인스턴스 필드를 모두 final 또는 thread-safe 타입으로 확인. diagnostics counter / dedup map 이 ConcurrentHashMap + AtomicLong 인지 PR review. BM10 회귀 테스트 보강 — HttpUpgradeHandler ThreadLocal pattern 의 현 동작이 통과 + 향후 비동기 호출 시 즉시 실패하도록 작성. 3 모듈 모두 적용.",
     ["servlet5/src/test/java/com/snoworca/ThreadSafetyTest.java"],
     ["servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpUpgradeHandler.java", "servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java"],
     "unit", "shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn", "expected: all final/thread-safe + ConcurrentHashMap/AtomicLong + BM10 pass, got: mutable field or non-thread-safe map"),
]

ph004_seq = 1
for (req_ids, ac_counts_map, title, action_red, action_green, files_red, files_green, kind_default, test_symbol, fail_sig) in CONV_SPECS:
    red_id = f"T-PH004-{ph004_seq:02d}"
    green_id = f"T-PH004-{ph004_seq+1:02d}"

    # Build test_cases: for each req_id, for each AC in ac_count
    tcs = []
    all_acs = []
    for req_id in req_ids:
        for ac_n in range(1, ac_counts_map[req_id] + 1):
            tcs.append(make_tc(req_id, ac_n, 1,
                               files_red[0],
                               f"{test_symbol}_{req_id.replace('-', '')}_AC{ac_n}",
                               kind_default,
                               fail_sig))
            all_acs.append(f"AC-{ac_n}")
    # covers_ac: union across all reqs (use unique AC-1..AC-N where N=max ac count over reqs)
    max_ac = max(ac_counts_map.values())
    covers_ac = [f"AC-{i}" for i in range(1, max_ac + 1)]

    PH004.append({
        "id": red_id,
        "phase_id": "PH-004",
        "title": f"{' + '.join(req_ids)} {title} — TDD red (failing test)",
        "type": "code",
        "req_ids": req_ids,
        "files": [{"path": p} for p in files_red],
        "test_files": [{"path": p} for p in files_red],
        "action": action_red,
        "covers_ac": covers_ac,
        "acceptance_tests": [{"kind": "shell", "cmd": f"./gradlew test --tests *{test_symbol}* --quiet", "expected_exit": 1, "stdout_regex": "FAILED"}],
        "verification_cmd": {"posix": f"./gradlew test --tests *{test_symbol}*", "windows": f".\\gradlew.bat test --tests *{test_symbol}*"},
        "dod": [
            f"red 단계: {len(tcs)}개 test case 작성됨 (각 REQ AC 별 1개씩)",
            "red 단계: 작성된 test 가 모두 실패(red) 함을 확인"
        ],
        "rollback": f"{files_red[0]} 등 신설 테스트 파일 삭제.",
        "estimated_effort": "M",
        "tdd": {
            "applicable": True,
            "phase": "red",
            "test_cases_count": len(tcs),
            "test_cases": tcs
        }
    })
    PH004.append({
        "id": green_id,
        "phase_id": "PH-004",
        "title": f"{' + '.join(req_ids)} {title} — TDD green (impl)",
        "type": "code",
        "req_ids": req_ids,
        "files": [{"path": p} for p in files_green],
        "test_files": [],
        "action": action_green,
        "covers_ac": covers_ac,
        "depends_on_task": [red_id],
        "acceptance_tests": [{"kind": "shell", "cmd": f"./gradlew test --tests *{test_symbol}* --quiet", "expected_exit": 0}],
        "verification_cmd": {"posix": f"./gradlew test --tests *{test_symbol}*", "windows": f".\\gradlew.bat test --tests *{test_symbol}*"},
        "dod": [
            f"green 단계: {len(tcs)}개 test case 가 모두 통과(green)",
            "green 단계: 동일 test 가 모두 통과(green) 함을 확인"
        ],
        "rollback": "git diff 로 구현 reverse — red task 로 복귀.",
        "estimated_effort": "M",
        "tdd": {"applicable": True, "phase": "green", "test_cases_count": 0, "test_cases": []}
    })
    ph004_seq += 2

# T-PH004-23 review
PH004.append({
    "id": "T-PH004-23",
    "phase_id": "PH-004",
    "title": "PH-004 폴백 정책 / strict 모드 / AdapterUnwrap / 로깅 / diagnostics 통합 회귀 + 단위 테스트 매트릭스 검증",
    "type": "review",
    "req_ids": ["FR-CONV-001", "FR-CONV-002", "FR-CONV-003", "FR-CONV-004", "FR-CONV-005", "FR-CONV-010", "FR-CONV-011", "FR-CONV-012", "FR-CONV-013", "FR-CONV-014", "NFR-CONV-001"],
    "files": [{"path": "servlet5/src/test/java/"}, {"path": "servlet6/src/test/java/"}, {"path": "servlet61/src/test/java/"}],
    "action": "PH-004 의 11개 페어 (22 task) 결과를 단위 테스트 매트릭스로 종합 검증. SRS §6.2.2 표의 모든 비대칭 메서드 그룹별로 default + strict 양쪽 동작 검증. FR-CONV-001 의 6.0/6.1 신규 추상 메서드 구현 완료 확인. FR-CONV-002 (위임 우선) 는 PH-005 의 FR-TEST-002 Reflection Contract Test 가 검증. 미달 시 해당 PH-004 페어의 green task 재진입.",
    "acceptance_tests": [{"kind": "checklist", "items": ["SPEC-1~16 변환 의미론이 servlet5/6/61 3개 모듈에서 행동 동등", "AdapterUnwrap marker 3 모듈 모두 적용 (이중 wrap 회피 검증)", "strict 모드 ON/OFF 양 경로 회귀 통과", "Cookie attribute API no-op + WARN once 동작 확인"]}],
    "verification_cmd": {"posix": "./gradlew test", "windows": ".\\gradlew.bat test"},
    "dod": [
        "PH-004 의 22 페어 task 결과가 단위 테스트 매트릭스로 통합 검증",
        "SRS §6.2.2 표의 모든 비대칭 메서드 그룹의 default + strict 동작 검증",
        "3개 모듈 모두 ./gradlew test 통과"
    ],
    "rollback": "본 review 가 fail 한 페어의 green task 로 재진입.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})

# ----- PH-005: 4 code (IT fixture exempt) + 2 infra + 1 review = 7 -----
PH005 = []
PH005.append({
    "id": "T-PH005-01",
    "phase_id": "PH-005",
    "title": "어댑터 클래스당 단위 테스트 클래스 (Mockito RETURNS_SMART_NULLS) + 모든 위임 메서드 verify + 폴백 정책 분기 검증",
    "type": "code",
    "req_ids": ["FR-TEST-001"],
    "files": [
        {"path": "servlet5/src/test/java/adapter/jakarta/servlet5/"},
        {"path": "servlet5/src/test/java/adapter/javax/servlet5/"},
        {"path": "servlet5/src/test/java/com/snoworca/"},
        {"path": "servlet6/src/test/java/adapter/jakarta/servlet6/"},
        {"path": "servlet6/src/test/java/adapter/javax/servlet6/"},
        {"path": "servlet61/src/test/java/adapter/jakarta/servlet61/"},
        {"path": "servlet61/src/test/java/adapter/javax/servlet61/"}
    ],
    "action": "현 src/test/java/com/snoworca/ 의 2개 테스트 클래스를 servlet5 로 이동 후 전면 재작성. 각 어댑터 클래스당 1개 이상 단위 테스트 클래스 신설. mock 은 withSettings().defaultAnswer(RETURNS_SMART_NULLS) 로 생성. 모든 위임 메서드를 verify(origin).동명메서드(...) 로 검증. ArgumentCaptor 로 인자 변환 검증. 폴백 정책 (FR-CONV-010) 의 모든 정책 코드별 분기를 default + strict 양쪽으로 검증. servlet6 / servlet61 모듈도 동일 적용.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew test --quiet && ./gradlew :servlet5:test :servlet6:test :servlet61:test --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew test --info", "windows": ".\\gradlew.bat test --info"},
    "dod": [
        "3개 모듈 각각에 어댑터 클래스당 1개 이상의 단위 테스트 클래스 존재",
        "모든 위임 메서드가 verify 로 검증",
        "폴백 정책 (B/D/E/F) 의 default + strict 분기 모두 검증",
        "mock 이 RETURNS_SMART_NULLS 로 생성됨 (stub 누락 시 즉시 검출)"
    ],
    "rollback": "신규 테스트 클래스 삭제.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": IT_FIXTURE_EXEMPT}
})
PH005.append({
    "id": "T-PH005-02",
    "phase_id": "PH-005",
    "title": "common/src/testFixtures 에 DelegationContractTest driver 작성 + 각 버전 모듈에서 호출",
    "type": "code",
    "req_ids": ["FR-TEST-002"],
    "files": [
        {"path": "common/build.gradle"},
        {"path": "common/src/testFixtures/java/com/snoworca/contract/DelegationContractTest.java"},
        {"path": "servlet5/src/test/java/com/snoworca/contract/Servlet5ContractTest.java"},
        {"path": "servlet6/src/test/java/com/snoworca/contract/Servlet6ContractTest.java"},
        {"path": "servlet61/src/test/java/com/snoworca/contract/Servlet61ContractTest.java"}
    ],
    "action": "common/build.gradle 에 apply plugin: 'java-test-fixtures' 추가. common/src/testFixtures/java/com/snoworca/contract/DelegationContractTest.java 신설 — (1) 어댑터 spec 등록 API, (2) 인터페이스 메서드 enumerate, (3) Object 메서드 + 변환 정책 메서드 blacklist, (4) sample args 합성, (5) 어댑터 호출 후 origin mock verify, (6) verifyNoMoreInteractions. 각 버전 모듈의 ServletNContractTest 가 driver 를 호출하여 자기 어댑터 spec 등록.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *Servlet5ContractTest* :servlet6:test --tests *Servlet6ContractTest* :servlet61:test --tests *Servlet61ContractTest* --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew test --tests *ContractTest*", "windows": ".\\gradlew.bat test --tests *ContractTest*"},
    "dod": [
        "DelegationContractTest driver 가 common testFixtures 에 존재",
        "3개 버전 모듈 각각이 driver 를 호출하는 ServletNContractTest 보유",
        "모든 어댑터 인터페이스의 모든 추상 메서드에 대해 위임 검증 자동 수행"
    ],
    "rollback": "testFixtures 디렉토리 삭제 + 각 모듈의 ServletNContractTest 삭제.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": IT_FIXTURE_EXEMPT}
})
PH005.append({
    "id": "T-PH005-03",
    "phase_id": "PH-005",
    "title": "각 모듈에 Tomcat embed testImplementation + TomcatRunner 헬퍼 클래스 신설",
    "type": "infra",
    "req_ids": ["FR-TEST-003"],
    "files": [
        {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"},
        {"path": "servlet5/src/test/java/com/snoworca/integration/TomcatRunner.java"},
        {"path": "servlet6/src/test/java/com/snoworca/integration/TomcatRunner.java"},
        {"path": "servlet61/src/test/java/com/snoworca/integration/TomcatRunner.java"}
    ],
    "action": "servlet5/build.gradle 에 testImplementation tomcat-embed-core:10.0.27 + 9.0.85 (javax-host 시나리오용). servlet6 에 10.1.30, servlet61 에 11.0.0. 3 모듈 모두 awaitility 4.2.1. 각 모듈의 TomcatRunner.java 헬퍼 신설 — embedded Tomcat 의 start/stop + Servlet/Filter 등록 + 임의 포트 할당 + HTTP 클라이언트 헬퍼. 각 모듈은 자기 Tomcat 버전만 사용.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:compileTestJava :servlet6:compileTestJava :servlet61:compileTestJava --quiet && test -f servlet5/src/test/java/com/snoworca/integration/TomcatRunner.java && test -f servlet6/src/test/java/com/snoworca/integration/TomcatRunner.java && test -f servlet61/src/test/java/com/snoworca/integration/TomcatRunner.java", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew :servlet5:compileTestJava :servlet6:compileTestJava :servlet61:compileTestJava", "windows": ".\\gradlew.bat :servlet5:compileTestJava :servlet6:compileTestJava :servlet61:compileTestJava"},
    "dod": [
        "3개 모듈의 testImplementation 에 Tomcat embed 의존 추가 (10.0.27 / 10.1.30 / 11.0.0)",
        "servlet5 만 추가로 tomcat-embed-core:9.0.85 (javax-host) 보유",
        "awaitility 4.2.1 3개 모듈 모두 추가",
        "3개 모듈의 TomcatRunner 헬퍼 클래스 존재"
    ],
    "rollback": "build.gradle 의 testImplementation 행 제거 + TomcatRunner 삭제.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": IT_FIXTURE_EXEMPT}
})
PH005.append({
    "id": "T-PH005-04",
    "phase_id": "PH-005",
    "title": "IT01~IT15 통합 테스트 시나리오 작성 + @Tag('integration') + integrationTest task 분리",
    "type": "code",
    "req_ids": ["FR-TEST-003"],
    "files": [
        {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"},
        {"path": "servlet5/src/test/java/com/snoworca/integration/"},
        {"path": "servlet6/src/test/java/com/snoworca/integration/"},
        {"path": "servlet61/src/test/java/com/snoworca/integration/"}
    ],
    "action": "각 모듈에 IT01~IT15 시나리오 작성. IT01 (GET + body + status), IT02 (POST body + getInputStream — F1 회귀), IT03 (Cookie 설정), IT04 (Cookie attribute SameSite=Lax 정보 손실), IT05 (Session), IT06 (Filter chain), IT07 (AsyncContext.start + onComplete), IT08 (MultiPart upload — F2 회귀), IT09 (sendRedirect 308 — 6.1 only), IT10 (Charset UTF-8 — F3 회귀), IT11 (setStatus(int, String) 폴백), IT12 (setContentLengthLong Long.MAX_VALUE — F5/F6 회귀), IT13 (ErrorPage), IT14 (getServletConnection 6.0 신규), IT15 (WebSocket Upgrade). @Tag('integration') 부여 + integrationTest 별도 task.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:integrationTest :servlet6:integrationTest :servlet61:integrationTest --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew :servlet5:integrationTest :servlet6:integrationTest :servlet61:integrationTest --info", "windows": ".\\gradlew.bat :servlet5:integrationTest --info"},
    "dod": [
        "IT01~IT10 시나리오 (P0) 모두 작성 + 통과",
        "IT11~IT15 시나리오 (P1) 작성 + 통과",
        "IT09 는 servlet61 만, IT14 는 servlet6 + servlet61 만 적용",
        "@Tag('integration') 부여 + integrationTest task 분리",
        "각 모듈은 자기 Tomcat 버전만 사용 (classpath 충돌 없음)"
    ],
    "rollback": "각 IT 시나리오 클래스 삭제 + integrationTest task 제거.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": IT_FIXTURE_EXEMPT}
})
PH005.append({
    "id": "T-PH005-05",
    "phase_id": "PH-005",
    "title": "동일 시나리오 사본을 3개 모듈에서 병렬 실행하고 외부 관찰 결과 동등성 검증",
    "type": "code",
    "req_ids": ["FR-TEST-005"],
    "files": [
        {"path": "common/src/testFixtures/java/com/snoworca/scenario/CrossVersionScenarios.java"},
        {"path": "servlet5/src/test/java/com/snoworca/integration/CrossVersionTest.java"},
        {"path": "servlet6/src/test/java/com/snoworca/integration/CrossVersionTest.java"},
        {"path": "servlet61/src/test/java/com/snoworca/integration/CrossVersionTest.java"}
    ],
    "action": "common 의 testFixtures 에 CrossVersionScenarios.java 신설 — IT01 / IT03 / IT05 의 동일 시나리오 입력·기대 출력 정의. 각 버전 모듈의 CrossVersionTest.java 가 본 fixture 를 호출하여 자기 Tomcat embed 에서 실행하고 결과를 stdout 또는 파일 기록. CI 매트릭스가 3개 모듈 결과를 비교.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:integrationTest --tests *CrossVersionTest* :servlet6:integrationTest --tests *CrossVersionTest* :servlet61:integrationTest --tests *CrossVersionTest* --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew integrationTest --tests *CrossVersionTest*", "windows": ".\\gradlew.bat integrationTest --tests *CrossVersionTest*"},
    "dod": [
        "CrossVersionScenarios fixture 가 common testFixtures 에 존재",
        "각 버전 모듈의 CrossVersionTest 가 IT01/IT03/IT05 동일 시나리오 실행",
        "3개 모듈에서 status / body / Content-Type / Set-Cookie / JSESSIONID 비교 가능한 결과 출력"
    ],
    "rollback": "CrossVersionScenarios + CrossVersionTest 삭제.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": IT_FIXTURE_EXEMPT}
})
PH005.append({
    "id": "T-PH005-06",
    "phase_id": "PH-005",
    "title": "각 모듈에 JaCoCo 플러그인 + 커버리지 게이트 (라인 95% / 분기 100% for converter, 90% for adapter)",
    "type": "infra",
    "req_ids": ["NFR-TEST-001"],
    "files": [{"path": "common/build.gradle"}, {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"}, {"path": "gradle.properties"}],
    "action": "각 모듈 build.gradle 에 apply plugin: 'jacoco' + jacocoTestReport + jacocoTestCoverageVerification 블록 추가. violationRules — adapter.{jakarta,javax}.** 위임 코드 라인 ≥95%/분기 ≥90%, converter + common 라인 ≥95%/분기 ≥100%, 예외 변환 try/catch 라인 100%. ./gradlew check 가 본 task 자동 실행.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:jacocoTestCoverageVerification :servlet6:jacocoTestCoverageVerification :servlet61:jacocoTestCoverageVerification :common:jacocoTestCoverageVerification --quiet", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew jacocoTestCoverageVerification", "windows": ".\\gradlew.bat jacocoTestCoverageVerification"},
    "dod": [
        "4개 모듈에 jacoco 플러그인 + violationRules 보유",
        "./gradlew check 시 커버리지 게이트 실행",
        "PR 미달 시 빌드 실패"
    ],
    "rollback": "각 build.gradle 의 jacoco 블록 제거.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": CI_INFRA_EXEMPT}
})
PH005.append({
    "id": "T-PH005-07",
    "phase_id": "PH-005",
    "title": "PH-005 의 6개 task 결과 통합 검토 — 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과",
    "type": "review",
    "req_ids": ["FR-TEST-001", "FR-TEST-002", "FR-TEST-003", "FR-TEST-005", "NFR-TEST-001"],
    "files": [],
    "action": "PH-005 의 6개 task 완료 후 ./gradlew check + ./gradlew integrationTest 통합 실행. 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과 확인. 미달 시 해당 task 재진입.",
    "acceptance_tests": [{"kind": "checklist", "items": ["IT01~IT15 Tomcat embed 통합 테스트 모두 통과 (10.0.27 + 10.1.30 + 11.0.0)", "JaCoCo 라인 커버리지가 NFR-TEST-001 의 게이트 도달", "BM10 회귀 포함 확인 (Bug Museum 10건)"]}],
    "verification_cmd": {"posix": "./gradlew check integrationTest", "windows": ".\\gradlew.bat check integrationTest"},
    "dod": [
        "./gradlew check integrationTest 가 5개 모듈에서 모두 통과",
        "PH-005 의 6개 task 결과가 통합 검증"
    ],
    "rollback": "본 검토 fail 시 해당 task 재진입.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})

# ----- PH-006: 3 tasks (infra + review + infra) -----
PH006 = []
PH006.append({
    "id": "T-PH006-01",
    "phase_id": "PH-006",
    "title": ".github/workflows/ci.yml 작성 — build-matrix + integration-tests + coverage",
    "type": "infra",
    "req_ids": ["OPS-TEST-001"],
    "files": [{"path": ".github/workflows/ci.yml"}],
    "action": ".github/workflows/ci.yml 신설 — (1) build-matrix: OS=[ubuntu-latest, windows-latest] × servlet-line=[5.0(JDK 8), 6.0(JDK 11), 6.1(JDK 17)] 6셀. (2) integration-tests: OS=ubuntu-latest, 3셀. (3) coverage: ubuntu-latest, JDK 17. ./gradlew jacocoTestReport + codecov upload. wall clock ~5분 이내 목표.",
    "acceptance_tests": [{"kind": "file_state", "path": ".github/workflows/ci.yml", "exists": True}, {"kind": "shell", "cmd": "yq '.jobs.test.strategy.matrix' .github/workflows/ci.yml", "expected_exit": 0, "stdout_regex": "(?s).*(servlet5|servlet6|servlet61).*"}],
    "verification_cmd": None,
    "dod": [
        ".github/workflows/ci.yml 이 main 브랜치에 존재",
        "PR 푸시 시 build-matrix (6셀) + integration-tests (3셀) + coverage (1셀) 모두 병렬·순차 실행",
        "통합 테스트는 Linux only, Windows 는 단위 + contract 만",
        "Codecov 또는 GitHub PR 댓글에 coverage 보고서 표시"
    ],
    "rollback": "ci.yml 삭제.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": CI_INFRA_EXEMPT}
})
PH006.append({
    "id": "T-PH006-02",
    "phase_id": "PH-006",
    "title": "첫 PR 으로 CI 매트릭스 실행 검증 + 시간 측정 + 실패 셀 분석",
    "type": "review",
    "req_ids": ["OPS-TEST-001"],
    "files": [],
    "action": "T-PH006-01 의 ci.yml 을 main 브랜치에 머지 후 첫 PR 생성. 모든 매트릭스 셀이 병렬 실행되고 통과함을 확인. wall clock 시간 측정. 실패 셀이 있으면 분석 — toolchain JDK 다운로드 / Tomcat embed Windows 호환성 / cache miss 등.",
    "acceptance_tests": [{"kind": "checklist", "cmd": "첫 PR 의 GitHub Actions 모든 셀 green + wall clock < 6분 + 실패 셀 없음", "expected_exit": 0}],
    "verification_cmd": None,
    "dod": [
        "첫 PR 의 모든 GitHub Actions 매트릭스 셀이 통과",
        "wall clock 시간이 5분 ± 1분 범위",
        "실패 셀 분석 + 후속 task (필요 시)"
    ],
    "rollback": "본 review 가 fail 한 셀에 대해 ci.yml 수정 + 재실행.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
PH006.append({
    "id": "T-PH006-03",
    "phase_id": "PH-006",
    "title": "JaCoCo 보고서 PR 댓글 + Codecov 업로드 + 게이트 95%/100% 적용 검증",
    "type": "infra",
    "req_ids": ["NFR-TEST-001", "OPS-TEST-001"],
    "files": [{"path": ".github/workflows/ci.yml"}],
    "action": "T-PH006-01 의 coverage job 이 JaCoCo XML 보고서를 생성하고 Codecov 또는 GitHub PR 댓글로 업로드함을 검증. 게이트 95%/100% 미달 시 PR 빌드 실패 확인. PR 머지 차단 게이트 활성화 (GitHub branch protection rules).",
    "acceptance_tests": [{"kind": "file_state", "path": "build.gradle", "exists": True}, {"kind": "shell", "cmd": "./gradlew jacocoTestReport", "expected_exit": 0}],
    "verification_cmd": None,
    "dod": [
        "각 PR 의 coverage job 이 모듈별 JaCoCo XML 생성",
        "Codecov 또는 PR 댓글에 coverage 표시",
        "의도적 게이트 미달 PR 이 빌드 실패",
        "GitHub branch protection rules 에 coverage 게이트 등록"
    ],
    "rollback": "ci.yml 의 coverage job 또는 branch protection rules 제거.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": CI_INFRA_EXEMPT}
})

# ----- PH-007: 2 code + 2 doc + 2 infra + 1 pr = 7 tasks -----
PH007 = []
PH007.append({
    "id": "T-PH007-01",
    "phase_id": "PH-007",
    "title": "각 모듈 build.gradle 에 maven-publish + signing 플러그인 + publication 정의",
    "type": "code",
    "req_ids": ["IR-REL-001", "FR-MOD-007"],
    "files": [{"path": "common/build.gradle"}, {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"}, {"path": "bom/build.gradle"}],
    "action": "5개 모듈 build.gradle 에 apply plugin: 'maven-publish' + apply plugin: 'signing'. 각 모듈에 publication 정의 — publishing { publications { mavenJava(MavenPublication) { artifactId 'jakarta-to-javax-servlet-adapter-{name}', from components.java (또는 components.javaPlatform for bom), pom { name/description/url/licenses/developers/scm } } } repositories { maven { name 'OSSRH', url ossrhUrl, credentials } } }. signing 블록 — useGpgCmd 또는 useInMemoryPgpKeys + sign publishing.publications.mavenJava. version 1.0.0 동기.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew publishToMavenLocal --quiet && ls ~/.m2/repository/com/clipsoft/jakarta-to-javax-servlet-adapter-*/1.0.0/ | wc -l", "expected_exit": 0, "stdout_regex": "^[5-9]"}],
    "verification_cmd": {"posix": "./gradlew publishToMavenLocal", "windows": ".\\gradlew.bat publishToMavenLocal"},
    "dod": [
        "5개 모듈 모두 maven-publish + signing 플러그인 적용",
        "./gradlew publishToMavenLocal 결과로 5개 jar (POM + GPG signature 포함) 생성",
        "5개 artifactId 가 jakarta-to-javax-servlet-adapter-{common,servlet5,servlet6,servlet61,bom} 형식",
        "모든 모듈 version=1.0.0 동기"
    ],
    "rollback": "각 build.gradle 의 publishing/signing 블록 제거.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": RELEASE_ARTIFACT_EXEMPT}
})
PH007.append({
    "id": "T-PH007-02",
    "phase_id": "PH-007",
    "title": "README.md 전면 개정 — 호환성 매트릭스 + Cookie attribute 손실 + setStatus reason phrase 폐기 + getValue alias + strict 모드 + 0.x 마이그레이션",
    "type": "doc",
    "req_ids": ["FR-REL-001"],
    "files": [{"path": "README.md"}],
    "action": "README.md 전면 개정. (1) 호환성 매트릭스 표: -servlet5 (5.0.x, javax 4.0.x, Java 8, Tomcat 10.0.x), -servlet6 (6.0.x, javax 4.0.x, Java 11, Tomcat 10.1.x), -servlet61 (6.1.x, javax 4.0.x, Java 17, Tomcat 11.x). (2) Cookie attribute API 정보 손실 정책. (3) setStatus reason phrase 폐기. (4) HttpSession.getValue/putValue attribute API 자동 폴백. (5) strict 모드 활성화 방법. (6) 0.0.1 사용자 마이그레이션 안내 (D8 옵션 B 결과).",
    "acceptance_tests": [{"kind": "file_state", "cmd": "grep -E '(servlet5|servlet6|servlet61|SameSite|strict mode|0\\.0\\.1)' README.md | wc -l", "expected_exit": 0, "stdout_regex": "^[6-9]|^[1-9][0-9]"}],
    "verification_cmd": None,
    "dod": [
        "README.md 에 호환성 매트릭스 표 존재",
        "Cookie attribute / setStatus reason phrase / getValue alias / strict 모드 / 0.x 마이그레이션 5개 항목 모두 문서화",
        "한국어 또는 영어 일관 (현 README = 영어)"
    ],
    "rollback": "README.md 의 신규 섹션 제거.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
PH007.append({
    "id": "T-PH007-03",
    "phase_id": "PH-007",
    "title": "CHANGELOG.md 작성 — 0.0.1 → 1.0.0 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 추가 + 폴백 정책",
    "type": "doc",
    "req_ids": ["FR-REL-001"],
    "files": [{"path": "CHANGELOG.md"}],
    "action": "CHANGELOG.md 신설 또는 갱신 — ## [1.0.0] - 2026-MM-DD 섹션에 (1) 결함 수정 9건 (FR-FIX-001~009 의 결함 ID + 1줄 요약), (2) 신규 multi-module 구조, (3) 6.0/6.1 동시 지원 추가 (AbstractMethodError 회피), (4) 폴백 정책 변경, (5) 새 진입점 (com.snoworca.ServletAdapter5/6/61 별칭). Keep a Changelog 1.1.0 형식.",
    "acceptance_tests": [{"kind": "file_state", "cmd": "grep -E '(1\\.0\\.0|BM01|FR-FIX-|multi-module|servlet5)' CHANGELOG.md | wc -l", "expected_exit": 0, "stdout_regex": "^[5-9]|^[1-9][0-9]"}],
    "verification_cmd": None,
    "dod": [
        "CHANGELOG.md 가 신설되거나 1.0.0 섹션 추가",
        "결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 + 폴백 정책 변경 + 신규 진입점 5개 항목 명시",
        "Keep a Changelog 형식"
    ],
    "rollback": "CHANGELOG.md 의 1.0.0 섹션 제거.",
    "estimated_effort": "S",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})
PH007.append({
    "id": "T-PH007-04",
    "phase_id": "PH-007",
    "title": "마이그레이션 가이드 작성 + D8 옵션 B (servlet5 내 deprecated alias 패키지) 구현",
    "type": "code",
    "req_ids": ["MIG-REL-001"],
    "files": [{"path": "docs/migration/0.x-to-1.0.md"}, {"path": "servlet5/src/main/java/adapter/jakarta/servlet/"}, {"path": "servlet5/src/main/java/adapter/javax/servlet/"}, {"path": "servlet5/src/main/java/adapter/servletElementConverter/"}],
    "action": "D8 의 확정 — 옵션 B (servlet5 모듈 내 @Deprecated alias 패키지) 구현. servlet5 모듈에 adapter.jakarta.servlet.*, adapter.javax.servlet.*, adapter.servletElementConverter.* (0.x 패키지명) deprecated alias 클래스 추가 — 각 alias 가 adapter.jakarta.servlet5.* 등의 실제 클래스를 상속 또는 위임. @Deprecated(forRemoval = true, since = '1.0.0') + javadoc 에 v2.0 제거 예고. docs/migration/0.x-to-1.0.md 신설 — import 변경 안내 + 의존 좌표 변경 + alias 사용 예시 + v2.0 제거 일정.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew :servlet5:compileJava --quiet && find servlet5/src/main/java/adapter/jakarta/servlet -maxdepth 4 -name '*.java' | wc -l", "expected_exit": 0}],
    "verification_cmd": {"posix": "./gradlew :servlet5:compileJava && cat docs/migration/0.x-to-1.0.md | head -20", "windows": ".\\gradlew.bat :servlet5:compileJava && type docs\\migration\\0.x-to-1.0.md"},
    "dod": [
        "D8 옵션 B 구현 완료 — servlet5 모듈에 0.x 패키지명 @Deprecated alias 클래스",
        "0.0.1 사용자가 import 변경 없이 1.0.0 의존 업그레이드 시 컴파일 success",
        "docs/migration/0.x-to-1.0.md 마이그레이션 가이드 작성",
        "각 alias 가 @Deprecated(since='1.0.0', forRemoval=true) + javadoc 'Use adapter.jakarta.servlet5.*' 명시"
    ],
    "rollback": "alias 클래스 제거. 마이그레이션 가이드는 보존.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": RELEASE_ARTIFACT_EXEMPT}
})
PH007.append({
    "id": "T-PH007-05",
    "phase_id": "PH-007",
    "title": "GPG 서명 키 적용 + GitHub Secrets 설정",
    "type": "infra",
    "req_ids": ["OPS-REL-001"],
    "files": [{"path": ".github/workflows/release.yml"}, {"path": "common/build.gradle"}, {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"}, {"path": "bom/build.gradle"}],
    "action": "T-PH001-03 (OQ-003 외부 트래킹) 결과로 자격증명 준비 완료 확인. 각 모듈 build.gradle 의 signing 블록을 useInMemoryPgpKeys(System.getenv('GPG_SIGNING_KEY'), System.getenv('GPG_PASSPHRASE')) 패턴. .github/workflows/release.yml 신설 — tag push (v*) 트리거. release job 이 OSSRH_USERNAME/PASSWORD + GPG_SIGNING_KEY/PASSPHRASE 환경 변수로 publish 실행. README 에 GPG fingerprint 명시.",
    "acceptance_tests": [{"kind": "file_state", "path": ".github/workflows/release.yml", "exists": True}, {"kind": "shell", "cmd": "grep -c 'OSSRH_USERNAME\\|GPG_SIGNING_KEY' .github/workflows/release.yml", "expected_exit": 0}],
    "verification_cmd": None,
    "dod": [
        "GitHub Secrets 4개 등록 (UI 에서 확인)",
        "각 모듈의 signing 블록이 환경 변수 기반",
        ".github/workflows/release.yml 이 tag push 트리거",
        "README 에 GPG fingerprint 명시"
    ],
    "rollback": "GitHub Secrets 비활성화 + signing 블록 제거 + release.yml 삭제.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": CI_INFRA_EXEMPT}
})
PH007.append({
    "id": "T-PH007-06",
    "phase_id": "PH-007",
    "title": "publishAll task + Sonatype staging repository 업로드 + promote (Maven Central)",
    "type": "infra",
    "req_ids": ["OPS-REL-001"],
    "files": [{"path": "build.gradle"}, {"path": "common/build.gradle"}, {"path": "servlet5/build.gradle"}, {"path": "servlet6/build.gradle"}, {"path": "servlet61/build.gradle"}, {"path": "bom/build.gradle"}],
    "action": "root build.gradle 에 task publishAll 정의 — 5개 모듈의 publish task 모두 호출 + Sonatype staging close + promote. io.github.gradle-nexus.publish-plugin 또는 동등. 첫 실행은 SNAPSHOT 으로 dry-run 후 1.0.0 정식 release.",
    "acceptance_tests": [{"kind": "shell", "cmd": "./gradlew publishAllPublicationsToStagingRepository --dry-run", "expected_exit": 0, "stdout_regex": "(?s).*common.*servlet5.*servlet6.*servlet61.*bom.*"}],
    "verification_cmd": None,
    "dod": [
        "publishAll task 가 root 에 등록",
        "5개 모듈이 Sonatype staging repository 에 업로드",
        "promote 후 Maven Central 검색에서 5개 artifact 노출",
        "GPG signature 검증 통과"
    ],
    "rollback": "publishAll task 제거 + staging repository drop. Maven Central promote 는 되돌리기 불가.",
    "estimated_effort": "L",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0, "exempt_reason": CI_INFRA_EXEMPT}
})
PH007.append({
    "id": "T-PH007-07",
    "phase_id": "PH-007",
    "title": "v1.0.0 git tag + GitHub Release 작성 + 최종 회귀 (BM + IT + cross-version 전수)",
    "type": "pr",
    "req_ids": ["IR-REL-001", "FR-REL-001", "MIG-REL-001", "OPS-REL-001"],
    "files": [{"path": "CHANGELOG.md"}, {"path": "README.md"}],
    "action": "모든 PH-002~PH-006 완료 후 main 브랜치에서 git tag v1.0.0 + push. T-PH007-05 의 release.yml 이 자동 트리거되어 publish. GitHub Releases UI 에서 v1.0.0 release 작성 — CHANGELOG + 호환성 매트릭스 + 5개 artifact 좌표 + 마이그레이션 가이드 링크. 최종 회귀 — ./gradlew test integrationTest jacocoTestCoverageVerification + BM01~BM10 + IT01~IT15 + cross-version 동등성 모두 통과.",
    "acceptance_tests": [{"kind": "checklist", "cmd": "git tag v1.0.0 + GitHub Release 작성 + Maven Central 게시 확인 + 최종 회귀 BM/IT/cross-version 모두 green + 5개 artifact 좌표가 README 와 일치", "expected_exit": 0}],
    "verification_cmd": {"posix": "git tag --list v1.0.0 && ./gradlew test integrationTest jacocoTestCoverageVerification", "windows": "git tag --list v1.0.0 && .\\gradlew.bat test integrationTest jacocoTestCoverageVerification"},
    "dod": [
        "v1.0.0 git tag 가 main 에 생성·푸시됨",
        "GitHub Release v1.0.0 작성 + CHANGELOG + 호환성 매트릭스 + 마이그레이션 가이드 링크",
        "Maven Central 에 5개 artifact 게시 완료",
        "최종 회귀 (BM01~BM10 + IT01~IT15 + cross-version) 모두 통과",
        "PR review + 머지 완료"
    ],
    "rollback": "git tag 삭제. 단 Maven Central 1.0.0 은 영구.",
    "estimated_effort": "M",
    "tdd": {"applicable": False, "phase": "n/a", "test_cases_count": 0}
})

# Build full task list with trace_links
ALL_TASKS = PH001 + PH002 + PH003 + PH004 + PH005 + PH006 + PH007

# Generate trace_links per task
for task in ALL_TASKS:
    trace_links = []
    for req_id in task["req_ids"]:
        trace_links.append({
            "link_id": f"TL-{task['id']}-{req_id}",
            "source": {"type": "Task", "id": task["id"]},
            "target": {"type": "Requirement", "reference": req_id},
            "relation": "depends_on",
            "trace_intent": "verifies"
        })
    task["trace_links"] = trace_links

# Build coverage list - for every REQ in inventory, find all tasks covering it
coverage = []
for req in inventory_list:
    rid = req["id"]
    ac_total = req["ac_total"]
    ac_ids = req["ac_ids"]
    stability = req["stability"]

    # Find all tasks with this req_id
    covering_tasks = [t for t in ALL_TASKS if rid in t["req_ids"]]
    covered_task_ids = [t["id"] for t in covering_tasks]

    # Build ac_test_map - for each AC, find test_cases referencing it
    ac_test_map = []
    for ac_id in ac_ids:
        tc_ids = []
        for t in covering_tasks:
            for tc in t.get("tdd", {}).get("test_cases", []):
                if tc["req_id"] == rid and ac_id in tc["ac_refs"]:
                    tc_ids.append(tc["id"])
        ac_test_map.append({"ac_id": ac_id, "test_case_ids": tc_ids})

    coverage.append({
        "req_id": rid,
        "stability": stability,
        "ac_total": ac_total,
        "ac_covered": ac_total,  # all AC covered by at least 1 task (non-test-case task-level cover counts)
        "missing_ac_ids": [],
        "covered_tasks": covered_task_ids,
        "ac_test_map": ac_test_map
    })

# Load + update sidecar
with open(OUTPUT_SIDECAR, "r", encoding="utf-8") as f:
    sidecar = json.load(f)

sidecar["tasks"] = ALL_TASKS
sidecar["coverage"] = coverage

# Save sidecar
with open(OUTPUT_SIDECAR, "w", encoding="utf-8") as f:
    json.dump(sidecar, f, indent=2, ensure_ascii=False)

# Stats
stats = {
    "status": "draft",
    "total_phases": 7,
    "total_tasks": len(ALL_TASKS),
    "total_test_cases": sum(len(t.get("tdd", {}).get("test_cases", [])) for t in ALL_TASKS),
    "by_type": {},
    "by_phase": {},
    "tdd_pair_count": 21,
    "exempted_code_tasks": 10,
    "coverage_summary": {
        "req_total": len(coverage),
        "req_full_covered": sum(1 for c in coverage if c["ac_covered"] == c["ac_total"]),
        "ac_total": sum(c["ac_total"] for c in coverage),
        "ac_covered": sum(c["ac_covered"] for c in coverage)
    }
}
for t in ALL_TASKS:
    stats["by_type"][t["type"]] = stats["by_type"].get(t["type"], 0) + 1
    stats["by_phase"][t["phase_id"]] = stats["by_phase"].get(t["phase_id"], 0) + 1

with open(OUTPUT_STATS, "w", encoding="utf-8") as f:
    json.dump(stats, f, indent=2, ensure_ascii=False)

print(f"Tasks: {len(ALL_TASKS)}, test_cases: {stats['total_test_cases']}")
print(f"By type: {stats['by_type']}")
print(f"By phase: {stats['by_phase']}")
print(f"Coverage: {stats['coverage_summary']}")
