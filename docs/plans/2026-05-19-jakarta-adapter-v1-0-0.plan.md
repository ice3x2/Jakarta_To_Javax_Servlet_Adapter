---
run_id: 2026-05-19-jakarta-adapter-v1-0-0
target: v1.0.0
plan_version: 0.1.0
plan_contract: "1.1.0"
generated_at: 2026-05-19T00:00:00Z
tool_versions:
  speckiwi: 2.2.2
  kiwi_planner: 0.3.0
  validator: 0.4.0
stability_summary:
  frozen: 0
  stable: 0
  evolving: 0
  draft: 0
  unset: 44
sidecar_path: ./2026-05-19-jakarta-adapter-v1-0-0.sidecar.json
md_sha256: TBD
---

## §1 개요

### 1.1 목표

`Jakarta_To_Javax_Servlet_Adapter` 0.0.1 단일 jar 를 `common / servlet5 / servlet6 / servlet61 / bom` 5-모듈 Gradle 토폴로지로 재구조화하고, 0.0.1 Critical 결함 9건을 TDD 방식으로 일괄 수정하며, Tomcat embed 기반 통합 테스트 슈트를 도입하여 Maven Central 에 v1.0.0 으로 게시한다.

### 1.2 범위 (in_scope)

- 5개 Gradle 서브 모듈 신설 (`common`, `servlet5`, `servlet6`, `servlet61`, `bom`) — FR-MOD-001~007 + CON-MOD-001 + NFR-MOD-001
- 0.0.1 코드의 9개 Critical 결함 수정 (StreamConverter, Part 재귀, setContentLengthLong narrow, encoding 교차 오호출, getRealPath 부재, instanceof 분기 오류, getCookies null) — FR-FIX-001~009
- 변환 의미론 SPEC-1~16 + 폴백 정책 + strict 모드 + AdapterUnwrap 도입 — FR-CONV-001~014 + NFR-CONV-001
- 5단계 테스트 피라미드 (단위/contract/통합/크로스버전/회귀) + Bug Museum BM01~BM10 + Tomcat embed IT01~IT15 — FR-TEST-001~005 + NFR-TEST-001
- GitHub Actions CI 매트릭스 + JaCoCo 게이트 — OPS-TEST-001
- Maven 좌표 + README 호환성 매트릭스 + 0.x 마이그레이션 + Sonatype/GPG 준비 — IR-REL-001 + FR-REL-001 + MIG-REL-001 + OPS-REL-001

### 1.3 제외사항 (out_of_scope)

excluded_reqs:
- **NFR-MOD-002** — JPMS `module-info.java` 본격 도입, Target=v1.1.0, Stability=draft. v1.0.0 에서는 `Automatic-Module-Name` MANIFEST 항목 (NFR-MOD-001) 까지만 처리.
- **OPS-TEST-002** — PIT mutation testing, Target=v1.1.0, Stability=draft. v1.0.0 의 매 PR 은 JaCoCo (95%/100%) 게이트만 적용 (D6 결정).

기타:
- v1.1.0 nightly cron + release-tag PIT 도입은 별도 plan 으로 분리한다.
- 0.x alias artifact 의 별도 게시 vs servlet5 내 deprecated alias 패키지 선택은 PH-001 Decision-Gate 의 OQ-001 결정에 따른다.

### 1.4 전제조건 / 가정

- `com.clipsoft` group 은 build.gradle 의 현 값을 그대로 유지한다.
- `javax.servlet:javax.servlet-api` 의존은 4.0.1 로 고정한다.
- Gradle Java Toolchain 으로 JDK 8 / 11 / 17 을 자동 다운로드한다.
- Mockito 5.6.0 + JUnit Jupiter 5.9.2 호환 (현 build.gradle 와 정합).
- 통합 테스트는 Linux 만 실행한다 (Windows 는 단위 + contract).
- 외부 의존: Sonatype 계정 / GPG 키 / `com.clipsoft` namespace 점유 — OQ-003 으로 외부 트래킹.

## §2 Phase 목록

| phase_id | title | goal | depends_on | task_count |
|---|---|---|---|---|
| PH-001 | Decision-Gate | OQ-001 0.x alias 정책 결정, OQ-002 JPMS 유보 확인, OQ-003 Sonatype/GPG 외부 트래킹 시작 | — | 3 |
| PH-002 | Multi-module 토폴로지 구축 | 5개 Gradle 서브 모듈 신설 + 0.0.1 코드 servlet5 이동 + 의존 그래프 강제 + Automatic-Module-Name | PH-001 | 9 |
| PH-003 | 결함 9건 + Bug Museum | FR-FIX-001~009 의 TDD 회귀 케이스 (BM01~BM09) 선행 작성 + 결함 수정 + BM10 잠금 | PH-002 | 11 |
| PH-004 | 변환 의미론 + 폴백 정책 | SPEC-1~16, AdapterUnwrap marker, strict 모드, 폴백 가시성 (로깅+diagnostics), Cookie attribute 정책, servlet6/61 모듈 사본 + 신규 추상 메서드 | PH-002 | 12 |
| PH-005 | 테스트 인프라 | 단위 테스트 (Mockito) + Reflection Contract Test + Tomcat embed IT01~IT15 + 크로스버전 + JaCoCo 게이트 | PH-003, PH-004 | 7 |
| PH-006 | CI 매트릭스 + JaCoCo | GitHub Actions ci.yml (build-matrix + integration-tests + coverage) | PH-005 | 3 |
| PH-007 | Maven Central 게시 | maven-publish + signing + BOM POM + README 호환성 매트릭스 + CHANGELOG + 0.x 마이그레이션 + GPG/Sonatype 적용 + 1.0.0 staging upload | PH-006, PH-001 | 7 |

## §3 Task 상세

### §3.PH-001 Decision-Gate

#### §3.PH-001.T-PH001-01 OQ-001 0.x → 1.0 alias 정책 결정 회의 트래킹

- id: T-PH001-01
- phase_id: PH-001
- title: OQ-001 0.x → 1.0 alias 정책 결정 회의 트래킹
- type: issue
- req_ids: [MIG-REL-001]
- files: [docs/research/80.decisions.md, docs/spec/00.index.md, docs/spec/50.compatibility.srs.md]
- action: GitHub Issue 를 생성하여 OQ-001 (옵션 A 별도 alias artifact 게시 vs 옵션 B servlet5 내 deprecated alias 패키지) 의 결정 회의를 트래킹한다. 결정 후 `80.decisions.md` §3 미해결 표에서 §2 결정 매트릭스 (D8) 로 이동시키고 옵션을 명시하며, `00.index.md` §9 의 OQ-001 status 를 open → closed 로 갱신한다. MIG-REL-001 AC 의 옵션 A/B 분기가 결정문에 따라 활성화된다.
- acceptance_tests: [{"kind": "checklist", "cmd": "GitHub Issue 'OQ-001 decision' 생성 + 결정 후 80.decisions.md §3 → §2 항목 이동 (D8) + 00.index.md §9 OQ-001 status closed", "expected_exit": 0}]
- verification_cmd: null
- dod:
  - GitHub Issue URL 이 본 task 의 acceptance evidence 로 등재된다
  - `docs/research/80.decisions.md` §2 결정 매트릭스에 D8 (OQ-001 결정문) 행이 추가되어 옵션 A 또는 B 가 명시된다
  - `docs/spec/00.index.md` §9 OQ-001 status 가 `closed` 로 갱신된다
  - PH-007 의 alias 구현 Task (T-PH007-04) 가 본 결정 결과를 참조한다
- rollback: GitHub Issue 닫지 않고 미결로 두면 PH-007 의 alias 구현 차단. 본 task 가 차단 상태로 머무르면 v1.0.0 출시 지연.
- estimated_effort: S

#### §3.PH-001.T-PH001-02 OQ-002 JPMS 유보 명시 확인

- id: T-PH001-02
- phase_id: PH-001
- title: OQ-002 JPMS 유보 명시 확인
- type: review
- req_ids: [NFR-MOD-001]
- files: [docs/spec/00.index.md, docs/spec/10.module-architecture.srs.md]
- action: NFR-MOD-002 (`module-info.java` 본격 도입) 가 v1.1.0 으로 이연됨을 확인한다. v1.0.0 에서는 NFR-MOD-001 의 `Automatic-Module-Name` MANIFEST 항목만 적용한다. SRS 인덱스 §9 의 OQ-002 status 가 `deferred` 임을 확인하고, plan 의 excluded_reqs 에 NFR-MOD-002 등재 여부를 재확인한다. 본 review 는 NFR-MOD-001 의 AC-2 (4개 모듈 module-name 표 일치) 의 사전 게이트로 작동한다 — PH-002 의 어떤 task 도 module-info.java 생성을 포함하지 않음을 보장.
- acceptance_tests: [{"kind": "checklist", "items": ["00.index.md §9 OQ-002 status=deferred 확인", "plan sidecar.json excluded_reqs 에 NFR-MOD-002 포함 확인", "PH-002 의 어떤 task 도 module-info.java 생성을 포함하지 않음 (T-PH002-09 의 NFR-MOD-001 적용만 활성)"]}]
- verification_cmd: null
- dod:
  - `docs/spec/00.index.md` §9 OQ-002 행이 status=`deferred` 로 유지됨을 확인
  - plan 의 `excluded_reqs[]` 에 `NFR-MOD-002` (사유: v1.1.0 target, JPMS deferred) 가 등재됨을 검토
  - PH-002 의 어떤 task 도 `module-info.java` 생성을 포함하지 않음을 PH-002 진입 전 확인
- rollback: 본 review 가 통과되지 않으면 PH-002 의 manifest 작성 task (T-PH002-09) 만 진행하고 module-info 작업은 v1.1.0 으로 차단.
- estimated_effort: S

#### §3.PH-001.T-PH001-03 OQ-003 Sonatype / GPG 외부 트래킹 시작

- id: T-PH001-03
- phase_id: PH-001
- title: OQ-003 Sonatype 계정 + GPG 키 외부 트래킹 이슈 생성
- type: issue
- req_ids: [OPS-REL-001]
- files: [docs/spec/50.compatibility.srs.md]
- action: GitHub Issue 를 생성하여 외부 의존 (Sonatype OSSRH 또는 Central Portal 계정 발급, `com.clipsoft` namespace 점유 검증, GPG 키 생성·키서버 등록) 의 준비 상태를 트래킹한다. PH-002~PH-006 의 코딩 작업과 병렬 진행하며 PH-007 진입 전까지 자격증명을 완비한다. GitHub Secrets (OSSRH_USERNAME/PASSWORD, GPG_SIGNING_KEY/PASSPHRASE) 등록도 본 이슈로 추적한다.
- acceptance_tests: [{"kind": "checklist", "cmd": "GitHub Issue 'OQ-003 Maven Central credentials prep' 생성 + 4개 sub-task 체크박스(계정/namespace/GPG/secrets) 포함", "expected_exit": 0}]
- verification_cmd: null
- dod:
  - GitHub Issue URL 이 본 task 의 acceptance evidence 로 등재된다
  - Issue 본문에 4개 sub-task 가 체크박스로 포함된다 (Sonatype 계정 / namespace 점유 / GPG 키 / GitHub Secrets)
  - PH-007 진입 시점에 본 이슈가 closed 상태여야 함을 README 또는 CHANGELOG 에 표기
- rollback: Issue 가 PH-007 진입 시점까지 open 이면 PH-007 의 publishAll task (T-PH007-07) 가 차단되어 v1.0.0 출시 지연.
- estimated_effort: S

### §3.PH-002 Multi-module 토폴로지 구축

#### §3.PH-002.T-PH002-01 settings.gradle 재작성

- id: T-PH002-01
- phase_id: PH-002
- title: settings.gradle rootProject.name 변경 + 5개 서브 프로젝트 include
- type: file_op
- req_ids: [FR-MOD-001, IR-REL-001]
- files: [settings.gradle]
- action: `settings.gradle` 의 `rootProject.name = 'JavaXServletToJakartaAdapter'` 를 `rootProject.name = 'jakarta-to-javax-servlet-adapter'` (kebab-case, D5) 로 변경하고 `include 'common', 'servlet5', 'servlet6', 'servlet61', 'bom'` 를 추가한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew projects --quiet | grep -E \"(common|servlet5|servlet6|servlet61|bom)\" | wc -l", "expected_exit": 0, "expected_stdout_regex": "^5$"}]
- verification_cmd: {"posix": "./gradlew projects", "windows": ".\\gradlew.bat projects"}
- dod:
  - `settings.gradle` 의 `rootProject.name` 이 `jakarta-to-javax-servlet-adapter` 로 변경됨
  - `include 'common', 'servlet5', 'servlet6', 'servlet61', 'bom'` 가 등록됨
  - `./gradlew projects` 출력에 5개 서브 프로젝트가 모두 표시됨
- rollback: git diff 로 이전 state 복원. settings.gradle 1 파일 단위 변경이므로 영향 범위 작음.
- estimated_effort: S

#### §3.PH-002.T-PH002-02 root build.gradle 재구성

- id: T-PH002-02
- phase_id: PH-002
- title: root build.gradle 을 allprojects / subprojects 공통 설정으로 변환
- type: code
- req_ids: [FR-MOD-001, IR-REL-001]
- files: [build.gradle, gradle.properties]
- action: 현 단일 모듈 `build.gradle` (36 LOC) 을 `allprojects { group='com.clipsoft', version='1.0.0' }` + `subprojects { apply plugin: 'java', repositories, ... }` 형태로 재구성한다. `gradle.properties` 를 신설하여 toolchain 버전 default, JaCoCo 버전, Mockito/JUnit 버전 등 공통 프로퍼티를 추출한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :common:properties --quiet | grep -E \"^(group|version):\"", "expected_exit": 0, "expected_stdout_regex": "group: com\\.clipsoft.*version: 1\\.0\\.0"}]
- verification_cmd: {"posix": "./gradlew properties | grep -E '(group|version)'", "windows": ".\\gradlew.bat properties | findstr /R \"group version\""}
- dod:
  - root `build.gradle` 가 `allprojects` + `subprojects` 블록을 가지며 모듈별 build.gradle 에 위임
  - 모든 서브 프로젝트에서 `group=com.clipsoft`, `version=1.0.0` 가 표시됨
  - `gradle.properties` 가 신설되어 공통 프로퍼티 보유
- rollback: git diff 로 build.gradle / gradle.properties 복원.
- estimated_effort: M

#### §3.PH-002.T-PH002-03 common 모듈 신설 + ABI 호환 컨버터 이동

- id: T-PH002-03
- phase_id: PH-002
- title: common 모듈 신설 (EnumsConverter, HttpComponentConverter, ReadListenerConverter, WriteListenerConverter, ConverterSupport)
- type: file_op
- req_ids: [FR-MOD-002, FR-MOD-004]
- files: [common/build.gradle, common/src/main/java/adapter/common/ConverterSupport.java, common/src/main/java/adapter/common/EnumsConverter.java, common/src/main/java/adapter/common/HttpComponentConverter.java, common/src/main/java/adapter/common/ReadListenerConverter.java, common/src/main/java/adapter/common/WriteListenerConverter.java]
- action: `common/` 디렉토리를 신설하고 `common/build.gradle` 에 `apply plugin: 'java'`, `sourceCompatibility=1.8`, `compileOnly 'jakarta.servlet:jakarta.servlet-api:5.0.0'`, `testImplementation 'jakarta.servlet:jakarta.servlet-api:5.0.0'` 를 작성한다. 현 `src/main/java/adapter/servletElementConverter/` 의 `EnumsConverter.java`, `HttpComponentConverter.java`, `ReadListenerConverter.java`, `WriteListenerConverter.java` 4개 파일을 `common/src/main/java/adapter/common/` 으로 `git mv` 하고 패키지 선언을 `package adapter.common;` 으로 변경한다. 공용 예외 변환 헬퍼 `ConverterSupport.java` 를 신설한다 (jakarta⇔javax `ServletException` 양방향 변환).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :common:compileJava --quiet", "expected_exit": 0}, {"kind": "file_state", "cmd": "test -f common/src/main/java/adapter/common/EnumsConverter.java && test -f common/src/main/java/adapter/common/HttpComponentConverter.java && test -f common/src/main/java/adapter/common/ReadListenerConverter.java && test -f common/src/main/java/adapter/common/WriteListenerConverter.java && test -f common/src/main/java/adapter/common/ConverterSupport.java", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :common:compileJava && ls common/src/main/java/adapter/common/", "windows": ".\\gradlew.bat :common:compileJava && dir common\\src\\main\\java\\adapter\\common"}
- dod:
  - `common/build.gradle` 가 jakarta 5.0.0 compileOnly 핀 + Java 8 타깃을 가진다
  - `common/src/main/java/adapter/common/` 에 5개 클래스 (EnumsConverter, HttpComponentConverter, ReadListenerConverter, WriteListenerConverter, ConverterSupport) 만 존재한다
  - 5개 클래스의 패키지 선언이 `package adapter.common;` 로 변경됨
  - `./gradlew :common:compileJava` 성공
- rollback: `git mv` 의 reverse 로 원상 복귀. 단 PH-003/PH-004 가 본 task 이동된 위치를 import 하므로 후속 phase 진입 전 결정.
- estimated_effort: M

#### §3.PH-002.T-PH002-04 servlet5 모듈 신설 + 0.0.1 코드 이동 + 패키지명 변경

- id: T-PH002-04
- phase_id: PH-002
- title: servlet5 모듈 신설 + 현 0.0.1 소스 이동 + adapter.jakarta.servlet5 / adapter.javax.servlet5 / adapter.servletElementConverter5 패키지 변경
- type: file_op
- req_ids: [FR-MOD-002, FR-MOD-003, FR-MOD-005, FR-MOD-006]
- files: [servlet5/build.gradle, servlet5/src/main/java/adapter/jakarta/servlet5/, servlet5/src/main/java/adapter/javax/servlet5/, servlet5/src/main/java/adapter/servletElementConverter5/, servlet5/src/main/java/com/snoworca/ServletAdapter.java, servlet5/src/main/java/com/snoworca/ServletAdapter5.java]
- action: `servlet5/` 디렉토리를 신설하고 `servlet5/build.gradle` 에 `sourceCompatibility=1.8`, `compileOnly 'jakarta.servlet:jakarta.servlet-api:5.0.0'`, `compileOnly 'javax.servlet:javax.servlet-api:4.0.1'`, `implementation project(':common')` 를 작성한다. 현 `src/main/java/adapter/jakarta/servlet/` → `servlet5/src/main/java/adapter/jakarta/servlet5/` 로 git mv 하고 패키지 선언을 `adapter.jakarta.servlet5` 로 변경한다 (재귀 — `.http` 도 포함). 동일하게 `adapter/javax/servlet` → `adapter/javax/servlet5`, 나머지 servletElementConverter 의 비-ABI 컨버터 (ServletConverter, StreamConverter, DescriptorConverter, AsyncContextConverter, ServletReqResConverter) 는 `adapter/servletElementConverter5/` 로 이동한다. `src/main/java/com/snoworca/ServletAdapter.java` 를 servlet5 로 이동하고 `ServletAdapter5.java` 별칭 클래스를 신설한다 (모든 정적 메서드를 ServletAdapter 로 위임). FR-MOD-006 의 시그니처 표대로 `adaptToJakarta(jakarta X)` 항등 오버로드를 추가한다. 0.0.1 ServletAdapter 와 시그니처 호환을 유지한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:compileJava --quiet", "expected_exit": 0}, {"kind": "file_state", "cmd": "test -d servlet5/src/main/java/adapter/jakarta/servlet5 && test -d servlet5/src/main/java/adapter/javax/servlet5 && test -d servlet5/src/main/java/adapter/servletElementConverter5 && test -f servlet5/src/main/java/com/snoworca/ServletAdapter.java && test -f servlet5/src/main/java/com/snoworca/ServletAdapter5.java", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:compileJava && find servlet5/src/main -name '*.java' | wc -l", "windows": ".\\gradlew.bat :servlet5:compileJava && dir /S /B servlet5\\src\\main\\*.java | find /C \".java\""}
- dod:
  - `servlet5/build.gradle` 가 jakarta 5.0.0 + javax 4.0.1 compileOnly + common project 의존을 가진다
  - 현 `src/main/java/adapter/jakarta/servlet/` 내용이 모두 `adapter.jakarta.servlet5.*` 패키지로 이동됨 (HttpServletRequest, HttpServletResponse, ServletContext, ServletRequest, ServletResponse, Part, HttpSession, HttpSessionContext, HttpUpgradeHandler, FilterRegistration 등)
  - 현 `src/main/java/adapter/javax/servlet/` 내용이 모두 `adapter.javax.servlet5.*` 패키지로 이동됨
  - 비-ABI 컨버터 5개 (ServletConverter, StreamConverter, DescriptorConverter, AsyncContextConverter, ServletReqResConverter) 가 `adapter.servletElementConverter5.*` 로 이동됨
  - `com.snoworca.ServletAdapter` 가 servlet5 로 이동되고 14개 정적 메서드 시그니처 보유 (FR-MOD-006 표대로 — `adaptToJakarta(jakarta X)` 항등 오버로드 추가)
  - `com.snoworca.ServletAdapter5` 별칭 클래스가 신설되어 ServletAdapter 로 위임
  - `./gradlew :servlet5:compileJava` 성공
- rollback: git mv reverse + 패키지 선언 복원. 0.0.1 코드 위치가 변경되므로 결함 수정 (PH-003) 전 본 task 가 stable 해야 한다.
- estimated_effort: L

#### §3.PH-002.T-PH002-05 servlet6 모듈 신설 (servlet5 사본 기준)

- id: T-PH002-05
- phase_id: PH-002
- title: servlet6 모듈 신설 + jakarta 6.0.0 + Java 11 toolchain + servlet5 코드 사본 (패키지명 변경)
- type: file_op
- req_ids: [FR-MOD-002, FR-MOD-003, FR-MOD-005, FR-MOD-006]
- files: [servlet6/build.gradle, servlet6/src/main/java/adapter/jakarta/servlet6/, servlet6/src/main/java/adapter/javax/servlet6/, servlet6/src/main/java/adapter/servletElementConverter6/, servlet6/src/main/java/com/snoworca/ServletAdapter.java, servlet6/src/main/java/com/snoworca/ServletAdapter6.java]
- action: `servlet6/build.gradle` 에 `java { toolchain { languageVersion = JavaLanguageVersion.of(11) } }`, `compileOnly 'jakarta.servlet:jakarta.servlet-api:6.0.0'`, `compileOnly 'javax.servlet:javax.servlet-api:4.0.1'`, `implementation project(':common')` 작성. servlet5 의 모든 소스를 servlet6 로 사본 + 패키지명을 `adapter.jakarta.servlet6` / `adapter.javax.servlet6` / `adapter.servletElementConverter6` 로 변경한다. `com.snoworca.ServletAdapter` 동일 FQCN + `ServletAdapter6` 별칭 신설. 본 task 는 6.0 신규 추상 메서드 (`getRequestId`, `getProtocolRequestId`, `getServletConnection`) 와 폴백은 PH-004 로 이연하고 컴파일만 통과하도록 placeholder 처리한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet6:compileJava --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet6:compileJava", "windows": ".\\gradlew.bat :servlet6:compileJava"}
- dod:
  - `servlet6/build.gradle` 가 jakarta 6.0.0 + javax 4.0.1 compileOnly + JDK 11 toolchain 보유
  - servlet5 의 모든 소스가 servlet6 로 패키지명 변경된 사본으로 존재
  - `com.snoworca.ServletAdapter6` 별칭 클래스 존재
  - `./gradlew :servlet6:compileJava` 성공 (placeholder 메서드 포함)
- rollback: servlet6 디렉토리 전체 삭제 + settings.gradle include 행 제거.
- estimated_effort: L

#### §3.PH-002.T-PH002-06 servlet61 모듈 신설 (servlet6 사본 기준)

- id: T-PH002-06
- phase_id: PH-002
- title: servlet61 모듈 신설 + jakarta 6.1.0 + Java 17 toolchain + servlet6 코드 사본 (패키지명 변경)
- type: file_op
- req_ids: [FR-MOD-002, FR-MOD-003, FR-MOD-005, FR-MOD-006]
- files: [servlet61/build.gradle, servlet61/src/main/java/adapter/jakarta/servlet61/, servlet61/src/main/java/adapter/javax/servlet61/, servlet61/src/main/java/adapter/servletElementConverter61/, servlet61/src/main/java/com/snoworca/ServletAdapter.java, servlet61/src/main/java/com/snoworca/ServletAdapter61.java]
- action: `servlet61/build.gradle` 에 `java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }`, `compileOnly 'jakarta.servlet:jakarta.servlet-api:6.1.0'`, `compileOnly 'javax.servlet:javax.servlet-api:4.0.1'`, `implementation project(':common')` 작성. servlet6 의 모든 소스를 servlet61 로 사본 + 패키지명을 `adapter.jakarta.servlet61` / `adapter.javax.servlet61` / `adapter.servletElementConverter61` 로 변경한다. `com.snoworca.ServletAdapter` 동일 FQCN + `ServletAdapter61` 별칭 신설. 6.1 신규 추상 메서드 `sendRedirect(String, int, boolean)` 는 PH-004 로 이연하고 컴파일만 placeholder 처리한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet61:compileJava --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet61:compileJava", "windows": ".\\gradlew.bat :servlet61:compileJava"}
- dod:
  - `servlet61/build.gradle` 가 jakarta 6.1.0 + javax 4.0.1 compileOnly + JDK 17 toolchain 보유
  - servlet6 의 모든 소스가 servlet61 로 패키지명 변경된 사본으로 존재
  - `com.snoworca.ServletAdapter61` 별칭 클래스 존재
  - `./gradlew :servlet61:compileJava` 성공
- rollback: servlet61 디렉토리 전체 삭제 + settings.gradle include 행 제거.
- estimated_effort: L

#### §3.PH-002.T-PH002-07 bom 모듈 신설

- id: T-PH002-07
- phase_id: PH-002
- title: bom 모듈 신설 (java-platform plugin + 4개 모듈 constraints)
- type: code
- req_ids: [FR-MOD-007]
- files: [bom/build.gradle]
- action: `bom/build.gradle` 에 `plugins { id 'java-platform'; id 'maven-publish' }` + `dependencies { constraints { api project(':common'); api project(':servlet5'); api project(':servlet6'); api project(':servlet61') } }` 작성. `generatePomFileForBomPlatformPublication` task 가 정상 생성됨을 검증한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :bom:generatePomFileForBomPlatformPublication --quiet && test -f bom/build/publications/bomPlatform/pom-default.xml && grep -E 'jakarta-to-javax-servlet-adapter-(common|servlet5|servlet6|servlet61)' bom/build/publications/bomPlatform/pom-default.xml | wc -l", "expected_exit": 0, "expected_stdout_regex": "^4$"}]
- verification_cmd: {"posix": "./gradlew :bom:generatePomFileForBomPlatformPublication && cat bom/build/publications/bomPlatform/pom-default.xml", "windows": ".\\gradlew.bat :bom:generatePomFileForBomPlatformPublication && type bom\\build\\publications\\bomPlatform\\pom-default.xml"}
- dod:
  - `bom/build.gradle` 가 java-platform + maven-publish 플러그인 보유
  - constraints 에 4개 모듈 (common/servlet5/6/61) 모두 등재
  - 생성된 POM 의 `<dependencyManagement>` 에 4개 artifactId 모두 포함
- rollback: bom 디렉토리 삭제 + settings.gradle include 행 제거.
- estimated_effort: S

#### §3.PH-002.T-PH002-08 CON-MOD-001 의존 그래프 강제 (단방향)

- id: T-PH002-08
- phase_id: PH-002
- title: 버전 모듈 간 상호 의존 금지 검증 + 위반 시 빌드 실패 게이트
- type: code
- req_ids: [CON-MOD-001]
- files: [build.gradle, servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle]
- action: 각 servletN/build.gradle 의 dependencies 블록에 `implementation project(':common')` 만 존재함을 보장한다. root build.gradle 에 단방향 의존 그래프 강제 게이트 task `validateNoVersionModuleCrossDeps` 를 추가하여 `:servlet5/6/61` 의 dependencies 출력에 다른 servletN project 가 없음을 검사하고 발견 시 throw new GradleException 한다. `./gradlew check` 가 본 task 를 통합 호출한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew validateNoVersionModuleCrossDeps --quiet", "expected_exit": 0}, {"kind": "shell", "cmd": "./gradlew :servlet5:dependencies --configuration compileClasspath --quiet | grep -E 'project :servlet(6|61)' | wc -l", "expected_exit": 0, "expected_stdout_regex": "^0$"}, {"kind": "shell", "cmd": "./gradlew :servlet6:dependencies --configuration compileClasspath --quiet | grep -E 'project :servlet(5|61)' | wc -l", "expected_exit": 0, "expected_stdout_regex": "^0$"}, {"kind": "shell", "cmd": "./gradlew :servlet61:dependencies --configuration compileClasspath --quiet | grep -E 'project :servlet(5|6)' | wc -l", "expected_exit": 0, "expected_stdout_regex": "^0$"}]
- verification_cmd: {"posix": "./gradlew validateNoVersionModuleCrossDeps", "windows": ".\\gradlew.bat validateNoVersionModuleCrossDeps"}
- dod:
  - 3 버전 모듈의 dependencies 출력에 다른 버전 모듈 project 가 없음
  - `validateNoVersionModuleCrossDeps` 게이트 task 가 root 에 등록되어 check task 가 의존
  - CON-MOD-001 의 3 AC 모두 자동 검증
- rollback: root build.gradle 의 게이트 task 제거 + servletN/build.gradle 의 의존 복원.
- estimated_effort: S

#### §3.PH-002.T-PH002-09 NFR-MOD-001 Automatic-Module-Name MANIFEST 적용

- id: T-PH002-09
- phase_id: PH-002
- title: 4개 모듈 jar 의 MANIFEST 에 Automatic-Module-Name 헤더 추가
- type: code
- req_ids: [NFR-MOD-001]
- files: [common/build.gradle, servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle]
- action: 각 모듈 build.gradle 의 `jar { manifest { attributes 'Automatic-Module-Name': '<name>' } }` 블록을 추가한다. 매핑: common → `com.clipsoft.servlet.adapter.common`, servlet5 → `com.clipsoft.servlet.adapter.servlet5`, servlet6 → `com.clipsoft.servlet.adapter.servlet6`, servlet61 → `com.clipsoft.servlet.adapter.servlet61`. (NFR-MOD-002 는 v1.0.0 범위 밖 — module-info.java 생성 금지.)
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :common:jar :servlet5:jar :servlet6:jar :servlet61:jar --quiet && for m in common servlet5 servlet6 servlet61; do unzip -p $m/build/libs/jakarta-to-javax-servlet-adapter-$m-1.0.0.jar META-INF/MANIFEST.MF | grep -E '^Automatic-Module-Name:' || exit 1; done", "expected_exit": 0}]
- verification_cmd: {"posix": "for m in common servlet5 servlet6 servlet61; do echo \"=== $m ===\"; unzip -p $m/build/libs/jakarta-to-javax-servlet-adapter-$m-1.0.0.jar META-INF/MANIFEST.MF | grep Automatic-Module-Name; done", "windows": "for %m in (common servlet5 servlet6 servlet61) do @echo === %m === && powershell -command \"Get-Content -Raw $env:TEMP\\manifest_%m.mf | Select-String 'Automatic-Module-Name'\""}
- dod:
  - 4개 모듈 jar 의 MANIFEST 에 `Automatic-Module-Name` 헤더가 존재
  - 헤더 값이 NFR-MOD-001 표와 일치 (com.clipsoft.servlet.adapter.{common,servlet5,servlet6,servlet61})
  - module-info.java 는 생성되지 않음 (NFR-MOD-002 v1.1.0 이연 확인)
- rollback: build.gradle jar 블록의 manifest 설정 제거.
- estimated_effort: S

### §3.PH-003 결함 9건 + Bug Museum

#### §3.PH-003.T-PH003-01 BugMuseumTest 클래스 골격 + BM01~BM10 TDD 회귀 케이스 선행 작성

- id: T-PH003-01
- phase_id: PH-003
- title: BugMuseumTest 클래스 신설 + BM01~BM10 회귀 케이스 (TDD 실패 확인)
- type: code
- req_ids: [FR-TEST-004]
- files: [servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java]
- action: `servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java` 신설. BM01~BM10 각각의 `@Test` + `@DisplayName` 메서드 작성. 각 테스트는 현 결함 있는 코드에서 명시적으로 실패하도록 작성 (TDD red). BM10 은 잠금 (lock-in) 으로 현 ThreadLocal 단일 스레드 동기 호출 가정을 회귀 보호 — 현 동작에서 통과하고 다중 스레드 호출 변경 시 실패. `./gradlew :servlet5:test --tests BugMuseumTest` 가 BM01~BM09 실패 + BM10 통과를 보고함을 확인.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest 2>&1 | grep -E 'BM0[1-9]' | grep -E '(FAILED|failed)' | wc -l", "expected_exit": 0, "expected_stdout_regex": "^9$"}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest --info", "windows": ".\\gradlew.bat :servlet5:test --tests com.snoworca.regression.BugMuseumTest --info"}
- dod:
  - `BugMuseumTest.java` 가 servlet5 에 신설
  - BM01~BM10 각각 `@DisplayName` 으로 결함 ID + 설명 명시
  - 현 0.0.1 결함 있는 코드에서 BM01~BM09 가 모두 실패하고 BM10 만 통과 (TDD red phase)
  - 본 task 가 PH-003 의 모든 후속 결함 수정 task 의 검증 기준이 된다
- rollback: BugMuseumTest.java 삭제. 단 본 task 가 PH-003 의 TDD 진입점이므로 필수.
- estimated_effort: L

#### §3.PH-003.T-PH003-02 FR-FIX-001 StreamConverter.read → read 수정 (BM01)

- id: T-PH003-02
- phase_id: PH-003
- title: StreamConverter L41/L93 의 read 가 readLine 호출하는 결함 수정 (양방향)
- type: code
- req_ids: [FR-FIX-001]
- files: [servlet5/src/main/java/adapter/servletElementConverter5/StreamConverter.java:40-94, servlet6/src/main/java/adapter/servletElementConverter6/StreamConverter.java:40-94, servlet61/src/main/java/adapter/servletElementConverter61/StreamConverter.java:40-94]
- action: `StreamConverter.java` L41 (jakarta 방향) 의 `inputstream.readLine(b, off, len)` 호출을 `inputstream.read(b, off, len)` 로 수정. L93 (javax 방향) 동일 수정. `readLine(byte[], int, int)` 시그니처는 별도로 유지되어 원본의 `readLine` 으로 위임됨을 확인. servlet6 / servlet61 모듈의 동일 위치도 동일 수정.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest --tests *BM01* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM01*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM01*"}
- dod:
  - `StreamConverter.java` L41 (jakarta) 및 L93 (javax) 의 `readLine(b, off, len)` 가 `read(b, off, len)` 로 수정됨
  - servlet5 / servlet6 / servlet61 3개 모듈 모두 적용
  - BM01 회귀 테스트 통과 (TDD green)
  - `readLine(byte[], int, int)` 시그니처는 별도 유지되어 원본의 `readLine` 위임
- rollback: git diff 로 3개 모듈의 StreamConverter.java L41/L93 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-03 FR-FIX-002 Part.getSubmittedFileName 무한 재귀 수정 (BM02)

- id: T-PH003-03
- phase_id: PH-003
- title: jakarta/javax Part 의 getSubmittedFileName 자기 재귀 → 원본 위임 수정
- type: code
- req_ids: [FR-FIX-002]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/http/Part.java:50-54, servlet5/src/main/java/adapter/javax/servlet5/http/Part.java, servlet6/src/main/java/adapter/jakarta/servlet6/http/Part.java, servlet6/src/main/java/adapter/javax/servlet6/http/Part.java, servlet61/src/main/java/adapter/jakarta/servlet61/http/Part.java, servlet61/src/main/java/adapter/javax/servlet61/http/Part.java]
- action: jakarta `Part.java` L52 의 `this.getSubmittedFileName()` (자기 재귀) 를 `this.part.getSubmittedFileName()` (원본 위임) 로 수정. javax `Part.java` 대칭 위치도 동일 검토·수정. servlet5 / servlet6 / servlet61 3개 모듈 모두 적용 (각 모듈에 jakarta + javax 사본 2개씩).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM02* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM02*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM02*"}
- dod:
  - jakarta Part.getSubmittedFileName 자기 재귀 → 원본 part.getSubmittedFileName 위임 수정
  - javax Part 대칭 수정
  - 3개 모듈 모두 적용
  - BM02 통과 (StackOverflowError 없음)
- rollback: git diff 로 Part.java L50-52 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-04 FR-FIX-003 setContentLengthLong narrow 수정 (BM05)

- id: T-PH003-04
- phase_id: PH-003
- title: jakarta/javax ServletResponse 의 setContentLengthLong narrow cast → 원본 동명 위임
- type: code
- req_ids: [FR-FIX-003]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletResponse.java:81-84, servlet5/src/main/java/adapter/javax/servlet5/ServletResponse.java, servlet6/src/main/java/adapter/jakarta/servlet6/ServletResponse.java, servlet6/src/main/java/adapter/javax/servlet6/ServletResponse.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletResponse.java, servlet61/src/main/java/adapter/javax/servlet61/ServletResponse.java]
- action: jakarta `ServletResponse.java` L82 의 `this.response.setContentLength((int)arg0)` 를 `this.response.setContentLengthLong(arg0)` 로 수정. javax 측 대칭 검토 (현재는 올바른 위임 — 참조로만 사용). 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM05* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM05*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM05*"}
- dod:
  - jakarta ServletResponse L82 `setContentLength((int)arg0)` → `setContentLengthLong(arg0)` 수정
  - 3개 모듈 모두 적용
  - BM05 통과 (Long.MAX_VALUE narrow 없음)
- rollback: git diff 로 ServletResponse.java L82 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-05 FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 수정 (BM03)

- id: T-PH003-05
- phase_id: PH-003
- title: jakarta ServletContext.setResponseCharacterEncoding 이 setRequestCharacterEncoding 호출하는 결함 수정
- type: code
- req_ids: [FR-FIX-004]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java:336-340, servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java]
- action: jakarta `ServletContext.java` L338 의 `this.setRequestCharacterEncoding(arg0)` (자기 메서드 + 잘못된 방향) 을 `this.servletContext.setResponseCharacterEncoding(arg0)` (원본 동명) 로 수정. 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM03* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM03*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM03*"}
- dod:
  - jakarta ServletContext L338 의 교차 호출 → 원본 동명 위임 수정
  - 3개 모듈 모두 적용
  - BM03 통과
- rollback: git diff 로 ServletContext.java L337-339 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-06 FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 수정 (BM08)

- id: T-PH003-06
- phase_id: PH-003
- title: javax ServletContext.setRequestCharacterEncoding 이 setResponseCharacterEncoding 호출하는 결함 수정 (대칭)
- type: code
- req_ids: [FR-FIX-005]
- files: [servlet5/src/main/java/adapter/javax/servlet5/ServletContext.java:361-365, servlet6/src/main/java/adapter/javax/servlet6/ServletContext.java, servlet61/src/main/java/adapter/javax/servlet61/ServletContext.java]
- action: javax `ServletContext.java` L363 의 `this.setResponseCharacterEncoding(encoding)` 을 `this.servletContext.setRequestCharacterEncoding(encoding)` 로 수정. 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM08* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM08*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM08*"}
- dod:
  - javax ServletContext L363 의 교차 호출 수정
  - 3개 모듈 모두 적용
  - BM08 통과
- rollback: git diff 로 javax ServletContext.java L362-364 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-07 FR-FIX-006 getContentLengthLong fallback 부정확 수정 (BM09)

- id: T-PH003-07
- phase_id: PH-003
- title: jakarta/javax ServletRequest.getContentLengthLong 의 try/catch fallback 제거 후 단순 위임
- type: code
- req_ids: [FR-FIX-006]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java:173-180, servlet5/src/main/java/adapter/javax/servlet5/ServletRequest.java, servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java, servlet6/src/main/java/adapter/javax/servlet6/ServletRequest.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java, servlet61/src/main/java/adapter/javax/servlet61/ServletRequest.java]
- action: jakarta `ServletRequest.java` L173-179 의 try/catch + `getContentLength()` int fallback 을 제거하고 `return this.request.getContentLengthLong()` 단순 위임으로 수정. javax 측 대칭 검토. 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM09* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM09*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM09*"}
- dod:
  - getContentLengthLong 의 try/catch 제거 + 단순 위임
  - 3개 모듈 모두 적용
  - BM09 통과 (Long.MAX_VALUE 정확 반환)
- rollback: git diff 로 ServletRequest.java L173-179 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-08 FR-FIX-007 jakarta ServletRequest.getRealPath 처리 (BM06)

- id: T-PH003-08
- phase_id: PH-003
- title: jakarta ServletRequest.getRealPath @Override 제거 또는 deprecated alias 로 변경
- type: code
- req_ids: [FR-FIX-007]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java, servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java:122-128, servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java, servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java]
- action: jakarta `ServletRequest.java` L124 의 `@Override` 가 5.0+ `ServletRequest` 인터페이스에 부재하는 메서드를 가리키는 결함 수정. jakarta 5.0 인터페이스 javadoc 확인 후 부재 시 메서드 자체를 제거하거나 `@Override` 만 제거하고 deprecated alias 로 유지 (javadoc + `@Deprecated`). jakarta `ServletContext.java` 의 `getRealPath` 도 동일 검토. 3개 모듈 (servlet5/6/61) 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:compileJava :servlet6:compileJava :servlet61:compileJava --quiet && ./gradlew :servlet5:test --tests *BM06* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM06*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM06*"}
- dod:
  - jakarta ServletRequest.getRealPath 의 `@Override` 가 제거되거나 메서드 자체 제거
  - 3개 모듈 컴파일 success
  - BM06 통과 (사용자 코드의 `jakarta.servlet.ServletRequest` 캐스트 후 getRealPath 호출이 IDE 컴파일 에러로 보고됨을 reflection 으로 검증)
- rollback: git diff 로 jakarta ServletRequest.java L124-127 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-09 FR-FIX-008 ServletReqResConverter instanceof 4방향 수정 (BM04)

- id: T-PH003-09
- phase_id: PH-003
- title: ServletReqResConverter 의 instanceof 분기 namespace 오류 4건 수정
- type: code
- req_ids: [FR-FIX-008]
- files: [servlet5/src/main/java/adapter/servletElementConverter5/ServletReqResConverter.java:10-28, servlet6/src/main/java/adapter/servletElementConverter6/ServletReqResConverter.java, servlet61/src/main/java/adapter/servletElementConverter61/ServletReqResConverter.java]
- action: `ServletReqResConverter.java` L14 의 `instanceof javax.servlet.http.HttpServletResponse` 가 jakarta 매개변수에 항상 false 인 결함. 정확한 분기는 `instanceof jakarta.servlet.http.HttpServletResponse` (양방향 모두 자기 namespace 검사). L18, L22, L26 의 4개 분기 모두 동일 검토·수정 — 각 메서드의 입력 매개변수 namespace 에 일치하는 instanceof 로 정정. 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM04* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM04*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM04*"}
- dod:
  - 4개 instanceof 분기가 입력 매개변수의 namespace 와 일치하도록 수정
  - 3개 모듈 모두 적용
  - BM04 통과 (4방향 instanceof 모두 정확 분기)
- rollback: git diff 로 ServletReqResConverter.java L14/L18/L22/L26 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-10 FR-FIX-009 HttpServletRequest.getCookies null 처리 (BM07)

- id: T-PH003-10
- phase_id: PH-003
- title: jakarta/javax HttpServletRequest.getCookies null 체크 + null 반환
- type: code
- req_ids: [FR-FIX-009]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpServletRequest.java:164-176, servlet5/src/main/java/adapter/javax/servlet5/http/HttpServletRequest.java, servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletRequest.java, servlet6/src/main/java/adapter/javax/servlet6/http/HttpServletRequest.java, servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletRequest.java, servlet61/src/main/java/adapter/javax/servlet61/http/HttpServletRequest.java]
- action: jakarta `HttpServletRequest.java` L166-174 의 `getCookies()` 메서드에 `Cookie[] cookies = this.httpRequest.getCookies(); if (cookies == null) return null;` 가드 추가. javax 측 대칭 적용. 빈 배열은 그대로 빈 배열 변환. 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM07* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM07*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM07*"}
- dod:
  - jakarta + javax HttpServletRequest.getCookies 의 null 가드 추가
  - 3개 모듈 모두 적용
  - BM07 통과 (mock 원본이 null 반환 시 어댑터도 null 반환)
  - 빈 배열은 빈 배열 변환 유지
- rollback: git diff 로 HttpServletRequest.java L166-174 복원.
- estimated_effort: S

#### §3.PH-003.T-PH003-11 BM 전체 회귀 검증 + servlet6/61 사본 동기화

- id: T-PH003-11
- phase_id: PH-003
- title: BM01~BM09 회귀 통과 + BM10 잠금 유지 + servlet6/61 결함 수정 사본 동기화 검증
- type: review
- req_ids: [FR-FIX-001, FR-FIX-002, FR-FIX-003, FR-FIX-004, FR-FIX-005, FR-FIX-006, FR-FIX-007, FR-FIX-008, FR-FIX-009, FR-TEST-004]
- files: [servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java, servlet6/src/test/java/com/snoworca/regression/BugMuseumTest.java, servlet61/src/test/java/com/snoworca/regression/BugMuseumTest.java]
- action: PH-003 의 모든 결함 수정 task 완료 후 `./gradlew :servlet5:test --tests *BugMuseum* :servlet6:test --tests *BugMuseum* :servlet61:test --tests *BugMuseum*` 실행. BM01~BM09 모두 통과 + BM10 잠금 유지 확인. servlet6 / servlet61 의 BugMuseumTest 는 servlet5 의 사본으로 동일 동작 검증 (BM05/BM06/BM07/BM08/BM09 6+ 사본 검증 — 6.0 신규 메서드 관련 결함은 별도). 미달 시 해당 결함 수정 task 로 재진입. 자동화 검증은 verification_cmd 에 위임.
- acceptance_tests: [{"kind": "checklist", "items": ["BM01~BM10 회귀가 servlet5 모듈에서 모두 통과 (./gradlew :servlet5:test --tests *BugMuseum* 출력 확인)", "FR-FIX-001~009 결함 9건이 servlet5 의 fix 커밋에 모두 반영됨을 git log 로 확인", "FR-FIX-007 servlet6/61 ServletContext 동기화 누락 없음 (Patch 2 의 files 가 적용됨)"]}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest :servlet6:test --tests com.snoworca.regression.BugMuseumTest :servlet61:test --tests com.snoworca.regression.BugMuseumTest", "windows": ".\\gradlew.bat :servlet5:test --tests com.snoworca.regression.BugMuseumTest :servlet6:test --tests com.snoworca.regression.BugMuseumTest :servlet61:test --tests com.snoworca.regression.BugMuseumTest"}
- dod:
  - 3개 모듈 모두에서 BM01~BM10 회귀 테스트 통과
  - PH-003 의 9개 결함 수정 task 가 모두 BM 회귀 케이스로 검증됨
  - servlet6 / servlet61 의 사본 동기화가 누락된 결함이 없음
- rollback: 본 검토에서 실패 발견 시 해당 결함 수정 task 재진입.
- estimated_effort: M

### §3.PH-004 변환 의미론 + 폴백 정책

#### §3.PH-004.T-PH004-01 AdapterUnwrap marker 인터페이스 신설 (common)

- id: T-PH004-01
- phase_id: PH-004
- title: common 모듈에 AdapterUnwrap<T> marker 인터페이스 신설
- type: code
- req_ids: [FR-CONV-003]
- files: [common/src/main/java/adapter/common/AdapterUnwrap.java]
- action: `common/src/main/java/adapter/common/AdapterUnwrap.java` 신설. `public interface AdapterUnwrap<T> { T unwrap(); }`. 모든 어댑터 클래스가 후속 task 에서 implements 한다.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :common:compileJava --quiet && test -f common/src/main/java/adapter/common/AdapterUnwrap.java", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :common:compileJava", "windows": ".\\gradlew.bat :common:compileJava"}
- dod:
  - `AdapterUnwrap<T>` 인터페이스가 common 에 존재
  - common 컴파일 성공
- rollback: AdapterUnwrap.java 삭제 — 단 후속 task 가 import 하므로 신중.
- estimated_effort: S

#### §3.PH-004.T-PH004-02 모든 어댑터 클래스에 AdapterUnwrap 구현 + ServletAdapter unwrap 사전 체크

- id: T-PH004-02
- phase_id: PH-004
- title: 모든 어댑터 클래스에 implements AdapterUnwrap<원본> + unwrap() 추가 + ServletAdapter 의 adaptToX 메서드에 instanceof AdapterUnwrap 사전 체크
- type: code
- req_ids: [FR-CONV-003, FR-CONV-004]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/, servlet5/src/main/java/adapter/javax/servlet5/, servlet5/src/main/java/com/snoworca/ServletAdapter.java, servlet6/src/main/java/adapter/jakarta/servlet6/, servlet6/src/main/java/adapter/javax/servlet6/, servlet6/src/main/java/com/snoworca/ServletAdapter.java, servlet61/src/main/java/adapter/jakarta/servlet61/, servlet61/src/main/java/adapter/javax/servlet61/, servlet61/src/main/java/com/snoworca/ServletAdapter.java]
- action: 모든 어댑터 클래스 (`adapter.jakarta.servletN.*` + `adapter.javax.servletN.*`) 에 `implements AdapterUnwrap<원본타입>` 추가 + `public 원본타입 unwrap() { return this.원본필드; }` 메서드 추가. equals / hashCode 도 FR-CONV-004 의 SPEC-6 패턴대로 unwrap 위임 + AdapterUnwrap instanceof 처리 패턴으로 override. ServletAdapter 의 모든 `adaptToJakarta / adaptToJavax` 메서드에 (1) `instanceof AdapterUnwrap` 사전 체크 + unwrap, (2) 반환 namespace 일치 시 원본 그대로 반환 (이중 wrap 회피), (3) 그렇지 않으면 새 어댑터 인스턴스 wrap 의 3단계 분기 추가.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *AdapterUnwrapTest* :servlet6:test --tests *AdapterUnwrapTest* :servlet61:test --tests *AdapterUnwrapTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *AdapterUnwrapTest*", "windows": ".\\gradlew.bat test --tests *AdapterUnwrapTest*"}
- dod:
  - 모든 어댑터 클래스가 `AdapterUnwrap<적절한_원본>` implements + unwrap 구현
  - 모든 어댑터 클래스의 equals/hashCode 가 SPEC-6 패턴 (unwrap 위임 + AdapterUnwrap chain)
  - ServletAdapter 의 14개 adaptToX 메서드가 instanceof + 이중 wrap 회피 분기 보유
  - `adaptToJavax(adaptToJakarta(javaxObj)) == javaxObj` unit test 통과
  - 동일 원본 wrap 한 두 어댑터 인스턴스가 equals + hashCode 일치
- rollback: AdapterUnwrap 구현을 모든 클래스에서 제거. equals/hashCode 복원.
- estimated_effort: L

#### §3.PH-004.T-PH004-03 FR-CONV-005 ServletException 양방향 변환 (try/catch 패턴)

- id: T-PH004-03
- phase_id: PH-004
- title: 모든 위임 메서드의 ServletException try/catch 양방향 변환 + cause chain
- type: code
- req_ids: [FR-CONV-005]
- files: [common/src/main/java/adapter/common/ConverterSupport.java, servlet5/src/main/java/adapter/jakarta/servlet5/, servlet5/src/main/java/adapter/javax/servlet5/, servlet6/src/main/java/adapter/jakarta/servlet6/, servlet6/src/main/java/adapter/javax/servlet6/, servlet61/src/main/java/adapter/jakarta/servlet61/, servlet61/src/main/java/adapter/javax/servlet61/]
- action: `common/ConverterSupport.java` 에 `javaxToJakartaServletException(javax.servlet.ServletException)` + `jakartaToJavaxServletException(jakarta.servlet.ServletException)` 헬퍼 추가 (각각 `new` 인스턴스 + `initCause(원본)`). 위임 메서드 중 `throws ServletException` 시그니처를 갖는 모든 메서드 (대표적으로 RequestDispatcher 의 forward/include, Filter chain doFilter 등) 에 try/catch 변환 패턴 적용. `IOException` / `IllegalStateException` / `UnsupportedOperationException` 등 표준 예외는 그대로 propagate (변환 없음). 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew test --tests *ServletExceptionConversionTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *ServletExceptionConversionTest*", "windows": ".\\gradlew.bat test --tests *ServletExceptionConversionTest*"}
- dod:
  - ConverterSupport 에 양방향 변환 헬퍼 2개
  - throws ServletException 메서드들이 try/catch 양방향 변환 + cause chain
  - IOException 등 표준 예외는 변환 없이 propagate
  - 단위 테스트로 양방향 변환 + cause 검증
- rollback: ConverterSupport 헬퍼 제거 + 위임 메서드의 try/catch 제거.
- estimated_effort: M

#### §3.PH-004.T-PH004-04 FR-CONV-006 + FR-CONV-009 + FR-CONV-007 / FR-CONV-008 SPEC 준수 검증 (보강)

- id: T-PH004-04
- phase_id: PH-004
- title: 컬렉션 eager snapshot + 부수효과 보존 + Stream/setContentLengthLong SPEC 준수 검증
- type: code
- req_ids: [FR-CONV-006, FR-CONV-007, FR-CONV-008, FR-CONV-009]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpServletRequest.java, servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java, servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java, servlet5/src/main/java/adapter/javax/servlet5/http/HttpServletRequest.java, servlet5/src/main/java/adapter/javax/servlet5/ServletContext.java, servlet5/src/main/java/adapter/servletElementConverter5/StreamConverter.java, servlet5/src/main/java/adapter/jakarta/servlet5/ServletResponse.java, servlet5/src/main/java/adapter/javax/servlet5/ServletResponse.java, servlet5/src/main/java/adapter/jakarta/servlet5/AsyncContext.java, servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpSession.java, servlet6/src/main/java/, servlet61/src/main/java/]
- action: PH-003 의 FIX 가 완료된 코드 위에서 추가 SPEC 검증. (1) FR-CONV-006: `getCookies/getParts/getServletRegistrations/getFilterRegistrations` 가 eager snapshot 으로 새 배열·컬렉션 반환하고 원본 null 시 null 반환. (2) FR-CONV-007 SPEC-12: ServletInputStream / ServletOutputStream 어댑터의 모든 시그니처 (read 시리즈, readLine, isFinished, isReady, setReadListener, write 시리즈, setWriteListener) 가 원본 동명 메서드로 정확 위임. (3) FR-CONV-008 SPEC-13: setContentLengthLong 가 원본 동명 위임 (이미 FR-FIX-003 완료, 단위 테스트로 잠금). (4) FR-CONV-009: flushBuffer/reset/resetBuffer/complete/invalidate/sendError/sendRedirect 가 원본 즉시 위임 (어댑터의 부수효과 지연·차단 없음). 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew test --tests *EagerSnapshotTest* --tests *StreamSpecTest* --tests *SideEffectTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *EagerSnapshotTest* --tests *StreamSpecTest* --tests *SideEffectTest*", "windows": ".\\gradlew.bat test --tests *EagerSnapshotTest* --tests *StreamSpecTest* --tests *SideEffectTest*"}
- dod:
  - getCookies/getParts/getServletRegistrations/getFilterRegistrations 가 eager snapshot 반환 + null pass-through
  - ServletInputStream / ServletOutputStream 의 9개 시그니처 (read 3개 + readLine + isFinished + isReady + setReadListener + write 3개 + setWriteListener) 모두 원본 동명 위임 verify
  - setContentLengthLong(Long.MAX_VALUE) 가 원본 동명 위임 verify (FR-FIX-003 의 이중 확인)
  - flushBuffer / reset / resetBuffer / complete / invalidate / sendError / sendRedirect 가 원본 즉시 위임 verify
- rollback: 각 단위 테스트만 제거 — 실제 위임 로직은 PH-003 의 FIX 와 PH-002 의 이동 코드에 이미 있음.
- estimated_effort: M

#### §3.PH-004.T-PH004-05 FR-CONV-011 strict 모드 (시스템 프로퍼티 + init-param)

- id: T-PH004-05
- phase_id: PH-004
- title: strict 모드 (lenient default + opt-in) 구현 (시스템 프로퍼티 + ServletContext init-param)
- type: code
- req_ids: [FR-CONV-011]
- files: [common/src/main/java/adapter/common/ConverterSupport.java, servlet5/src/main/java/com/snoworca/ServletAdapter.java, servlet6/src/main/java/com/snoworca/ServletAdapter.java, servlet61/src/main/java/com/snoworca/ServletAdapter.java]
- action: `ConverterSupport` 에 strict 모드 상태 관리 (`static volatile boolean strictMode = readSystemProperty("com.snoworca.adapter.strict")`) + ServletContext init-param 우선순위 (시스템 프로퍼티 우선, init-param fallback) 로직 추가. `throwIfStrict(message)` 헬퍼 추가 — strict 활성 시 `UnsupportedOperationException` throw (메시지 형식: `"Strict mode: {ClassName}.{methodName}({args}) is unsupported in this direction. Reason: {removed in jakarta 6.0 | absent in javax 4.0}"`). 모든 어댑터의 폴백 분기 (B/D/E/F) 가 본 헬퍼를 호출. `ServletRequest.getRequestId` / `getServletConnection` 은 strict 정책이 "F (동일)" 이므로 예외 없이 default 동작 유지. `getProtocolRequestId` 도 strict "D (동일)" 이므로 `""` 유지.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew test --tests *StrictModeTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *StrictModeTest*", "windows": ".\\gradlew.bat test --tests *StrictModeTest*"}
- dod:
  - ConverterSupport.strictMode 상태 관리 + 시스템 프로퍼티 + init-param 우선순위
  - throwIfStrict 헬퍼가 정확한 메시지 형식으로 throw
  - 시스템 프로퍼티 `com.snoworca.adapter.strict=true` 설정 시 HttpSession.getValue 호출이 UnsupportedOperationException throw 검증
  - 미설정 시 동일 호출이 attribute API 폴백 + WARN 검증
  - getRequestId / getServletConnection / getProtocolRequestId 는 strict 활성 시에도 default 유지 검증
- rollback: ConverterSupport 의 strict 관련 코드 제거 + 어댑터 클래스의 throwIfStrict 호출 제거.
- estimated_effort: M

#### §3.PH-004.T-PH004-06 FR-CONV-012 폴백 가시성 (로깅 dedup + diagnostics API)

- id: T-PH004-06
- phase_id: PH-004
- title: java.util.logging 기반 WARN/INFO dedup 로깅 + diagnostics() 정적 메서드
- type: code
- req_ids: [FR-CONV-012]
- files: [common/src/main/java/adapter/common/ConverterSupport.java, servlet5/src/main/java/com/snoworca/ServletAdapter.java, servlet6/src/main/java/com/snoworca/ServletAdapter.java, servlet61/src/main/java/com/snoworca/ServletAdapter.java]
- action: `ConverterSupport` 에 (1) `java.util.logging.Logger.getLogger("com.snoworca.servletadapter")` static 필드, (2) `ConcurrentHashMap<String, Boolean>` dedup map, (3) `logFallbackOnce(String methodKey, Level level, String message)` 메서드, (4) `ConcurrentHashMap<String, AtomicLong> diagnostics` counter, (5) `incrementDiagnostics(String methodKey)` 추가. 정책 B/D/F → WARN, 정책 E → INFO. dedup 은 once-per-method-key. `ServletAdapter.diagnostics()` 정적 메서드를 추가 (시스템 프로퍼티 `com.snoworca.adapter.diagnostics=true` 활성 시 Map 반환, 미설정 시 빈 Map). 모든 어댑터의 폴백 분기에서 이 헬퍼 호출.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew test --tests *DiagnosticsTest* --tests *LoggingDedupTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *DiagnosticsTest* --tests *LoggingDedupTest*", "windows": ".\\gradlew.bat test --tests *DiagnosticsTest* --tests *LoggingDedupTest*"}
- dod:
  - ConverterSupport 에 logFallbackOnce + incrementDiagnostics + 정책 B/D/E/F 매핑
  - ServletAdapter.diagnostics() 정적 메서드 (시스템 프로퍼티 opt-in)
  - WARN 로그가 polluted method 의 첫 호출 1회만 emit (반복 호출 시 추가 없음)
  - dedup ConcurrentHashMap thread-safe
  - diagnostics 활성 시 Map 반환, 비활성 시 빈 Map
- rollback: ConverterSupport 의 logging/diagnostics 헬퍼 제거 + ServletAdapter.diagnostics() 제거.
- estimated_effort: M

#### §3.PH-004.T-PH004-07 FR-CONV-010 폴백 정책 매트릭스 적용 (servlet6/61)

- id: T-PH004-07
- phase_id: PH-004
- title: FR-CONV-010 SPEC-7 폴백 정책 매트릭스 servlet6/61 어댑터 적용
- type: code
- req_ids: [FR-CONV-010, FR-CONV-014]
- files: [servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpSession.java, servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletResponse.java, servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletRequest.java, servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java, servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpSession.java, servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletResponse.java, servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletRequest.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java, servlet6/src/main/java/adapter/jakarta/servlet6/http/EmptyHttpSessionContext.java, servlet61/src/main/java/adapter/jakarta/servlet61/http/EmptyHttpSessionContext.java]
- action: SRS §6.2.2 표대로 정책 적용 — (1) HttpSession.getValue/putValue/removeValue/getValueNames jakarta→javax 방향 = 정책 B (attribute API 자동 폴백 + WARN once), (2) HttpSession.getSessionContext() jakarta→javax = 정책 D (EmptyHttpSessionContext 더미 + WARN), (3) HttpServletResponse.encodeUrl/encodeRedirectUrl jakarta→javax = 정책 E (대문자 alias 위임 + INFO log), (4) setStatus(int, String) jakarta→javax = 정책 E1 (setStatus(int) 호출 + WARN, reason phrase 폐기), (5) HttpServletRequest.isRequestedSessionIdFromUrl jakarta→javax = 정책 E (isRequestedSessionIdFromURL 위임 + INFO), (6) ServletContext.log(Exception, String) jakarta→javax = 정책 E (log(String, Throwable) 인자 재배치). servlet6 / servlet61 의 EmptyHttpSessionContext 더미 클래스 신설 (jakarta 6.0 에서 HttpSessionContext 삭제됨). servlet5 모듈은 5.0 인터페이스에 비대칭이 거의 없어 폴백 코드 dormant (FR-CONV-014).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet6:test --tests *FallbackPolicyTest* :servlet61:test --tests *FallbackPolicyTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet6:test --tests *FallbackPolicyTest* :servlet61:test --tests *FallbackPolicyTest*", "windows": ".\\gradlew.bat :servlet6:test --tests *FallbackPolicyTest* :servlet61:test --tests *FallbackPolicyTest*"}
- dod:
  - 6개 비대칭 메서드 그룹의 default 폴백 정책 정확 적용
  - strict 활성 시 UnsupportedOperationException throw (T-PH004-05 의 throwIfStrict 헬퍼 사용)
  - WARN/INFO 로그 dedup once-per-method
  - EmptyHttpSessionContext 더미 클래스가 servlet6 / servlet61 에 존재
  - servlet5 모듈은 폴백 분기 dormant 검증 (FR-CONV-014 AC-1)
  - servlet6 단위 테스트로 HttpSession.getValue 폴백 B 진입 검증 (FR-CONV-014 AC-2)
- rollback: 각 어댑터 클래스의 폴백 분기 코드 제거 + EmptyHttpSessionContext 삭제.
- estimated_effort: L

#### §3.PH-004.T-PH004-08 FR-CONV-013 Cookie attribute API 정책 (D: no-op + WARN once)

- id: T-PH004-08
- phase_id: PH-004
- title: javax Cookie 어댑터의 setAttribute/getAttribute/getAttributes 폴백 (no-op + WARN once)
- type: code
- req_ids: [FR-CONV-013]
- files: [servlet5/src/main/java/adapter/javax/servlet5/http/Cookie.java, servlet6/src/main/java/adapter/javax/servlet6/http/Cookie.java, servlet61/src/main/java/adapter/javax/servlet61/http/Cookie.java]
- action: javax `Cookie` 어댑터 (jakarta 6.0+ 신규 attribute API 가 노출되는 케이스) 에 (1) `setAttribute(String, String)` → no-op + ConverterSupport.logFallbackOnce(WARN) + ConverterSupport.incrementDiagnostics, (2) `getAttribute(String)` → `null` 반환, (3) `getAttributes()` → `Collections.emptyMap()` 반환. 자체 attribute Map 을 보관하지 않음. 3개 모듈 모두 적용. (단 javax 4 측 Cookie 가 jakarta 6 인터페이스로 wrap 되는 경우만 — 일반 jakarta 5 시나리오는 attribute API 자체가 없음.)
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew test --tests *CookieAttributePolicyTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *CookieAttributePolicyTest*", "windows": ".\\gradlew.bat test --tests *CookieAttributePolicyTest*"}
- dod:
  - setAttribute 호출 시 어댑터에 attribute Map 보관되지 않음 + WARN 로그 + diagnostics 카운터 증가
  - getAttribute 가 null 반환
  - getAttributes 가 empty unmodifiable Map 반환
  - 3개 모듈 모두 적용
- rollback: Cookie 어댑터의 attribute 메서드 구현 제거 (placeholder 로 복원).
- estimated_effort: S

#### §3.PH-004.T-PH004-09 FR-CONV-001 jakarta 6.0 신규 추상 메서드 구현 (getRequestId, getProtocolRequestId, getServletConnection) + EmptyHttpSessionContext

- id: T-PH004-09
- phase_id: PH-004
- title: servlet6 / servlet61 의 ServletRequest 신규 추상 메서드 구현 (request attribute lazy UUID + dummy ServletConnection + '' return)
- type: code
- req_ids: [FR-CONV-001]
- files: [servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java, servlet6/src/main/java/adapter/jakarta/servlet6/DummyServletConnection.java, servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java, servlet61/src/main/java/adapter/jakarta/servlet61/DummyServletConnection.java]
- action: servlet6 / servlet61 의 `adapter.jakarta.servletN.ServletRequest` 에 (1) `getRequestId()` → request attribute (key=`com.snoworca.adapter.requestId`) lazy UUID 캐싱 (정책 F), (2) `getProtocolRequestId()` → `""` 반환 (정책 D), (3) `getServletConnection()` → 어댑터 내부 `DummyServletConnection` 인스턴스 합성 반환 (정책 F) 구현. `DummyServletConnection.java` 신설 (getConnectionId/getProtocol/getProtocolConnectionId/isSecure 의 안전한 default 반환). 본 폴백은 strict 모드에서도 default 유지 (T-PH004-05 의 예외 분기).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet6:test --tests *RequestIdTest* :servlet61:test --tests *RequestIdTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet6:test --tests *RequestIdTest* :servlet61:test --tests *RequestIdTest*", "windows": ".\\gradlew.bat :servlet6:test --tests *RequestIdTest* :servlet61:test --tests *RequestIdTest*"}
- dod:
  - getRequestId 가 lazy UUID 캐싱 + 동일 요청 내 동일 UUID 반환
  - getProtocolRequestId 가 `""` 반환
  - getServletConnection 가 DummyServletConnection 합성 반환 + getConnectionId 비-null
  - strict 모드에서도 default 동작 유지 (예외 throw 안 함)
  - servlet6 + servlet61 두 모듈 모두 적용
- rollback: 신규 메서드 + DummyServletConnection 삭제 (placeholder 복원).
- estimated_effort: M

#### §3.PH-004.T-PH004-10 FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override

- id: T-PH004-10
- phase_id: PH-004
- title: servlet61 의 HttpServletResponse 에 sendRedirect(loc, sc, clearBuffer) 명시 override 구현
- type: code
- req_ids: [FR-CONV-001, FR-CONV-014]
- files: [servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletResponse.java]
- action: servlet61 의 `adapter.jakarta.servlet61.http.HttpServletResponse` 에 `sendRedirect(String location, int sc, boolean clearBuffer)` 메서드 명시 override 추가. 구현: (1) `this.response.setStatus(sc)`, (2) `this.response.setHeader("Location", location)`, (3) clearBuffer==true 이면 `this.response.resetBuffer()`, (4) `this.response.flushBuffer()`. javax 4.0 측에 본 시그니처가 없으므로 명시 합성. AbstractMethodError 회피.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet61:test --tests *SendRedirect61Test* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet61:test --tests *SendRedirect61Test*", "windows": ".\\gradlew.bat :servlet61:test --tests *SendRedirect61Test*"}
- dod:
  - servlet61 HttpServletResponse 의 sendRedirect(loc, sc, clearBuffer) 명시 override 존재
  - setStatus(sc) + setHeader("Location", loc) + (clearBuffer 시 resetBuffer) + flushBuffer 순서 verify
  - servlet61 단위 테스트 통과 (FR-CONV-014 AC-3)
- rollback: 본 메서드 제거 (단 servlet61 컴파일이 AbstractMethodError 위험 — placeholder 로 복원 필수).
- estimated_effort: S

#### §3.PH-004.T-PH004-11 NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10)

- id: T-PH004-11
- phase_id: PH-004
- title: 어댑터 final/thread-safe 검증 + HttpUpgradeHandler ThreadLocal 단일 스레드 가정 회귀 보호 (BM10)
- type: code
- req_ids: [NFR-CONV-001]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpUpgradeHandler.java, servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java]
- action: 어댑터 클래스의 인스턴스 필드를 모두 `final` 또는 thread-safe 타입으로 확인. diagnostics counter / dedup map 이 ConcurrentHashMap + AtomicLong 인지 PR review. BM10 회귀 테스트 보강 — HttpUpgradeHandler ThreadLocal pattern (`setJakartaUpgradeHandlerClass(...)` 가 호출된 스레드와 다른 스레드에서 `init()` 실행 시 잠금) 의 현 동작이 통과 + 향후 비동기 호출 시 즉시 실패하도록 작성. 3개 모듈 모두 적용.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *BM10* --tests *ThreadSafetyTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:test --tests *BM10* --tests *ThreadSafetyTest*", "windows": ".\\gradlew.bat :servlet5:test --tests *BM10* --tests *ThreadSafetyTest*"}
- dod:
  - 모든 어댑터 인스턴스 필드가 final 또는 thread-safe 타입
  - dedup / diagnostics 자료구조가 ConcurrentHashMap + AtomicLong
  - BM10 (ThreadLocal 단일 스레드 동기 호출 잠금) 통과
  - 향후 다중 스레드 호출로 동작 변경 시 BM10 즉시 실패 (회귀 보호)
- rollback: BM10 테스트만 수정 — 코드 변경은 PH-002 의 이동 결과 위에서 검증만.
- estimated_effort: M

#### §3.PH-004.T-PH004-12 PH-004 통합 회귀 + 폴백 정책 표 단위 테스트 매트릭스

- id: T-PH004-12
- phase_id: PH-004
- title: PH-004 폴백 정책 / strict 모드 / AdapterUnwrap / 로깅 / diagnostics 통합 회귀 + 단위 테스트 매트릭스 검증
- type: review
- req_ids: [FR-CONV-001, FR-CONV-002, FR-CONV-003, FR-CONV-004, FR-CONV-005, FR-CONV-010, FR-CONV-011, FR-CONV-012, FR-CONV-013, FR-CONV-014, NFR-CONV-001]
- files: [servlet5/src/test/java/, servlet6/src/test/java/, servlet61/src/test/java/]
- action: PH-004 의 12개 task 결과를 단위 테스트 매트릭스로 종합 검증. SRS §6.2.2 표의 모든 비대칭 메서드 그룹별로 default + strict 양쪽 동작 검증. FR-CONV-001 의 6.0/6.1 신규 추상 메서드 구현 완료 확인. FR-CONV-002 (위임 우선) 는 PH-005 의 FR-TEST-002 Reflection Contract Test 가 검증. 미달 시 해당 PH-004 task 재진입. 자동화 검증은 verification_cmd 에 위임.
- acceptance_tests: [{"kind": "checklist", "items": ["SPEC-1~16 변환 의미론이 servlet5/6/61 3개 모듈에서 행동 동등 (단위 테스트 매트릭스 통과)", "AdapterUnwrap marker 가 3 모듈 모두 적용 (이중 wrap 회피 검증)", "strict 모드 ON/OFF 양 경로 회귀 통과 (com.snoworca.adapter.strict=true|false)", "Cookie attribute API no-op + WARN once (FR-CONV-013) 동작 확인"]}]
- verification_cmd: {"posix": "./gradlew test", "windows": ".\\gradlew.bat test"}
- dod:
  - PH-004 의 12개 task 결과가 단위 테스트 매트릭스로 통합 검증
  - SRS §6.2.2 표의 모든 비대칭 메서드 그룹의 default + strict 동작 검증
  - 3개 모듈 모두 `./gradlew test` 통과
- rollback: 본 review 가 fail 한 task 로 재진입.
- estimated_effort: M

### §3.PH-005 테스트 인프라

#### §3.PH-005.T-PH005-01 FR-TEST-001 단위 테스트 (Mockito RETURNS_SMART_NULLS + verify) 전면 재작성

- id: T-PH005-01
- phase_id: PH-005
- title: 어댑터 클래스당 단위 테스트 클래스 신설 (Mockito RETURNS_SMART_NULLS) + 모든 위임 메서드 verify + 폴백 정책 분기 검증
- type: code
- req_ids: [FR-TEST-001]
- files: [servlet5/src/test/java/adapter/jakarta/servlet5/, servlet5/src/test/java/adapter/javax/servlet5/, servlet5/src/test/java/com/snoworca/, servlet6/src/test/java/adapter/jakarta/servlet6/, servlet6/src/test/java/adapter/javax/servlet6/, servlet61/src/test/java/adapter/jakarta/servlet61/, servlet61/src/test/java/adapter/javax/servlet61/]
- action: 현 `src/test/java/com/snoworca/ServletContextAdapterTest.java`, `ServletRequestResponseAdapterTest.java` (총 50 LOC, ~5% 커버리지) 를 servlet5 로 이동 후 전면 재작성. 각 어댑터 클래스당 1개 이상 단위 테스트 클래스 신설. mock 은 `withSettings().defaultAnswer(RETURNS_SMART_NULLS)` 로 생성. 모든 위임 메서드를 `verify(origin).동명메서드(...)` 로 검증. ArgumentCaptor 로 인자 변환 검증. 폴백 정책 (FR-CONV-010) 의 모든 정책 코드별 분기를 default + strict 양쪽으로 검증. servlet6 / servlet61 모듈도 동일 적용 (특히 6.0 신규 메서드 + 폴백 분기 + sendRedirect 61 명시 override).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew test --quiet && ./gradlew :servlet5:test :servlet6:test :servlet61:test --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --info", "windows": ".\\gradlew.bat test --info"}
- dod:
  - 3개 모듈 각각에 어댑터 클래스당 1개 이상의 단위 테스트 클래스 존재
  - 모든 위임 메서드가 verify 로 검증
  - 폴백 정책 (B/D/E/F) 의 default + strict 분기 모두 검증
  - mock 이 RETURNS_SMART_NULLS 로 생성됨 (stub 누락 시 즉시 검출)
- rollback: 신규 테스트 클래스 삭제. 0.0.1 의 ServletContextAdapterTest 만 servlet5 로 이동된 상태로 복원.
- estimated_effort: L

#### §3.PH-005.T-PH005-02 FR-TEST-002 DelegationContractTest (Reflection 기반) — common testFixtures

- id: T-PH005-02
- phase_id: PH-005
- title: common/src/testFixtures 에 DelegationContractTest driver 작성 + 각 버전 모듈에서 호출
- type: code
- req_ids: [FR-TEST-002]
- files: [common/build.gradle, common/src/testFixtures/java/com/snoworca/contract/DelegationContractTest.java, servlet5/src/test/java/com/snoworca/contract/Servlet5ContractTest.java, servlet6/src/test/java/com/snoworca/contract/Servlet6ContractTest.java, servlet61/src/test/java/com/snoworca/contract/Servlet61ContractTest.java]
- action: common/build.gradle 에 `apply plugin: 'java-test-fixtures'` 추가. `common/src/testFixtures/java/com/snoworca/contract/DelegationContractTest.java` 신설 — (1) 어댑터 spec 등록 API, (2) 인터페이스 메서드 enumerate (`Class.getMethods()`), (3) Object 메서드 + 변환 정책 메서드 blacklist, (4) sample args 합성, (5) 어댑터 호출 후 origin mock verify, (6) verifyNoMoreInteractions. 각 버전 모듈의 `Servlet5ContractTest` / `Servlet6ContractTest` / `Servlet61ContractTest` 가 본 driver 를 호출하여 자기 어댑터 spec 등록.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:test --tests *Servlet5ContractTest* :servlet6:test --tests *Servlet6ContractTest* :servlet61:test --tests *Servlet61ContractTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew test --tests *ContractTest*", "windows": ".\\gradlew.bat test --tests *ContractTest*"}
- dod:
  - DelegationContractTest driver 가 common testFixtures 에 존재
  - 3개 버전 모듈 각각이 driver 를 호출하는 ServletNContractTest 보유
  - 모든 어댑터 인터페이스 (HttpServletRequest, HttpServletResponse, HttpSession, ServletContext, ServletRequest, ServletResponse, Cookie, Part, AsyncContext 등) 의 모든 추상 메서드에 대해 위임 검증 자동 수행
  - 변환 정책 메서드는 blacklist 또는 별도 단위 테스트로 보완
- rollback: testFixtures 디렉토리 삭제 + 각 모듈의 ServletNContractTest 삭제.
- estimated_effort: L

#### §3.PH-005.T-PH005-03 FR-TEST-003 Tomcat embed 의존 추가 + TomcatRunner 헬퍼 3개 모듈

- id: T-PH005-03
- phase_id: PH-005
- title: 각 모듈에 Tomcat embed testImplementation + TomcatRunner 헬퍼 클래스 신설
- type: infra
- req_ids: [FR-TEST-003]
- files: [servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle, servlet5/src/test/java/com/snoworca/integration/TomcatRunner.java, servlet6/src/test/java/com/snoworca/integration/TomcatRunner.java, servlet61/src/test/java/com/snoworca/integration/TomcatRunner.java]
- action: servlet5/build.gradle 에 `testImplementation 'org.apache.tomcat.embed:tomcat-embed-core:10.0.27'` + javax-host 시나리오용 `testImplementation 'org.apache.tomcat.embed:tomcat-embed-core:9.0.85'` (별도 source set 또는 다른 test task — classpath 충돌 회피). servlet6/build.gradle 에 `tomcat-embed-core:10.1.30`. servlet61/build.gradle 에 `tomcat-embed-core:11.0.0`. 3개 모듈 모두 `testImplementation 'org.awaitility:awaitility:4.2.1'`. 각 모듈의 `TomcatRunner.java` 헬퍼 신설 — embedded Tomcat 의 start/stop + Servlet/Filter 등록 + 임의 포트 할당 + HTTP 클라이언트 헬퍼. 각 모듈은 자기 Tomcat 버전만 사용 (cross-version classpath 격리).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:compileTestJava :servlet6:compileTestJava :servlet61:compileTestJava --quiet && test -f servlet5/src/test/java/com/snoworca/integration/TomcatRunner.java && test -f servlet6/src/test/java/com/snoworca/integration/TomcatRunner.java && test -f servlet61/src/test/java/com/snoworca/integration/TomcatRunner.java", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:compileTestJava :servlet6:compileTestJava :servlet61:compileTestJava", "windows": ".\\gradlew.bat :servlet5:compileTestJava :servlet6:compileTestJava :servlet61:compileTestJava"}
- dod:
  - 3개 모듈의 testImplementation 에 Tomcat embed 의존 추가 (10.0.27 / 10.1.30 / 11.0.0)
  - servlet5 만 추가로 tomcat-embed-core:9.0.85 (javax-host) 보유 (별도 test task 또는 source set)
  - awaitility 4.2.1 3개 모듈 모두 추가
  - 3개 모듈의 TomcatRunner 헬퍼 클래스 존재
  - compileTestJava 성공
- rollback: build.gradle 의 testImplementation 행 제거 + TomcatRunner 삭제.
- estimated_effort: M

#### §3.PH-005.T-PH005-04 FR-TEST-003 IT01~IT15 통합 테스트 시나리오 작성

- id: T-PH005-04
- phase_id: PH-005
- title: IT01~IT15 통합 테스트 시나리오 작성 + @Tag('integration') + integrationTest task 분리
- type: code
- req_ids: [FR-TEST-003]
- files: [servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle, servlet5/src/test/java/com/snoworca/integration/, servlet6/src/test/java/com/snoworca/integration/, servlet61/src/test/java/com/snoworca/integration/]
- action: 각 모듈에 IT01~IT15 시나리오 작성. IT01 (GET + body + status), IT02 (POST body + getInputStream — F1 회귀), IT03 (Cookie 설정), IT04 (Cookie attribute SameSite=Lax 정보 손실 — jakarta→javax 만), IT05 (Session), IT06 (Filter chain), IT07 (AsyncContext.start + onComplete + awaitility), IT08 (MultiPart upload — F2 회귀), IT09 (sendRedirect(loc, 308) — 6.1 only), IT10 (Charset UTF-8 — F3 회귀), IT11 (setStatus(int, String) 폴백), IT12 (setContentLengthLong(Long.MAX_VALUE) — F5/F6 회귀), IT13 (ErrorPage dispatch), IT14 (getServletConnection 6.0 신규), IT15 (WebSocket Upgrade). 각 클래스에 `@Tag("integration")` 부여. 각 모듈 build.gradle 에 `integrationTest` 별도 task 추가 (기본 `test` task 에서 `@Tag("integration")` 제외).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:integrationTest :servlet6:integrationTest :servlet61:integrationTest --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:integrationTest :servlet6:integrationTest :servlet61:integrationTest --info", "windows": ".\\gradlew.bat :servlet5:integrationTest :servlet6:integrationTest :servlet61:integrationTest --info"}
- dod:
  - IT01~IT10 시나리오 (P0) 모두 작성 + 통과
  - IT11~IT15 시나리오 (P1) 작성 + 통과
  - IT09 는 servlet61 만, IT14 는 servlet6 + servlet61 만 적용 (cross-version skip)
  - `@Tag("integration")` 부여 + integrationTest task 분리
  - 각 모듈은 자기 Tomcat 버전만 사용 (classpath 충돌 없음)
- rollback: 각 IT 시나리오 클래스 삭제 + integrationTest task 제거.
- estimated_effort: L

#### §3.PH-005.T-PH005-05 FR-TEST-005 크로스 버전 호환성 시나리오 sharing + 비교

- id: T-PH005-05
- phase_id: PH-005
- title: 동일 시나리오 사본을 3개 모듈에서 병렬 실행하고 외부 관찰 결과 동등성 검증
- type: code
- req_ids: [FR-TEST-005]
- files: [common/src/testFixtures/java/com/snoworca/scenario/CrossVersionScenarios.java, servlet5/src/test/java/com/snoworca/integration/CrossVersionTest.java, servlet6/src/test/java/com/snoworca/integration/CrossVersionTest.java, servlet61/src/test/java/com/snoworca/integration/CrossVersionTest.java]
- action: common 의 testFixtures 에 `CrossVersionScenarios.java` 신설 — IT01 / IT03 / IT05 의 동일 시나리오 입력·기대 출력 정의. 각 버전 모듈의 `CrossVersionTest.java` 가 본 fixture 를 호출하여 자기 Tomcat embed 에서 실행하고 결과를 stdout 또는 파일 기록. CI 매트릭스 (PH-006) 가 3개 모듈 결과를 비교한다. 본 task 는 cross-version classpath 공유를 시도하지 않음 (별도 빌드 매트릭스 = CI 셀로 격리).
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:integrationTest --tests *CrossVersionTest* :servlet6:integrationTest --tests *CrossVersionTest* :servlet61:integrationTest --tests *CrossVersionTest* --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew integrationTest --tests *CrossVersionTest*", "windows": ".\\gradlew.bat integrationTest --tests *CrossVersionTest*"}
- dod:
  - CrossVersionScenarios fixture 가 common testFixtures 에 존재
  - 각 버전 모듈의 CrossVersionTest 가 IT01/IT03/IT05 의 동일 시나리오 실행
  - 3개 모듈에서 status / body / Content-Type / Set-Cookie / JSESSIONID 비교 가능한 결과 출력
  - PH-006 의 CI 가 결과를 한 보고서에 통합
- rollback: CrossVersionScenarios + CrossVersionTest 삭제.
- estimated_effort: M

#### §3.PH-005.T-PH005-06 NFR-TEST-001 JaCoCo 게이트 (95%/100%) 적용

- id: T-PH005-06
- phase_id: PH-005
- title: 각 모듈에 JaCoCo 플러그인 + 커버리지 게이트 (라인 95% / 분기 100% for converter, 90% for adapter) 적용
- type: infra
- req_ids: [NFR-TEST-001]
- files: [common/build.gradle, servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle, gradle.properties]
- action: 각 모듈 build.gradle 에 `apply plugin: 'jacoco'` + `jacocoTestReport` + `jacocoTestCoverageVerification` 블록 추가. violationRules — (1) `adapter.{jakarta,javax}.**` 단순 위임 코드는 라인 ≥95%, 분기 ≥90%, (2) `adapter.servletElementConverter*.**` + `adapter.common.**` 은 라인 ≥95%, 분기 ≥100% (enum/instanceof silent fallback 방지), (3) 예외 변환 try/catch 는 라인 100%. `./gradlew check` 가 본 task 를 자동 실행하여 미달 시 빌드 실패. gradle.properties 에 JaCoCo 버전 (0.8.11) 명시.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew :servlet5:jacocoTestCoverageVerification :servlet6:jacocoTestCoverageVerification :servlet61:jacocoTestCoverageVerification :common:jacocoTestCoverageVerification --quiet", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew jacocoTestCoverageVerification", "windows": ".\\gradlew.bat jacocoTestCoverageVerification"}
- dod:
  - 4개 모듈 (common + servlet5/6/61) 에 jacoco 플러그인 + violationRules 보유
  - `./gradlew check` 시 커버리지 게이트 실행
  - PR 미달 시 빌드 실패
  - 1.0.0 출시 시점 모든 모듈이 게이트 통과
- rollback: 각 build.gradle 의 jacoco 블록 제거.
- estimated_effort: M

#### §3.PH-005.T-PH005-07 PH-005 통합 검토 (단위 + contract + integration + cross-version + jacoco)

- id: T-PH005-07
- phase_id: PH-005
- title: PH-005 의 6개 task 결과 통합 검토 — 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과
- type: review
- req_ids: [FR-TEST-001, FR-TEST-002, FR-TEST-003, FR-TEST-005, NFR-TEST-001]
- files: []
- action: PH-005 의 6개 task 완료 후 `./gradlew check` + `./gradlew integrationTest` 통합 실행. 단위 테스트 + contract test + integration test + cross-version test + jacoco 게이트 모두 통과 확인. 미달 항목은 해당 task 로 재진입. 자동화 검증은 verification_cmd 에 위임.
- acceptance_tests: [{"kind": "checklist", "items": ["IT01~IT15 Tomcat embed 통합 테스트 모두 통과 (10.0.27 + 10.1.30 + 11.0.0 3개 매트릭스)", "JaCoCo 라인 커버리지가 NFR-TEST-001 의 게이트 (예: 80%) 도달", "BM10 회귀 포함 확인 (Bug Museum 10건)"]}]
- verification_cmd: {"posix": "./gradlew check integrationTest", "windows": ".\\gradlew.bat check integrationTest"}
- dod:
  - `./gradlew check integrationTest` 가 5개 모듈 (common + 4개 + bom) 에서 모두 통과
  - PH-005 의 6개 task 결과가 통합 검증
- rollback: 본 검토 fail 시 해당 task 재진입.
- estimated_effort: S

### §3.PH-006 CI 매트릭스 + JaCoCo

#### §3.PH-006.T-PH006-01 OPS-TEST-001 GitHub Actions ci.yml 작성 (build-matrix + integration-tests + coverage)

- id: T-PH006-01
- phase_id: PH-006
- title: .github/workflows/ci.yml 작성 — build-matrix + integration-tests + coverage
- type: infra
- req_ids: [OPS-TEST-001]
- files: [.github/workflows/ci.yml]
- action: `.github/workflows/ci.yml` 신설 — (1) `build-matrix` job: OS=[ubuntu-latest, windows-latest] × servlet-line=[5.0(JDK 8), 6.0(JDK 11), 6.1(JDK 17)] 6셀. 각 셀 `actions/setup-java@v4` (multiple JDKs) + Gradle cache `actions/cache@v4` + `./gradlew :servletN:test` (단위 + contract). 실패 시 test report artifact upload. (2) `integration-tests` job: OS=ubuntu-latest, servlet-line=[5.0, 6.0, 6.1] 3셀. build-matrix 의존. `./gradlew :servletN:integrationTest`. (3) `coverage` job: ubuntu-latest, JDK 17. `./gradlew jacocoTestReport` + `codecov/codecov-action@v4` 업로드. 70.testing-strategy.md §13.1 yaml 기준. wall clock ~5분 이내 목표.
- acceptance_tests: [{"kind": "file_state", "path": ".github/workflows/ci.yml", "exists": true}, {"kind": "shell", "cmd": "yq '.jobs.test.strategy.matrix' .github/workflows/ci.yml", "expected_exit": 0, "stdout_regex": "(?s).*(servlet5|servlet6|servlet61).*"}]
- verification_cmd: null
- dod:
  - `.github/workflows/ci.yml` 이 main 브랜치에 존재
  - PR 푸시 시 build-matrix (6셀) + integration-tests (3셀) + coverage (1셀) 모두 병렬·순차 실행
  - 빌드 시간 wall clock ~5분 이내 (캐시 hit 가정)
  - 통합 테스트는 Linux only, Windows 는 단위 + contract 만
  - Codecov 또는 GitHub PR 댓글에 coverage 보고서 표시
  - PR 푸시 후 GitHub Actions 가 모든 매트릭스 셀 실행 + 5분 이내 wall clock + Codecov 보고서 업로드 확인 (외부 인프라 의존, 첫 PR 검증으로 확인)
- rollback: ci.yml 삭제. 로컬 빌드는 영향 없음.
- estimated_effort: M

#### §3.PH-006.T-PH006-02 OPS-TEST-001 CI 매트릭스 첫 PR 검증 + 시간 측정

- id: T-PH006-02
- phase_id: PH-006
- title: 첫 PR 으로 CI 매트릭스 실행 검증 + 시간 측정 + 실패 셀 분석
- type: review
- req_ids: [OPS-TEST-001]
- files: []
- action: T-PH006-01 의 ci.yml 을 main 브랜치에 머지 후 첫 PR 생성. 모든 매트릭스 셀이 병렬 실행되고 통과함을 확인. wall clock 시간 측정 (~5분 목표). 실패 셀이 있으면 분석 — toolchain JDK 다운로드 / Tomcat embed Windows 호환성 / cache miss 등. 실패 케이스 별 후속 task 생성 또는 ci.yml 수정.
- acceptance_tests: [{"kind": "checklist", "cmd": "첫 PR 의 GitHub Actions 모든 셀 green + wall clock < 6분 + 실패 셀 없음", "expected_exit": 0}]
- verification_cmd: null
- dod:
  - 첫 PR 의 모든 GitHub Actions 매트릭스 셀이 통과
  - wall clock 시간이 5분 ± 1분 범위
  - 실패 셀 분석 + 후속 task (필요 시)
- rollback: 본 review 가 fail 한 셀에 대해 ci.yml 수정 + 재실행.
- estimated_effort: S

#### §3.PH-006.T-PH006-03 NFR-TEST-001 + OPS-TEST-001 JaCoCo 보고서 + Codecov 연동 검증

- id: T-PH006-03
- phase_id: PH-006
- title: JaCoCo 보고서 PR 댓글 + Codecov 업로드 + 게이트 95%/100% 적용 검증
- type: infra
- req_ids: [NFR-TEST-001, OPS-TEST-001]
- files: [.github/workflows/ci.yml]
- action: T-PH006-01 의 coverage job 이 JaCoCo XML 보고서를 생성하고 Codecov 또는 GitHub PR 댓글로 업로드함을 검증. 게이트 95%/100% 미달 시 PR 빌드 실패 확인. PR 머지 차단 게이트 활성화 (GitHub branch protection rules).
- acceptance_tests: [{"kind": "file_state", "path": "build.gradle", "exists": true}, {"kind": "shell", "cmd": "./gradlew jacocoTestReport", "expected_exit": 0}]
- verification_cmd: null
- dod:
  - 각 PR 의 coverage job 이 모듈별 JaCoCo XML 생성
  - Codecov 또는 PR 댓글에 coverage 표시
  - 의도적 게이트 미달 PR 이 빌드 실패 (검증 PR 1건)
  - GitHub branch protection rules 에 coverage 게이트 등록
  - Codecov 또는 PR 댓글에 모듈별 coverage 표시 + 의도적 게이트 위반 PR 이 빌드 실패하는지 검증 (외부 인프라 의존)
- rollback: ci.yml 의 coverage job 또는 branch protection rules 제거.
- estimated_effort: S

### §3.PH-007 Maven Central 게시

#### §3.PH-007.T-PH007-01 IR-REL-001 maven-publish + signing 플러그인 적용 (5개 모듈)

- id: T-PH007-01
- phase_id: PH-007
- title: 각 모듈 build.gradle 에 maven-publish + signing 플러그인 + publication 정의
- type: code
- req_ids: [IR-REL-001, FR-MOD-007]
- files: [common/build.gradle, servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle, bom/build.gradle]
- action: 5개 모듈 build.gradle 에 `apply plugin: 'maven-publish'` + `apply plugin: 'signing'` 추가. 각 모듈에 publication 정의 — `publishing { publications { mavenJava(MavenPublication) { artifactId 'jakarta-to-javax-servlet-adapter-{name}', from components.java (또는 components.javaPlatform for bom), pom { name/description/url/licenses/developers/scm } } } repositories { maven { name 'OSSRH', url ossrhUrl, credentials } } }`. signing 블록은 `useGpgCmd()` 또는 `useInMemoryPgpKeys(env.GPG_SIGNING_KEY, env.GPG_PASSPHRASE)` + `sign publishing.publications.mavenJava`. version 1.0.0 동기.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew publishToMavenLocal --quiet && ls ~/.m2/repository/com/clipsoft/jakarta-to-javax-servlet-adapter-*/1.0.0/ | wc -l", "expected_exit": 0, "expected_stdout_regex": "^[5-9]"}]
- verification_cmd: {"posix": "./gradlew publishToMavenLocal", "windows": ".\\gradlew.bat publishToMavenLocal"}
- dod:
  - 5개 모듈 모두 maven-publish + signing 플러그인 적용
  - `./gradlew publishToMavenLocal` 결과로 5개 jar (POM + GPG signature 포함) 생성
  - 5개 artifactId 가 `jakarta-to-javax-servlet-adapter-{common,servlet5,servlet6,servlet61,bom}` 형식
  - 모든 모듈 version=1.0.0 동기
- rollback: 각 build.gradle 의 publishing/signing 블록 제거.
- estimated_effort: M

#### §3.PH-007.T-PH007-02 FR-REL-001 README 호환성 매트릭스 + 정보 손실 5개 항목 작성

- id: T-PH007-02
- phase_id: PH-007
- title: README.md 전면 개정 — 호환성 매트릭스 + Cookie attribute 손실 + setStatus reason phrase 폐기 + getValue alias + strict 모드 + 0.x 마이그레이션
- type: doc
- req_ids: [FR-REL-001]
- files: [README.md]
- action: README.md 전면 개정. (1) 호환성 매트릭스 표: -servlet5 (5.0.x, javax 4.0.x, Java 8, Tomcat 10.0.x), -servlet6 (6.0.x, javax 4.0.x, Java 11, Tomcat 10.1.x), -servlet61 (6.1.x, javax 4.0.x, Java 17, Tomcat 11.x). (2) Cookie attribute API setAttribute("SameSite", ...) 의 정보 손실 정책 (FR-CONV-013). (3) setStatus(int, String) reason phrase 폐기 (FR-CONV-010). (4) HttpSession.getValue/putValue 의 attribute API 자동 폴백 (FR-CONV-010). (5) strict 모드 활성화 방법 (시스템 프로퍼티 + init-param 우선순위, FR-CONV-011). (6) 0.0.1 사용자 마이그레이션 안내 (T-PH007-04 의 alias 정책 결정 후 채움). 영어로 작성 (현 README 일관성).
- acceptance_tests: [{"kind": "file_state", "cmd": "grep -E '(servlet5|servlet6|servlet61|SameSite|strict mode|0\\.0\\.1)' README.md | wc -l", "expected_exit": 0, "expected_stdout_regex": "^[6-9]|^[1-9][0-9]"}]
- verification_cmd: null
- dod:
  - README.md 에 호환성 매트릭스 표 존재
  - Cookie attribute / setStatus reason phrase / getValue alias / strict 모드 / 0.x 마이그레이션 5개 항목 모두 문서화
  - 한국어 또는 영어 일관 (현 README = 영어)
- rollback: README.md 의 신규 섹션 제거 → 0.0.1 README 로 복원.
- estimated_effort: M

#### §3.PH-007.T-PH007-03 CHANGELOG 작성 (0.0.1 → 1.0.0)

- id: T-PH007-03
- phase_id: PH-007
- title: CHANGELOG.md 작성 — 0.0.1 → 1.0.0 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 추가 + 폴백 정책
- type: doc
- req_ids: [FR-REL-001]
- files: [CHANGELOG.md]
- action: `CHANGELOG.md` 신설 또는 갱신 — `## [1.0.0] - 2026-MM-DD` 섹션에 (1) 결함 수정 9건 (FR-FIX-001~009 의 결함 ID + 1줄 요약), (2) 신규 multi-module 구조 (common/servlet5/6/61/bom), (3) 6.0/6.1 동시 지원 추가 (AbstractMethodError 회피), (4) 폴백 정책 변경 (Cookie attribute D, strict opt-in, setStatus reason phrase 폐기), (5) 새 진입점 (com.snoworca.ServletAdapter5/6/61 별칭). Keep a Changelog 1.1.0 형식.
- acceptance_tests: [{"kind": "file_state", "cmd": "grep -E '(1\\.0\\.0|BM01|FR-FIX-|multi-module|servlet5)' CHANGELOG.md | wc -l", "expected_exit": 0, "expected_stdout_regex": "^[5-9]|^[1-9][0-9]"}]
- verification_cmd: null
- dod:
  - CHANGELOG.md 가 신설되거나 1.0.0 섹션 추가
  - 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 + 폴백 정책 변경 + 신규 진입점 5개 항목 명시
  - Keep a Changelog 형식
- rollback: CHANGELOG.md 의 1.0.0 섹션 제거.
- estimated_effort: S

#### §3.PH-007.T-PH007-04 MIG-REL-001 0.x → 1.0 마이그레이션 가이드 + alias 구현 (OQ-001 결정 반영)

- id: T-PH007-04
- phase_id: PH-007
- title: 마이그레이션 가이드 작성 + OQ-001 결정에 따른 alias artifact 별도 게시 (옵션 A) 또는 servlet5 내 deprecated alias 패키지 (옵션 B) 구현
- type: code
- req_ids: [MIG-REL-001]
- files: [docs/migration/0.x-to-1.0.md, servlet5/src/main/java/adapter/jakarta/servlet/, servlet5/src/main/java/adapter/javax/servlet/, servlet5/src/main/java/adapter/servletElementConverter/]
- action: PH-001 의 T-PH001-01 결과 (OQ-001 결정) 를 반영하여 옵션 A 또는 B 를 구현. **옵션 A 선택 시**: `jakarta-to-javax-servlet-adapter:0.x.y` 별도 artifact 를 Maven Central 에 추가 게시 — 0.0.1 패키지명 (`adapter.jakarta.servlet.*`) 유지. 본 plan 의 PH-002 이동을 reverse 한 별도 빌드 라인. **옵션 B 선택 시**: servlet5 모듈에 `adapter.jakarta.servlet.*`, `adapter.javax.servlet.*`, `adapter.servletElementConverter.*` (0.x 패키지명) deprecated alias 클래스 추가 — 각 alias 가 `adapter.jakarta.servlet5.*` 등의 실제 클래스를 상속 또는 위임. `@Deprecated(forRemoval = true, since = "1.0.0")` + javadoc 에 v2.0 제거 예고. `docs/migration/0.x-to-1.0.md` 신설 — import 변경 안내 + 의존 좌표 변경 + alias 사용 예시 + v2.0 제거 일정.
- acceptance_tests: [{"kind": "shell", "cmd": "if grep -q 'option A' docs/research/80.decisions.md; then echo 'Option A: external artifact'; ls -la 0.x-build/ 2>/dev/null || echo 'pending external build'; else ./gradlew :servlet5:compileJava --quiet && find servlet5/src/main/java/adapter/jakarta/servlet -maxdepth 4 -name '*.java' | wc -l; fi", "expected_exit": 0}]
- verification_cmd: {"posix": "./gradlew :servlet5:compileJava && cat docs/migration/0.x-to-1.0.md | head -20", "windows": ".\\gradlew.bat :servlet5:compileJava && type docs\\migration\\0.x-to-1.0.md"}
- dod:
  - OQ-001 결정에 따른 옵션 A 또는 B 구현 완료
  - 옵션 A: 별도 artifact `jakarta-to-javax-servlet-adapter:0.x.y` Maven Central 게시
  - 옵션 B: servlet5 모듈에 0.x 패키지명 @Deprecated alias 클래스 + javadoc
  - 0.0.1 사용자가 import 변경 없이 1.0.0 의존 업그레이드 시 컴파일 success
  - docs/migration/0.x-to-1.0.md 마이그레이션 가이드 작성
  - 본 REQ 의 verification 은 OQ-001 종결 후 4 주 이내 완료
- rollback: 옵션 B 의 alias 클래스 제거 또는 옵션 A 의 별도 빌드 라인 제거. 마이그레이션 가이드는 보존.
- estimated_effort: L

#### §3.PH-007.T-PH007-05 OPS-REL-001 GPG 서명 + GitHub Secrets 설정

- id: T-PH007-05
- phase_id: PH-007
- title: GPG 서명 키 적용 + GitHub Secrets 설정
- type: infra
- req_ids: [OPS-REL-001]
- files: [.github/workflows/release.yml, common/build.gradle, servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle, bom/build.gradle]
- action: T-PH001-03 (OQ-003 Sonatype/GPG 외부 트래킹) 의 결과로 자격증명 준비 완료 확인. 각 모듈 build.gradle 의 signing 블록을 `useInMemoryPgpKeys(System.getenv('GPG_SIGNING_KEY'), System.getenv('GPG_PASSPHRASE'))` 패턴으로 설정 (또는 useGpgCmd). `.github/workflows/release.yml` 신설 — tag push (`v*`) 트리거. release job 이 OSSRH_USERNAME/PASSWORD + GPG_SIGNING_KEY/PASSPHRASE 환경 변수로 publish task 실행. README 에 GPG fingerprint 명시.
- acceptance_tests: [{"kind": "file_state", "path": ".github/workflows/release.yml", "exists": true}, {"kind": "shell", "cmd": "grep -c 'OSSRH_USERNAME\\|GPG_SIGNING_KEY' .github/workflows/release.yml", "expected_exit": 0}]
- verification_cmd: null
- dod:
  - GitHub Secrets 4개 등록 (UI 에서 확인)
  - 각 모듈의 signing 블록이 환경 변수 기반
  - .github/workflows/release.yml 이 tag push 트리거
  - README 에 GPG fingerprint 명시
  - GitHub Secrets 4개 (OSSRH_USERNAME, OSSRH_PASSWORD, GPG_SIGNING_KEY, GPG_PASSPHRASE) 등록 확인 (외부 의존 — Secrets 등록은 GitHub UI 수동 작업)
- rollback: GitHub Secrets 비활성화 + signing 블록 제거 + release.yml 삭제.
- estimated_effort: M

#### §3.PH-007.T-PH007-06 OPS-REL-001 publishAll task + staging upload 검증

- id: T-PH007-06
- phase_id: PH-007
- title: publishAll task + Sonatype staging repository 업로드 + promote (Maven Central)
- type: infra
- req_ids: [OPS-REL-001]
- files: [build.gradle, common/build.gradle, servlet5/build.gradle, servlet6/build.gradle, servlet61/build.gradle, bom/build.gradle]
- action: root build.gradle 에 `task publishAll` 정의 — 5개 모듈의 publish task 를 모두 호출 + Sonatype staging close + promote. `io.github.gradle-nexus.publish-plugin` 또는 동등 플러그인 적용 권장. 본 task 의 첫 실행은 SNAPSHOT 으로 dry-run 후 1.0.0 정식 release 로 진행.
- acceptance_tests: [{"kind": "shell", "cmd": "./gradlew publishAllPublicationsToStagingRepository --dry-run", "expected_exit": 0, "stdout_regex": "(?s).*common.*servlet5.*servlet6.*servlet61.*bom.*"}]
- verification_cmd: null
- dod:
  - publishAll task 가 root 에 등록
  - 5개 모듈이 Sonatype staging repository 에 업로드
  - promote 후 Maven Central 검색에서 5개 artifact 노출
  - GPG signature 검증 통과
  - ./gradlew publishAll --dry-run 으로 5개 모듈 staging URL 출력 확인 후 정식 publish 시 Sonatype staging UI 에서 5개 artifact 확인 + promote 후 Maven Central 검색 결과 노출 (외부 의존)
- rollback: publishAll task 제거 + staging repository drop. Maven Central promote 는 되돌리기 불가 (1.0.0 은 영구) — 신중.
- estimated_effort: L

#### §3.PH-007.T-PH007-07 1.0.0 release tag + 최종 회귀

- id: T-PH007-07
- phase_id: PH-007
- title: v1.0.0 git tag + GitHub Release 작성 + 최종 회귀 (BM + IT + cross-version 전수)
- type: pr
- req_ids: [IR-REL-001, FR-REL-001, MIG-REL-001, OPS-REL-001]
- files: [CHANGELOG.md, README.md]
- action: 모든 PH-002~PH-006 완료 후 main 브랜치에서 `git tag v1.0.0 -m "v1.0.0 Maven Central release"` + `git push origin v1.0.0`. T-PH007-05 의 release.yml 이 자동 트리거되어 publish 실행. GitHub Releases UI 에서 v1.0.0 release 작성 — CHANGELOG 의 1.0.0 섹션 + 호환성 매트릭스 + 5개 artifact 좌표 + 마이그레이션 가이드 링크. PR review 후 머지. 마지막으로 `./gradlew test integrationTest jacocoTestCoverageVerification` 통과 + BM01~BM10 통과 + IT01~IT15 통과 + cross-version 동등성 통과 최종 회귀 확인.
- acceptance_tests: [{"kind": "checklist", "cmd": "git tag v1.0.0 + GitHub Release 작성 + Maven Central 게시 확인 + 최종 회귀 BM/IT/cross-version 모두 green + 5개 artifact 좌표가 README 와 일치", "expected_exit": 0}]
- verification_cmd: {"posix": "git tag --list v1.0.0 && ./gradlew test integrationTest jacocoTestCoverageVerification", "windows": "git tag --list v1.0.0 && .\\gradlew.bat test integrationTest jacocoTestCoverageVerification"}
- dod:
  - v1.0.0 git tag 가 main 에 생성·푸시됨
  - GitHub Release v1.0.0 작성 + CHANGELOG + 호환성 매트릭스 + 마이그레이션 가이드 링크
  - Maven Central 에 5개 artifact 게시 완료
  - 최종 회귀 (BM01~BM10 + IT01~IT15 + cross-version) 모두 통과
  - PR review + 머지 완료
- rollback: git tag 삭제 (`git tag -d v1.0.0 && git push --delete origin v1.0.0`). 단 Maven Central 1.0.0 은 영구 — 1.0.1 hotfix 로만 정정.
- estimated_effort: M

## §4 REQ ↔ Task 역색인

| req_id | stability | task_ids | ac_covered/ac_total |
|---|---|---|---|
| FR-MOD-001 | unset | T-PH002-01, T-PH002-02 | 4/4 |
| FR-MOD-002 | unset | T-PH002-03, T-PH002-04, T-PH002-05, T-PH002-06 | 3/3 |
| FR-MOD-003 | unset | T-PH002-04, T-PH002-05, T-PH002-06 | 4/4 |
| FR-MOD-004 | unset | T-PH002-03 | 3/3 |
| FR-MOD-005 | unset | T-PH002-04, T-PH002-05, T-PH002-06 | 2/2 |
| FR-MOD-006 | unset | T-PH002-04, T-PH002-05, T-PH002-06 | 3/3 |
| FR-MOD-007 | unset | T-PH002-07, T-PH007-01 | 2/2 |
| CON-MOD-001 | unset | T-PH002-08 | 3/3 |
| NFR-MOD-001 | unset | T-PH001-02, T-PH002-09 | 2/2 |
| FR-CONV-001 | unset | T-PH004-09, T-PH004-10, T-PH004-12 | 2/2 |
| FR-CONV-002 | unset | T-PH004-12, T-PH005-02 | 2/2 |
| FR-CONV-003 | unset | T-PH004-01, T-PH004-02, T-PH004-12 | 4/4 |
| FR-CONV-004 | unset | T-PH004-02, T-PH004-12 | 3/3 |
| FR-CONV-005 | unset | T-PH004-03, T-PH004-12 | 3/3 |
| FR-CONV-006 | unset | T-PH004-04, T-PH003-10 | 3/3 |
| FR-CONV-007 | unset | T-PH004-04, T-PH003-02 | 9/9 |
| FR-CONV-008 | unset | T-PH004-04, T-PH003-04 | 2/2 |
| FR-CONV-009 | unset | T-PH004-04 | 7/7 |
| FR-CONV-010 | unset | T-PH004-07, T-PH004-12 | 3/3 |
| FR-CONV-011 | unset | T-PH004-05, T-PH004-12 | 4/4 |
| FR-CONV-012 | unset | T-PH004-06, T-PH004-12 | 3/3 |
| FR-CONV-013 | unset | T-PH004-08, T-PH005-04 | 4/4 |
| NFR-CONV-001 | unset | T-PH004-11, T-PH003-01 | 3/3 |
| FR-CONV-014 | unset | T-PH004-07, T-PH004-10, T-PH004-12 | 3/3 |
| FR-FIX-001 | unset | T-PH003-02, T-PH003-11 | 4/4 |
| FR-FIX-002 | unset | T-PH003-03, T-PH003-11 | 4/4 |
| FR-FIX-003 | unset | T-PH003-04, T-PH003-11 | 3/3 |
| FR-FIX-004 | unset | T-PH003-05, T-PH003-11 | 4/4 |
| FR-FIX-005 | unset | T-PH003-06, T-PH003-11 | 3/3 |
| FR-FIX-006 | unset | T-PH003-07, T-PH003-11 | 2/2 |
| FR-FIX-007 | unset | T-PH003-08, T-PH003-11 | 3/3 |
| FR-FIX-008 | unset | T-PH003-09, T-PH003-11 | 4/4 |
| FR-FIX-009 | unset | T-PH003-10, T-PH003-11 | 2/2 |
| FR-TEST-001 | unset | T-PH005-01, T-PH005-07 | 4/4 |
| FR-TEST-002 | unset | T-PH005-02, T-PH005-07 | 4/4 |
| FR-TEST-003 | unset | T-PH005-03, T-PH005-04, T-PH005-07 | 5/5 |
| FR-TEST-004 | unset | T-PH003-01, T-PH003-11 | 5/5 |
| FR-TEST-005 | unset | T-PH005-05, T-PH005-07 | 4/4 |
| NFR-TEST-001 | unset | T-PH005-06, T-PH006-03, T-PH005-07 | 3/3 |
| OPS-TEST-001 | unset | T-PH006-01, T-PH006-02, T-PH006-03 | 4/4 |
| IR-REL-001 | unset | T-PH002-01, T-PH002-02, T-PH007-01, T-PH007-07 | 3/3 |
| FR-REL-001 | unset | T-PH007-02, T-PH007-03, T-PH007-07 | 3/3 |
| MIG-REL-001 | unset | T-PH001-01, T-PH007-04, T-PH007-07 | 4/4 |
| OPS-REL-001 | unset | T-PH001-03, T-PH007-05, T-PH007-06, T-PH007-07 | 4/4 |

## §5 위험 · 미해결

### 5.1 위험

| risk_id | severity | mitigation | affected_task_ids |
|---|---|---|---|
| RISK-001 | high | servlet6 / servlet61 분리 유지 (D2 결정) 로 `AbstractMethodError` 회피. FR-CONV-001 의 sendRedirect(loc, sc, clearBuffer) 명시 override (T-PH004-10) 로 6.1 추상 메서드 누락 차단. | T-PH002-05, T-PH002-06, T-PH004-09, T-PH004-10 |
| RISK-002 | high | OQ-003 의 외부 트래킹 (T-PH001-03) 을 PH-001 부터 시작하여 PH-007 진입 전까지 완비. Sonatype/GPG 미준비 시 PH-007 차단 명시. | T-PH001-03, T-PH007-05, T-PH007-06 |
| RISK-003 | medium | README 호환성 매트릭스 + 정보 손실 5개 항목 (T-PH007-02) + 마이그레이션 가이드 (T-PH007-04) 작성. 0.0.1 사용자가 자기 환경 호환성을 사전 확인 가능. | T-PH007-02, T-PH007-04 |
| RISK-004 | medium | servlet6/61 사본의 결함 수정 동기화 누락 위험. T-PH003-11 의 통합 회귀 검토에서 3개 모듈 모두 BM01~BM10 통과 강제. | T-PH003-02, T-PH003-03, T-PH003-04, T-PH003-05, T-PH003-06, T-PH003-07, T-PH003-08, T-PH003-09, T-PH003-10, T-PH003-11 |
| RISK-005 | medium | `./gradlew check integrationTest` 시간 + Tomcat embed 3가지 버전 시작·정지로 CI wall clock 5분 초과 가능. T-PH006-02 의 시간 측정 + Gradle cache 최적화로 mitigation. | T-PH006-01, T-PH006-02 |
| RISK-006 | low | Java Toolchain 으로 JDK 8/11/17 자동 다운로드 — Windows CI 셀에서 네트워크 / 인증서 이슈 가능. T-PH006-02 의 첫 PR 검증에서 발견 후 조치. | T-PH006-01, T-PH006-02 |

### 5.2 Open Questions

| id | question | blocks_task_ids |
|---|---|---|
| OQ-001 | 0.x → 1.0 마이그레이션 alias 정책 (옵션 A 별도 alias artifact 게시 vs 옵션 B servlet5 내 deprecated alias 패키지) — MIG-REL-001 영향. v1.0.0 출시 직전 결정 필요. | T-PH007-04 |
| OQ-002 | JPMS module-info.java 본격 도입 시점 — NFR-MOD-002 (v1.1.0 이연). v1.0.0 에서는 NFR-MOD-001 의 Automatic-Module-Name 만 처리. | (none — deferred 확인만) |
| OQ-003 | Sonatype OSSRH 또는 Central Portal 계정 + `com.clipsoft` namespace 점유 + GPG 키 등록 상태 — OPS-REL-001 영향. 외부 의존, 코딩과 병렬 트래킹. | T-PH007-05, T-PH007-06, T-PH007-07 |

### 5.3 unreferenced_reqs

(비어있음)

### 5.4 deferred_ac

(비어있음 — 모든 AC 가 Task 커버리지에 포함됨. 본 target 의 stability 가 모두 unset 이므로 frozen/stable 게이트는 발동하지 않으나, AC 단위 커버리지를 목표로 작성.)

## §6 부록

### 6.1 사이드카 JSON 경로 / md_sha256

- 사이드카 경로: `./2026-05-19-jakarta-adapter-v1-0-0.sidecar.json` (본 plan.md 와 동일 디렉토리)
- md_sha256: `TBD` (Phase 4 validator.mjs 가 자동 갱신)

### 6.2 검증 스크립트 실행 방법

- 빌드 전수: `./gradlew clean build` (5개 모듈)
- 단위 + contract: `./gradlew test` (전 모듈)
- 통합: `./gradlew integrationTest` (Linux only)
- 커버리지 게이트: `./gradlew jacocoTestCoverageVerification`
- Bug Museum 회귀: `./gradlew test --tests com.snoworca.regression.BugMuseumTest`
- Maven Local 게시 dry-run: `./gradlew publishToMavenLocal`
- 모듈 의존 그래프 검증: `./gradlew validateNoVersionModuleCrossDeps`
- Phase 4 validator (외부): `node tools/kiwi-planner/validator.mjs --plan docs/plans/2026-05-19-jakarta-adapter-v1-0-0.plan.md --sidecar docs/plans/2026-05-19-jakarta-adapter-v1-0-0.sidecar.json`

### 6.3 mcp_call_log 요약

본 Phase (Phase 2 plan drafting) 에서는 SRS mutation MCP 호출 금지 (§0.G1 황금률). `add_trace_link` / `add_verification_evidence` 는 Phase 5 (Mutation + Report) 에서만 실행. 본 plan 의 sidecar.json `mcp_call_log` 는 빈 배열로 초기화됨.
