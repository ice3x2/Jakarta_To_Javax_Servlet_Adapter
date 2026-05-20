---
run_id: 2026-05-19-jakarta-adapter-v1-0-0-v03
target: v1.0.0
plan_version: 0.3.0
plan_contract: "1.2.0"
generated_at: 2026-05-19T15:30:00Z
tool_versions:
  speckiwi: 2.2.2
  kiwi_planner: 0.6.0
  validator: 0.6.0
stability_summary:
  frozen: 0
  stable: 0
  evolving: 32
  draft: 0
tdd_policy: relaxed
sidecar_path: ./2026-05-19-jakarta-adapter-v1-0-0-v03.sidecar.json
md_sha256: "9b3125fa9ac5c537740e55ee5e594f086e57a76daa7889eebcc45ebcd0d8a595"
---

# v1.0.0 잔여 작업 구현 계획 (v03)

## §1 개요

### 1.1 목표

Jakarta ↔ Javax Servlet Adapter v1.0.0 의 잔여 작업 (PH-003 잔여 결함 6건 페어 + PH-004 변환 의미론 + PH-005 테스트 인프라 + PH-006 CI + PH-007 Maven Central 게시) 을 v02 의 완료 20 task 위에 이어서 수행한다. 본 v03 은 v02 plan 의 53 잔여 task 를 입력으로 받아 `--draft-policy=exclude-draft` 정책으로 draft-only task 7건을 drop 한 46 task / 5 phase / 32 evolving REQ 의 정형 계획이다.

### 1.2 범위 (in_scope[])

- 5 phase: PH-003 (결함 6건 페어), PH-004 (변환 의미론), PH-005 (테스트 인프라), PH-006 (CI 매트릭스), PH-007 (Maven Central publish)
- 46 task: code 38건 (TDD red/green 페어 18건 + green-only 2건 + exempt 2건), review 3건, doc 2건, infra 2건, pr 1건
- 32 evolving REQ (FR-FIX-001~009, FR-CONV-001~014, FR-MOD-007, FR-REL-001, FR-TEST-001/002/004, NFR-CONV-001, NFR-TEST-001, IR-REL-001, MIG-REL-001)

### 1.3 제외사항 (out_of_scope[], excluded_reqs 포함)

- **PH-001 + PH-002 + PH-003 (T-PH003-01~08)** — v02 에서 이미 완료 (20 task). v03 은 잔여 phase 만 다룬다.
- **4 draft REQ** (`--draft-policy=exclude-draft`):
  - FR-TEST-003 (jqwik property-based testing, v1.1.0 이연)
  - FR-TEST-005 (fuzz testing, v1.1.0 이연)
  - OPS-TEST-001 (PIT mutation testing per D6, v1.1.0 이연)
  - OPS-REL-001 (Sonatype/GPG/Secrets 외부 인프라, v1.1.0 이연)
- **2 draft REQ carry-over** (v02 sidecar 인계):
  - NFR-MOD-002 (JPMS module-info.java, v1.1.0 per OQ-002)
  - OPS-TEST-002 (추가 mutation testing 항목)
- **7 draft-only task drop**: T-PH005-03, T-PH005-04, T-PH005-05, T-PH006-01, T-PH006-02, T-PH007-05, T-PH007-06

### 1.4 전제조건 / 가정

- v02 의 20 completed task 가 git working tree 에 반영되어 있음 (BugMuseumTest 15 case, 3 모듈 토폴로지, FR-FIX-001/002/003 green 완료, 4 jar Automatic-Module-Name + archivesName 적용)
- speckiwi MCP v2.2.2 가용, active_target=v1.0.0
- kiwi-coder state 재개 시 v03 신규 session 으로 시작 (run-id 변경) — v02 state.json 의 completed_task_ids 는 본 계획에서 in_scope 가 아님
- gradle 8.10.2 + Microsoft JDK 8/11/17 toolchain 셋업 완료

## §2 Phase 목록

| phase_id | title | goal | depends_on | task_count |
|---|---|---|---|---|
| PH-003 | 결함 9건 + Bug Museum (TDD red+green 페어) | FR-FIX-001~009 + FR-TEST-004 의 red 테스트 선행 → green 구현 페어 분해 (10 페어 = 20 task) + BM 통합 review | - | 13 |
| PH-004 | 변환 의미론 + 폴백 정책 (TDD red+green 페어) | SPEC-1~16 + AdapterUnwrap + strict + 폴백 가시성 + Cookie attribute + servlet6/61 신규 메서드 — 11 페어 = 22 task + 통합 review | PH-003 | 23 |
| PH-005 | 테스트 인프라 | 단위 테스트 (Mockito) + Reflection Contract Test + Tomcat embed IT01~IT15 + 크로스버전 + JaCoCo 게이트 | PH-004 | 4 |
| PH-006 | CI 매트릭스 + JaCoCo | GitHub Actions ci.yml (build-matrix + integration-tests + coverage) | PH-005 | 1 |
| PH-007 | Maven Central 게시 | maven-publish + signing + BOM POM + README 호환성 매트릭스 + CHANGELOG + 0.x alias (D8 옵션 B) + GPG/Sonatype + 1.0.0 staging upload | PH-006 | 5 |

## §3 Task 상세

### §3.PH-003 결함 9건 + Bug Museum (TDD red+green 페어)

**Goal**: FR-FIX-001~009 + FR-TEST-004 의 red 테스트 선행 → green 구현 페어 분해 (10 페어 = 20 task) + BM 통합 review  
**Depends on**: -  
**Task count**: 13

#### §3.PH-003.T-PH003-09 FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding) — TDD red (failing test)

- id: T-PH003-09
- phase_id: PH-003
- title: FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding) — TDD red (failing test)
- type: code
- req_ids: [FR-FIX-004]
- files: [servlet5/src/test/java/adapter/jakarta/servlet5/ServletContextEncodingDefectTest.java]
- action: ServletContextEncodingDefectTest.java 신설. jakarta ServletContext.setResponseCharacterEncoding 호출 시 원본의 setResponseCharacterEncoding 이 호출되는지 verify (현 코드는 setRequestCharacterEncoding 으로 잘못 호출). AC-1~AC-4 — direction_correct / no_request_method_invoked / origin_response_method_called / cross_module_consistency.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX004* --quiet", expected_exit=1, stdout_regex="FAILED.*FR-FIX-004"}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX004*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX004*"}
- dod:
  - red 단계: FR-FIX-004 의 4개 AC test case 작성됨 (현 결함 코드에서 모두 실패)
  - BM03 회귀 케이스가 본 task 의 test_cases 와 매핑됨
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/adapter/jakarta/servlet5/ServletContextEncodingDefectTest.java 삭제.
- estimated_effort: S
- tdd: {applicable: true, phase: "red", test_cases_count: 4}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-10 FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding) — TDD green (impl fix)

- id: T-PH003-10
- phase_id: PH-003
- title: FR-FIX-004 jakarta setResponseCharacterEncoding 교차 호출 (→setRequestCharacterEncoding) — TDD green (impl fix)
- type: code
- req_ids: [FR-FIX-004]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java,servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java,servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java]
- action: jakarta ServletContext.java L338 의 this.setRequestCharacterEncoding(arg0) (자기 메서드 + 잘못된 방향) 을 this.servletContext.setResponseCharacterEncoding(arg0) (원본 동명) 로 수정. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX004* :servlet6:test --tests *FRFIX004* :servlet61:test --tests *FRFIX004* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX004* :servlet6:test --tests *FRFIX004* :servlet61:test --tests *FRFIX004*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX004*"}
- dod:
  - green 단계: FR-FIX-004 의 4개 test case 가 모두 통과(green)
  - BM03 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 FR-FIX-004 수정 reverse — red task 로 복귀.
- estimated_effort: S
- depends_on_task: [T-PH003-09]
- tdd: {applicable: true, phase: "green", test_cases_count: 4}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-11 FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭) — TDD red (failing test)

- id: T-PH003-11
- phase_id: PH-003
- title: FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭) — TDD red (failing test)
- type: code
- req_ids: [FR-FIX-005]
- files: [servlet5/src/test/java/adapter/javax/servlet5/ServletContextEncodingDefectTest.java]
- action: javax ServletContextEncodingDefectTest.java 신설. javax ServletContext.setRequestCharacterEncoding 호출 시 원본의 setRequestCharacterEncoding 호출 verify (현재는 setResponseCharacterEncoding 으로 잘못 호출). AC-1~AC-3 — direction_correct / no_response_method_invoked / origin_request_method_called.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX005* --quiet", expected_exit=1, stdout_regex="FAILED.*FR-FIX-005"}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX005*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX005*"}
- dod:
  - red 단계: FR-FIX-005 의 3개 AC test case 작성됨 (현 결함 코드에서 모두 실패)
  - BM08 회귀 케이스가 본 task 의 test_cases 와 매핑됨
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/adapter/javax/servlet5/ServletContextEncodingDefectTest.java 삭제.
- estimated_effort: S
- tdd: {applicable: true, phase: "red", test_cases_count: 3}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-12 FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭) — TDD green (impl fix)

- id: T-PH003-12
- phase_id: PH-003
- title: FR-FIX-005 javax setRequestCharacterEncoding 교차 호출 (→setResponseCharacterEncoding, 대칭) — TDD green (impl fix)
- type: code
- req_ids: [FR-FIX-005]
- files: [servlet5/src/main/java/adapter/javax/servlet5/ServletContext.java,servlet6/src/main/java/adapter/javax/servlet6/ServletContext.java,servlet61/src/main/java/adapter/javax/servlet61/ServletContext.java]
- action: javax ServletContext.java L363 의 this.setResponseCharacterEncoding(encoding) 을 this.servletContext.setRequestCharacterEncoding(encoding) 로 수정. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX005* :servlet6:test --tests *FRFIX005* :servlet61:test --tests *FRFIX005* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX005* :servlet6:test --tests *FRFIX005* :servlet61:test --tests *FRFIX005*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX005*"}
- dod:
  - green 단계: FR-FIX-005 의 3개 test case 가 모두 통과(green)
  - BM08 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 FR-FIX-005 수정 reverse — red task 로 복귀.
- estimated_effort: S
- depends_on_task: [T-PH003-11]
- tdd: {applicable: true, phase: "green", test_cases_count: 3}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-13 FR-FIX-006 getContentLengthLong fallback 부정확 (try/catch + int fallback) — TDD red (failing test)

- id: T-PH003-13
- phase_id: PH-003
- title: FR-FIX-006 getContentLengthLong fallback 부정확 (try/catch + int fallback) — TDD red (failing test)
- type: code
- req_ids: [FR-FIX-006]
- files: [servlet5/src/test/java/adapter/jakarta/servlet5/ContentLengthDefectTest.java]
- action: ContentLengthDefectTest.java 신설. ServletRequest.getContentLengthLong(Long.MAX_VALUE) 호출 시 원본의 getContentLengthLong long 결과 그대로 반환 verify. 현 코드는 try/catch fallback 에서 int 반환. AC-1~AC-2 — long_precision_preserved / no_int_fallback.
- covers_ac: [AC-1, AC-2]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX006* --quiet", expected_exit=1, stdout_regex="FAILED.*FR-FIX-006"}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX006*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX006*"}
- dod:
  - red 단계: FR-FIX-006 의 2개 AC test case 작성됨 (현 결함 코드에서 모두 실패)
  - BM09 회귀 케이스가 본 task 의 test_cases 와 매핑됨
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/adapter/jakarta/servlet5/ContentLengthDefectTest.java 삭제.
- estimated_effort: S
- tdd: {applicable: true, phase: "red", test_cases_count: 2}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-14 FR-FIX-006 getContentLengthLong fallback 부정확 (try/catch + int fallback) — TDD green (impl fix)

- id: T-PH003-14
- phase_id: PH-003
- title: FR-FIX-006 getContentLengthLong fallback 부정확 (try/catch + int fallback) — TDD green (impl fix)
- type: code
- req_ids: [FR-FIX-006]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java,servlet5/src/main/java/adapter/javax/servlet5/ServletRequest.java,servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java,servlet6/src/main/java/adapter/javax/servlet6/ServletRequest.java,servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java,servlet61/src/main/java/adapter/javax/servlet61/ServletRequest.java]
- action: jakarta ServletRequest.java L173-179 의 try/catch + getContentLength() int fallback 을 제거하고 return this.request.getContentLengthLong() 단순 위임으로 수정. javax 측 대칭 검토. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX006* :servlet6:test --tests *FRFIX006* :servlet61:test --tests *FRFIX006* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX006* :servlet6:test --tests *FRFIX006* :servlet61:test --tests *FRFIX006*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX006*"}
- dod:
  - green 단계: FR-FIX-006 의 2개 test case 가 모두 통과(green)
  - BM09 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 FR-FIX-006 수정 reverse — red task 로 복귀.
- estimated_effort: S
- depends_on_task: [T-PH003-13]
- tdd: {applicable: true, phase: "green", test_cases_count: 2}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-15 FR-FIX-007 jakarta ServletRequest.getRealPath @Override 부재 메서드 — TDD red (failing test)

- id: T-PH003-15
- phase_id: PH-003
- title: FR-FIX-007 jakarta ServletRequest.getRealPath @Override 부재 메서드 — TDD red (failing test)
- type: code
- req_ids: [FR-FIX-007]
- files: [servlet5/src/test/java/adapter/jakarta/servlet5/GetRealPathDefectTest.java]
- action: GetRealPathDefectTest.java 신설. jakarta ServletRequest.getRealPath 가 jakarta 5.0+ 인터페이스에 부재 → @Override 제거 또는 메서드 자체 제거 verify (reflection 기반). AC-1~AC-3 — no_override_annotation / method_absent_or_deprecated_only / cross_module_consistency.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX007* --quiet", expected_exit=1, stdout_regex="FAILED.*FR-FIX-007"}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX007*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX007*"}
- dod:
  - red 단계: FR-FIX-007 의 3개 AC test case 작성됨 (현 결함 코드에서 모두 실패)
  - BM06 회귀 케이스가 본 task 의 test_cases 와 매핑됨
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/adapter/jakarta/servlet5/GetRealPathDefectTest.java 삭제.
- estimated_effort: S
- tdd: {applicable: true, phase: "red", test_cases_count: 3}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-16 FR-FIX-007 jakarta ServletRequest.getRealPath @Override 부재 메서드 — TDD green (impl fix)

- id: T-PH003-16
- phase_id: PH-003
- title: FR-FIX-007 jakarta ServletRequest.getRealPath @Override 부재 메서드 — TDD green (impl fix)
- type: code
- req_ids: [FR-FIX-007]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/ServletContext.java,servlet5/src/main/java/adapter/jakarta/servlet5/ServletRequest.java,servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java,servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java,servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java,servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java]
- action: jakarta ServletRequest.java L124 의 @Override 가 5.0+ ServletRequest 인터페이스에 부재하는 메서드를 가리키는 결함 수정. jakarta 5.0 인터페이스 javadoc 확인 후 부재 시 메서드 자체를 제거하거나 @Override 만 제거하고 deprecated alias 로 유지. jakarta ServletContext.java 의 getRealPath 도 동일 검토. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX007* :servlet6:test --tests *FRFIX007* :servlet61:test --tests *FRFIX007* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX007* :servlet6:test --tests *FRFIX007* :servlet61:test --tests *FRFIX007*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX007*"}
- dod:
  - green 단계: FR-FIX-007 의 3개 test case 가 모두 통과(green)
  - BM06 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 FR-FIX-007 수정 reverse — red task 로 복귀.
- estimated_effort: S
- depends_on_task: [T-PH003-15]
- tdd: {applicable: true, phase: "green", test_cases_count: 3}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-17 FR-FIX-008 ServletReqResConverter instanceof 분기 namespace 오류 4건 — TDD red (failing test)

- id: T-PH003-17
- phase_id: PH-003
- title: FR-FIX-008 ServletReqResConverter instanceof 분기 namespace 오류 4건 — TDD red (failing test)
- type: code
- req_ids: [FR-FIX-008]
- files: [servlet5/src/test/java/adapter/servletElementConverter5/InstanceofBranchDefectTest.java]
- action: InstanceofBranchDefectTest.java 신설. ServletReqResConverter 의 4개 분기 (L14/L18/L22/L26) 가 입력 매개변수의 namespace 와 일치하는 instanceof 로 분기되는지 verify. 현 코드는 javax 매개변수에 jakarta instanceof 또는 그 반대로 항상 false. AC-1~AC-4 — jakarta_input_jakarta_check / javax_input_javax_check / no_cross_namespace / cross_module_consistency.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX008* --quiet", expected_exit=1, stdout_regex="FAILED.*FR-FIX-008"}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX008*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX008*"}
- dod:
  - red 단계: FR-FIX-008 의 4개 AC test case 작성됨 (현 결함 코드에서 모두 실패)
  - BM04 회귀 케이스가 본 task 의 test_cases 와 매핑됨
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/adapter/servletElementConverter5/InstanceofBranchDefectTest.java 삭제.
- estimated_effort: S
- tdd: {applicable: true, phase: "red", test_cases_count: 4}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-18 FR-FIX-008 ServletReqResConverter instanceof 분기 namespace 오류 4건 — TDD green (impl fix)

- id: T-PH003-18
- phase_id: PH-003
- title: FR-FIX-008 ServletReqResConverter instanceof 분기 namespace 오류 4건 — TDD green (impl fix)
- type: code
- req_ids: [FR-FIX-008]
- files: [servlet5/src/main/java/adapter/servletElementConverter5/ServletReqResConverter.java,servlet6/src/main/java/adapter/servletElementConverter6/ServletReqResConverter.java,servlet61/src/main/java/adapter/servletElementConverter61/ServletReqResConverter.java]
- action: ServletReqResConverter.java L14 의 instanceof javax.servlet.http.HttpServletResponse 가 jakarta 매개변수에 항상 false 인 결함. 정확한 분기는 instanceof jakarta.servlet.http.HttpServletResponse. L18, L22, L26 의 4개 분기 모두 동일 검토·수정 — 각 메서드의 입력 매개변수 namespace 에 일치하는 instanceof 로 정정. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX008* :servlet6:test --tests *FRFIX008* :servlet61:test --tests *FRFIX008* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX008* :servlet6:test --tests *FRFIX008* :servlet61:test --tests *FRFIX008*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX008*"}
- dod:
  - green 단계: FR-FIX-008 의 4개 test case 가 모두 통과(green)
  - BM04 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 FR-FIX-008 수정 reverse — red task 로 복귀.
- estimated_effort: S
- depends_on_task: [T-PH003-17]
- tdd: {applicable: true, phase: "green", test_cases_count: 4}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-19 FR-FIX-009 HttpServletRequest.getCookies null 미처리 — TDD red (failing test)

- id: T-PH003-19
- phase_id: PH-003
- title: FR-FIX-009 HttpServletRequest.getCookies null 미처리 — TDD red (failing test)
- type: code
- req_ids: [FR-FIX-009]
- files: [servlet5/src/test/java/adapter/jakarta/servlet5/http/GetCookiesNullDefectTest.java]
- action: GetCookiesNullDefectTest.java 신설. jakarta + javax HttpServletRequest.getCookies() 호출 시 원본이 null 반환하면 어댑터도 null 반환 verify. 현 코드는 null cookies 배열 순회 시 NPE. AC-1~AC-2 — null_passthrough / empty_array_preserved.
- covers_ac: [AC-1, AC-2]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX009* --quiet", expected_exit=1, stdout_regex="FAILED.*FR-FIX-009"}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX009*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX009*"}
- dod:
  - red 단계: FR-FIX-009 의 2개 AC test case 작성됨 (현 결함 코드에서 모두 실패)
  - BM07 회귀 케이스가 본 task 의 test_cases 와 매핑됨
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/adapter/jakarta/servlet5/http/GetCookiesNullDefectTest.java 삭제.
- estimated_effort: S
- tdd: {applicable: true, phase: "red", test_cases_count: 2}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-20 FR-FIX-009 HttpServletRequest.getCookies null 미처리 — TDD green (impl fix)

- id: T-PH003-20
- phase_id: PH-003
- title: FR-FIX-009 HttpServletRequest.getCookies null 미처리 — TDD green (impl fix)
- type: code
- req_ids: [FR-FIX-009]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpServletRequest.java,servlet5/src/main/java/adapter/javax/servlet5/http/HttpServletRequest.java,servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletRequest.java,servlet6/src/main/java/adapter/javax/servlet6/http/HttpServletRequest.java,servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletRequest.java,servlet61/src/main/java/adapter/javax/servlet61/http/HttpServletRequest.java]
- action: jakarta HttpServletRequest.java L166-174 의 getCookies() 메서드에 Cookie[] cookies = this.httpRequest.getCookies(); if (cookies == null) return null; 가드 추가. javax 측 대칭 적용. 빈 배열은 그대로 빈 배열 변환. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2]
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *FRFIX009* :servlet6:test --tests *FRFIX009* :servlet61:test --tests *FRFIX009* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests *FRFIX009* :servlet6:test --tests *FRFIX009* :servlet61:test --tests *FRFIX009*", windows: ".\gradlew.bat :servlet5:test --tests *FRFIX009*"}
- dod:
  - green 단계: FR-FIX-009 의 2개 test case 가 모두 통과(green)
  - BM07 회귀 케이스 통과 — 3 모듈 (servlet5/6/61) 모두
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 FR-FIX-009 수정 reverse — red task 로 복귀.
- estimated_effort: S
- depends_on_task: [T-PH003-19]
- tdd: {applicable: true, phase: "green", test_cases_count: 2}
- evaluation_topology: simplified

#### §3.PH-003.T-PH003-21 BM01~BM10 회귀 통과 + servlet6/61 사본 동기화 검증

- id: T-PH003-21
- phase_id: PH-003
- title: BM01~BM10 회귀 통과 + servlet6/61 사본 동기화 검증
- type: review
- req_ids: [FR-FIX-001,FR-FIX-002,FR-FIX-003,FR-FIX-004,FR-FIX-005,FR-FIX-006,FR-FIX-007,FR-FIX-008,FR-FIX-009,FR-TEST-004]
- files: [servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java,servlet6/src/test/java/com/snoworca/regression/BugMuseumTest.java,servlet61/src/test/java/com/snoworca/regression/BugMuseumTest.java]
- action: PH-003 의 모든 결함 수정 페어 (T-PH003-03~T-PH003-20) 완료 후 ./gradlew :servlet5:test --tests *BugMuseum* :servlet6:test --tests *BugMuseum* :servlet61:test --tests *BugMuseum* 실행. BM01~BM09 모두 통과 + BM10 잠금 유지 확인. servlet6/servlet61 의 BugMuseumTest 는 servlet5 의 사본으로 동일 동작 검증. 미달 시 해당 fix 페어의 green task 로 재진입.
- acceptance_tests: {checklist: 3 items}
- verification_cmd: {posix: "./gradlew :servlet5:test --tests com.snoworca.regression.BugMuseumTest :servlet6:test --tests com.snoworca.regression.BugMuseumTest :servlet61:test --tests com.snoworca.regression.BugMuseumTest", windows: ".\gradlew.bat :servlet5:test --tests com.snoworca.regression.BugMuseumTest"}
- dod:
  - 3개 모듈 모두에서 BM01~BM10 회귀 테스트 통과
  - PH-003 의 10개 결함 수정 페어가 모두 BM 회귀 케이스로 검증
  - servlet6 / servlet61 의 사본 동기화가 누락된 결함이 없음
- rollback: 본 검토에서 실패 발견 시 해당 fix 페어의 green task 재진입.
- estimated_effort: M
- tdd: {applicable: false, phase: "n/a", exempt_reason: "auto-exempt by task type"}
- evaluation_topology: file_op

### §3.PH-004 변환 의미론 + 폴백 정책 (TDD red+green 페어)

**Goal**: SPEC-1~16 + AdapterUnwrap + strict + 폴백 가시성 + Cookie attribute + servlet6/61 신규 메서드 — 11 페어 = 22 task + 통합 review  
**Depends on**: PH-003  
**Task count**: 23

#### §3.PH-004.T-PH004-01 FR-CONV-003 AdapterUnwrap marker 인터페이스 (common 모듈 신설) — TDD red (failing test)

- id: T-PH004-01
- phase_id: PH-004
- title: FR-CONV-003 AdapterUnwrap marker 인터페이스 (common 모듈 신설) — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-003]
- files: [common/src/test/java/adapter/common/AdapterUnwrapTest.java]
- action: common/src/test/java/adapter/common/AdapterUnwrapTest.java 신설. AdapterUnwrap<T> 인터페이스 존재 + unwrap() 반환 verify. AC-1~AC-4 — interface_exists / unwrap_method_present / generic_T_correct / common_module_compile.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldDefineAdapterUnwrapInterface* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldDefineAdapterUnwrapInterface*", windows: ".\gradlew.bat test --tests *shouldDefineAdapterUnwrapInterface*"}
- dod:
  - red 단계: 4개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: common/src/test/java/adapter/common/AdapterUnwrapTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 4}
- evaluation_topology: full

#### §3.PH-004.T-PH004-02 FR-CONV-003 AdapterUnwrap marker 인터페이스 (common 모듈 신설) — TDD green (impl)

- id: T-PH004-02
- phase_id: PH-004
- title: FR-CONV-003 AdapterUnwrap marker 인터페이스 (common 모듈 신설) — TDD green (impl)
- type: code
- req_ids: [FR-CONV-003]
- files: [common/src/main/java/adapter/common/AdapterUnwrap.java]
- action: common/src/main/java/adapter/common/AdapterUnwrap.java 신설. public interface AdapterUnwrap<T> { T unwrap(); }. 후속 페어가 모든 어댑터 클래스가 implements.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldDefineAdapterUnwrapInterface* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldDefineAdapterUnwrapInterface*", windows: ".\gradlew.bat test --tests *shouldDefineAdapterUnwrapInterface*"}
- dod:
  - green 단계: 4개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-01]
- tdd: {applicable: true, phase: "green", test_cases_count: 4}
- evaluation_topology: full

#### §3.PH-004.T-PH004-03 FR-CONV-003 + FR-CONV-004 모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피 — TDD red (failing test)

- id: T-PH004-03
- phase_id: PH-004
- title: FR-CONV-003 + FR-CONV-004 모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피 — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-003,FR-CONV-004]
- files: [servlet5/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java,servlet6/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java,servlet61/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java]
- action: AdapterUnwrapAllClassesTest.java 신설 (3 모듈). 모든 어댑터 클래스가 implements AdapterUnwrap<원본> + unwrap() 반환 정확 verify. equals/hashCode 가 SPEC-6 패턴 (unwrap 위임 + AdapterUnwrap chain) verify. ServletAdapter.adaptToX 메서드의 instanceof + 이중 wrap 회피 분기 verify. 각 REQ 의 AC 모두 cover.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance*", windows: ".\gradlew.bat test --tests *shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance*"}
- dod:
  - red 단계: 7개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/AdapterUnwrapAllClassesTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 7}
- evaluation_topology: full

#### §3.PH-004.T-PH004-04 FR-CONV-003 + FR-CONV-004 모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피 — TDD green (impl)

- id: T-PH004-04
- phase_id: PH-004
- title: FR-CONV-003 + FR-CONV-004 모든 어댑터 implements AdapterUnwrap + equals/hashCode + ServletAdapter 사전 unwrap 체크 + 이중 wrap 회피 — TDD green (impl)
- type: code
- req_ids: [FR-CONV-003,FR-CONV-004]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/,servlet5/src/main/java/adapter/javax/servlet5/,servlet5/src/main/java/com/snoworca/ServletAdapter.java,servlet6/src/main/java/adapter/jakarta/servlet6/,servlet6/src/main/java/adapter/javax/servlet6/,servlet6/src/main/java/com/snoworca/ServletAdapter.java,servlet61/src/main/java/adapter/jakarta/servlet61/,servlet61/src/main/java/adapter/javax/servlet61/,servlet61/src/main/java/com/snoworca/ServletAdapter.java]
- action: 모든 어댑터 클래스에 implements AdapterUnwrap<원본타입> 추가 + public 원본타입 unwrap() { return this.원본필드; } 메서드 추가. equals / hashCode 도 FR-CONV-004 의 SPEC-6 패턴대로 unwrap 위임 + AdapterUnwrap instanceof 처리 패턴으로 override. ServletAdapter 의 모든 adaptToJakarta / adaptToJavax 메서드에 3단계 분기 (instanceof AdapterUnwrap 사전 + 반환 namespace 일치 시 원본 반환 + 새 어댑터 wrap) 추가.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance*", windows: ".\gradlew.bat test --tests *shouldImplementAdapterUnwrapWithEqualsHashCodeAndDoubleWrapAvoidance*"}
- dod:
  - green 단계: 7개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-03]
- tdd: {applicable: true, phase: "green", test_cases_count: 7}
- evaluation_topology: full

#### §3.PH-004.T-PH004-05 FR-CONV-005 ServletException 양방향 변환 (try/catch + cause chain) — TDD red (failing test)

- id: T-PH004-05
- phase_id: PH-004
- title: FR-CONV-005 ServletException 양방향 변환 (try/catch + cause chain) — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-005]
- files: [servlet5/src/test/java/com/snoworca/ServletExceptionConversionTest.java]
- action: ServletExceptionConversionTest.java 신설. throws ServletException 시그니처를 갖는 위임 메서드에서 양방향 변환 + cause chain verify. AC-1~AC-3 — bidirectional / cause_preserved / non_servlet_exception_passthrough.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldConvertServletExceptionBidirectionalWithCauseChain* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldConvertServletExceptionBidirectionalWithCauseChain*", windows: ".\gradlew.bat test --tests *shouldConvertServletExceptionBidirectionalWithCauseChain*"}
- dod:
  - red 단계: 3개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/ServletExceptionConversionTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 3}
- evaluation_topology: full

#### §3.PH-004.T-PH004-06 FR-CONV-005 ServletException 양방향 변환 (try/catch + cause chain) — TDD green (impl)

- id: T-PH004-06
- phase_id: PH-004
- title: FR-CONV-005 ServletException 양방향 변환 (try/catch + cause chain) — TDD green (impl)
- type: code
- req_ids: [FR-CONV-005]
- files: [common/src/main/java/adapter/common/ConverterSupport.java,servlet5/src/main/java/adapter/jakarta/servlet5/,servlet5/src/main/java/adapter/javax/servlet5/,servlet6/src/main/java/adapter/jakarta/servlet6/,servlet6/src/main/java/adapter/javax/servlet6/,servlet61/src/main/java/adapter/jakarta/servlet61/,servlet61/src/main/java/adapter/javax/servlet61/]
- action: common/ConverterSupport.java 에 javaxToJakartaServletException + jakartaToJavaxServletException 헬퍼 추가 (new 인스턴스 + initCause(원본)). 위임 메서드 중 throws ServletException 시그니처에 try/catch 변환 패턴 적용. IOException / IllegalStateException 등 표준 예외는 그대로 propagate. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldConvertServletExceptionBidirectionalWithCauseChain* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldConvertServletExceptionBidirectionalWithCauseChain*", windows: ".\gradlew.bat test --tests *shouldConvertServletExceptionBidirectionalWithCauseChain*"}
- dod:
  - green 단계: 3개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-05]
- tdd: {applicable: true, phase: "green", test_cases_count: 3}
- evaluation_topology: full

#### §3.PH-004.T-PH004-07 FR-CONV-006 + FR-CONV-007 + FR-CONV-008 + FR-CONV-009 컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16 — TDD red (failing test)

- id: T-PH004-07
- phase_id: PH-004
- title: FR-CONV-006 + FR-CONV-007 + FR-CONV-008 + FR-CONV-009 컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16 — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-006,FR-CONV-007,FR-CONV-008,FR-CONV-009]
- files: [servlet5/src/test/java/com/snoworca/EagerSnapshotTest.java,servlet5/src/test/java/com/snoworca/SideEffectTest.java,servlet5/src/test/java/com/snoworca/StreamSpecTest.java]
- action: EagerSnapshotTest.java + StreamSpecTest.java + SideEffectTest.java 신설 (3 모듈). FR-CONV-006 AC 3개 (eager snapshot / null pass / array copy), FR-CONV-007 AC 9개 (read 3 + readLine + isFinished + isReady + setReadListener + write 3 + setWriteListener), FR-CONV-008 AC 2개 (long delegate + no narrow), FR-CONV-009 AC 7개 (flushBuffer/reset/resetBuffer/complete/invalidate/sendError/sendRedirect). 모든 AC 의 test case 매트릭스.
- covers_ac: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7, AC-8, AC-9]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths*", windows: ".\gradlew.bat test --tests *shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths*"}
- dod:
  - red 단계: 21개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/EagerSnapshotTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 21}
- evaluation_topology: full

#### §3.PH-004.T-PH004-08 FR-CONV-006 + FR-CONV-007 + FR-CONV-008 + FR-CONV-009 컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16 — TDD green (impl)

- id: T-PH004-08
- phase_id: PH-004
- title: FR-CONV-006 + FR-CONV-007 + FR-CONV-008 + FR-CONV-009 컬렉션 eager snapshot + Stream SPEC-12 + setContentLengthLong SPEC-13 + 부수효과 보존 SPEC-16 — TDD green (impl)
- type: code
- req_ids: [FR-CONV-006,FR-CONV-007,FR-CONV-008,FR-CONV-009]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/,servlet5/src/main/java/adapter/javax/servlet5/,servlet6/src/main/java/,servlet61/src/main/java/]
- action: PH-003 의 FIX 가 완료된 코드 위에서 추가 SPEC 검증 단위 테스트로 잠금. (1) FR-CONV-006: getCookies/getParts/getServletRegistrations/getFilterRegistrations 가 eager snapshot 반환 + 원본 null 시 null 반환. (2) FR-CONV-007: ServletInputStream/OutputStream 어댑터의 모든 시그니처 원본 동명 위임. (3) FR-CONV-008: setContentLengthLong 원본 동명 위임 (FR-FIX-003 의 잠금). (4) FR-CONV-009: flushBuffer/reset/resetBuffer/complete/invalidate/sendError/sendRedirect 즉시 위임. 3 모듈 모두.
- covers_ac: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7, AC-8, AC-9]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths*", windows: ".\gradlew.bat test --tests *shouldVerifyEagerSnapshotStreamSpecAndSideEffectsAllPaths*"}
- dod:
  - green 단계: 21개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-07]
- tdd: {applicable: true, phase: "green", test_cases_count: 21}
- evaluation_topology: full

#### §3.PH-004.T-PH004-09 FR-CONV-011 strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위 — TDD red (failing test)

- id: T-PH004-09
- phase_id: PH-004
- title: FR-CONV-011 strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위 — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-011]
- files: [servlet5/src/test/java/com/snoworca/StrictModeTest.java]
- action: StrictModeTest.java 신설. AC-1 (시스템 프로퍼티 ON → UnsupportedOperationException) + AC-2 (init-param ON → 동일) + AC-3 (OFF default → lenient 폴백) + AC-4 (getRequestId/getServletConnection 은 strict 시에도 default 유지) 4건 test.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldEnforceStrictModeWithOptInAndDefaultLenient* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldEnforceStrictModeWithOptInAndDefaultLenient*", windows: ".\gradlew.bat test --tests *shouldEnforceStrictModeWithOptInAndDefaultLenient*"}
- dod:
  - red 단계: 4개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/StrictModeTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 4}
- evaluation_topology: full

#### §3.PH-004.T-PH004-10 FR-CONV-011 strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위 — TDD green (impl)

- id: T-PH004-10
- phase_id: PH-004
- title: FR-CONV-011 strict 모드 (lenient default + opt-in) + 시스템 프로퍼티 + init-param 우선순위 — TDD green (impl)
- type: code
- req_ids: [FR-CONV-011]
- files: [common/src/main/java/adapter/common/ConverterSupport.java,servlet5/src/main/java/com/snoworca/ServletAdapter.java,servlet6/src/main/java/com/snoworca/ServletAdapter.java,servlet61/src/main/java/com/snoworca/ServletAdapter.java]
- action: ConverterSupport 에 strict 모드 상태 관리 (static volatile boolean strictMode) + ServletContext init-param 우선순위 (시스템 프로퍼티 우선, init-param fallback) 로직. throwIfStrict(message) 헬퍼 — strict 활성 시 UnsupportedOperationException throw. 모든 어댑터의 폴백 분기 (B/D/E/F) 가 본 헬퍼 호출. getRequestId / getServletConnection 은 strict 정책이 F 동일이므로 예외 없이 default 동작 유지. getProtocolRequestId 도 strict D 동일이므로 빈 문자열 유지.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldEnforceStrictModeWithOptInAndDefaultLenient* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldEnforceStrictModeWithOptInAndDefaultLenient*", windows: ".\gradlew.bat test --tests *shouldEnforceStrictModeWithOptInAndDefaultLenient*"}
- dod:
  - green 단계: 4개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-09]
- tdd: {applicable: true, phase: "green", test_cases_count: 4}
- evaluation_topology: full

#### §3.PH-004.T-PH004-11 FR-CONV-012 폴백 가시성 (java.util.logging dedup + diagnostics API) — TDD red (failing test)

- id: T-PH004-11
- phase_id: PH-004
- title: FR-CONV-012 폴백 가시성 (java.util.logging dedup + diagnostics API) — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-012]
- files: [servlet5/src/test/java/com/snoworca/DiagnosticsTest.java,servlet5/src/test/java/com/snoworca/LoggingDedupTest.java]
- action: DiagnosticsTest.java + LoggingDedupTest.java 신설. AC-1 (once-per-method dedup) + AC-2 (diagnostics() 시스템 프로퍼티 opt-in) + AC-3 (incrementDiagnostics thread-safe) 3건 test.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldProvideFallbackVisibilityWithLoggingAndDiagnostics* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldProvideFallbackVisibilityWithLoggingAndDiagnostics*", windows: ".\gradlew.bat test --tests *shouldProvideFallbackVisibilityWithLoggingAndDiagnostics*"}
- dod:
  - red 단계: 3개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/DiagnosticsTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 3}
- evaluation_topology: full

#### §3.PH-004.T-PH004-12 FR-CONV-012 폴백 가시성 (java.util.logging dedup + diagnostics API) — TDD green (impl)

- id: T-PH004-12
- phase_id: PH-004
- title: FR-CONV-012 폴백 가시성 (java.util.logging dedup + diagnostics API) — TDD green (impl)
- type: code
- req_ids: [FR-CONV-012]
- files: [common/src/main/java/adapter/common/ConverterSupport.java,servlet5/src/main/java/com/snoworca/ServletAdapter.java,servlet6/src/main/java/com/snoworca/ServletAdapter.java,servlet61/src/main/java/com/snoworca/ServletAdapter.java]
- action: ConverterSupport 에 (1) Logger.getLogger('com.snoworca.servletadapter') static, (2) ConcurrentHashMap<String, Boolean> dedup map, (3) logFallbackOnce(methodKey, level, message) 메서드, (4) ConcurrentHashMap<String, AtomicLong> diagnostics counter, (5) incrementDiagnostics(methodKey) 추가. 정책 B/D/F → WARN, 정책 E → INFO. ServletAdapter.diagnostics() 정적 메서드 (시스템 프로퍼티 com.snoworca.adapter.diagnostics=true 활성 시 Map 반환).
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldProvideFallbackVisibilityWithLoggingAndDiagnostics* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldProvideFallbackVisibilityWithLoggingAndDiagnostics*", windows: ".\gradlew.bat test --tests *shouldProvideFallbackVisibilityWithLoggingAndDiagnostics*"}
- dod:
  - green 단계: 3개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-11]
- tdd: {applicable: true, phase: "green", test_cases_count: 3}
- evaluation_topology: full

#### §3.PH-004.T-PH004-13 FR-CONV-010 + FR-CONV-014 FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위 — TDD red (failing test)

- id: T-PH004-13
- phase_id: PH-004
- title: FR-CONV-010 + FR-CONV-014 FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위 — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-010,FR-CONV-014]
- files: [servlet6/src/test/java/com/snoworca/FallbackPolicyTest.java,servlet61/src/test/java/com/snoworca/FallbackPolicyTest.java]
- action: FallbackPolicyTest.java 신설 (servlet6, servlet61). FR-CONV-010 AC 3개 (정책 B+D+E 정확 분기) + FR-CONV-014 AC 3개 (servlet5 dormant + servlet6 B 진입 + servlet61 sendRedirect 명시) 6건 test 매트릭스.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant*", windows: ".\gradlew.bat test --tests *shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant*"}
- dod:
  - red 단계: 6개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet6/src/test/java/com/snoworca/FallbackPolicyTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 6}
- evaluation_topology: full

#### §3.PH-004.T-PH004-14 FR-CONV-010 + FR-CONV-014 FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위 — TDD green (impl)

- id: T-PH004-14
- phase_id: PH-004
- title: FR-CONV-010 + FR-CONV-014 FR-CONV-010 폴백 정책 매트릭스 servlet6/61 적용 + FR-CONV-014 모듈별 적용 범위 — TDD green (impl)
- type: code
- req_ids: [FR-CONV-010,FR-CONV-014]
- files: [servlet6/src/main/java/adapter/jakarta/servlet6/ServletContext.java,servlet6/src/main/java/adapter/jakarta/servlet6/http/EmptyHttpSessionContext.java,servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletRequest.java,servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpServletResponse.java,servlet6/src/main/java/adapter/jakarta/servlet6/http/HttpSession.java,servlet61/src/main/java/adapter/jakarta/servlet61/ServletContext.java,servlet61/src/main/java/adapter/jakarta/servlet61/http/EmptyHttpSessionContext.java,servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletRequest.java,servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletResponse.java,servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpSession.java]
- action: SRS §6.2.2 표대로 정책 적용 — HttpSession.getValue/putValue jakarta→javax = B, HttpSession.getSessionContext = D (EmptyHttpSessionContext 더미), encodeUrl/encodeRedirectUrl = E, setStatus(int,String) = E1, isRequestedSessionIdFromUrl = E, ServletContext.log(Exception,String) = E. servlet6/servlet61 의 EmptyHttpSessionContext 신설. servlet5 는 5.0 비대칭 거의 없어 dormant.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant*", windows: ".\gradlew.bat test --tests *shouldApplyFallbackMatrixForServlet6And61AndKeepServlet5Dormant*"}
- dod:
  - green 단계: 6개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-13]
- tdd: {applicable: true, phase: "green", test_cases_count: 6}
- evaluation_topology: full

#### §3.PH-004.T-PH004-15 FR-CONV-013 Cookie attribute API 정책 D (no-op + WARN once) — TDD red (failing test)

- id: T-PH004-15
- phase_id: PH-004
- title: FR-CONV-013 Cookie attribute API 정책 D (no-op + WARN once) — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-013]
- files: [servlet5/src/test/java/com/snoworca/CookieAttributePolicyTest.java]
- action: CookieAttributePolicyTest.java 신설. AC-1 (setAttribute no-op + WARN) + AC-2 (getAttribute null) + AC-3 (getAttributes empty unmodifiable Map) + AC-4 (no internal Map storage) 4건 test.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldApplyNoOpWithWarnOnceForCookieAttributeApi* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldApplyNoOpWithWarnOnceForCookieAttributeApi*", windows: ".\gradlew.bat test --tests *shouldApplyNoOpWithWarnOnceForCookieAttributeApi*"}
- dod:
  - red 단계: 4개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/CookieAttributePolicyTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 4}
- evaluation_topology: full

#### §3.PH-004.T-PH004-16 FR-CONV-013 Cookie attribute API 정책 D (no-op + WARN once) — TDD green (impl)

- id: T-PH004-16
- phase_id: PH-004
- title: FR-CONV-013 Cookie attribute API 정책 D (no-op + WARN once) — TDD green (impl)
- type: code
- req_ids: [FR-CONV-013]
- files: [servlet5/src/main/java/adapter/javax/servlet5/http/Cookie.java,servlet6/src/main/java/adapter/javax/servlet6/http/Cookie.java,servlet61/src/main/java/adapter/javax/servlet61/http/Cookie.java]
- action: javax Cookie 어댑터 (jakarta 6.0+ 신규 attribute API 가 노출되는 케이스) 에 (1) setAttribute(String, String) → no-op + ConverterSupport.logFallbackOnce(WARN) + incrementDiagnostics, (2) getAttribute(String) → null, (3) getAttributes() → Collections.emptyMap(). 자체 attribute Map 미보관. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3, AC-4]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldApplyNoOpWithWarnOnceForCookieAttributeApi* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldApplyNoOpWithWarnOnceForCookieAttributeApi*", windows: ".\gradlew.bat test --tests *shouldApplyNoOpWithWarnOnceForCookieAttributeApi*"}
- dod:
  - green 단계: 4개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-15]
- tdd: {applicable: true, phase: "green", test_cases_count: 4}
- evaluation_topology: full

#### §3.PH-004.T-PH004-17 FR-CONV-001 jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현 — TDD red (failing test)

- id: T-PH004-17
- phase_id: PH-004
- title: FR-CONV-001 jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현 — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-001]
- files: [servlet6/src/test/java/com/snoworca/RequestIdTest.java,servlet61/src/test/java/com/snoworca/RequestIdTest.java]
- action: RequestIdTest.java 신설 (servlet6 + servlet61). AC-1 (getRequestId lazy UUID) + AC-2 (getProtocolRequestId 빈 문자열 + getServletConnection DummyServletConnection 합성) 2건 test.
- covers_ac: [AC-1, AC-2]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldImplementJakarta6NewAbstractMethods* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldImplementJakarta6NewAbstractMethods*", windows: ".\gradlew.bat test --tests *shouldImplementJakarta6NewAbstractMethods*"}
- dod:
  - red 단계: 2개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet6/src/test/java/com/snoworca/RequestIdTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 2}
- evaluation_topology: full

#### §3.PH-004.T-PH004-18 FR-CONV-001 jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현 — TDD green (impl)

- id: T-PH004-18
- phase_id: PH-004
- title: FR-CONV-001 jakarta 6.0 신규 추상 메서드 (getRequestId + getProtocolRequestId + getServletConnection) 구현 — TDD green (impl)
- type: code
- req_ids: [FR-CONV-001]
- files: [servlet6/src/main/java/adapter/jakarta/servlet6/DummyServletConnection.java,servlet6/src/main/java/adapter/jakarta/servlet6/ServletRequest.java,servlet61/src/main/java/adapter/jakarta/servlet61/DummyServletConnection.java,servlet61/src/main/java/adapter/jakarta/servlet61/ServletRequest.java]
- action: servlet6 / servlet61 의 adapter.jakarta.servletN.ServletRequest 에 (1) getRequestId() → request attribute (key=com.snoworca.adapter.requestId) lazy UUID 캐싱, (2) getProtocolRequestId() → '' 반환, (3) getServletConnection() → DummyServletConnection 인스턴스 합성 반환. DummyServletConnection.java 신설. 본 폴백은 strict 모드에서도 default 유지.
- covers_ac: [AC-1, AC-2]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldImplementJakarta6NewAbstractMethods* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldImplementJakarta6NewAbstractMethods*", windows: ".\gradlew.bat test --tests *shouldImplementJakarta6NewAbstractMethods*"}
- dod:
  - green 단계: 2개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-17]
- tdd: {applicable: true, phase: "green", test_cases_count: 2}
- evaluation_topology: full

#### §3.PH-004.T-PH004-19 FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override — TDD red (failing test)

- id: T-PH004-19
- phase_id: PH-004
- title: FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override — TDD red (failing test)
- type: code
- req_ids: [FR-CONV-001,FR-CONV-014]
- files: [servlet61/src/test/java/com/snoworca/SendRedirect61Test.java]
- action: SendRedirect61Test.java 신설 (servlet61). FR-CONV-001 AC 2개 (override 존재 + AbstractMethodError 없음) + FR-CONV-014 AC 3개 (servlet5 dormant + servlet6 B + servlet61 명시) 5건 test.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61*", windows: ".\gradlew.bat test --tests *shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61*"}
- dod:
  - red 단계: 5개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet61/src/test/java/com/snoworca/SendRedirect61Test.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 5}
- evaluation_topology: full

#### §3.PH-004.T-PH004-20 FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override — TDD green (impl)

- id: T-PH004-20
- phase_id: PH-004
- title: FR-CONV-001 + FR-CONV-014 jakarta 6.1 sendRedirect(String, int, boolean) 명시 override — TDD green (impl)
- type: code
- req_ids: [FR-CONV-001,FR-CONV-014]
- files: [servlet61/src/main/java/adapter/jakarta/servlet61/http/HttpServletResponse.java]
- action: servlet61 의 adapter.jakarta.servlet61.http.HttpServletResponse 에 sendRedirect(String location, int sc, boolean clearBuffer) 메서드 명시 override 추가. 구현 — setStatus(sc) + setHeader('Location', location) + clearBuffer 시 resetBuffer + flushBuffer. javax 4.0 측에 본 시그니처가 없으므로 명시 합성. AbstractMethodError 회피.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61*", windows: ".\gradlew.bat test --tests *shouldOverrideSendRedirectWithLocationStatusClearBufferOnServlet61*"}
- dod:
  - green 단계: 5개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-19]
- tdd: {applicable: true, phase: "green", test_cases_count: 5}
- evaluation_topology: full

#### §3.PH-004.T-PH004-21 NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10) — TDD red (failing test)

- id: T-PH004-21
- phase_id: PH-004
- title: NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10) — TDD red (failing test)
- type: code
- req_ids: [NFR-CONV-001]
- files: [servlet5/src/test/java/com/snoworca/ThreadSafetyTest.java]
- action: ThreadSafetyTest.java 신설. AC-1 (어댑터 final/thread-safe 필드) + AC-2 (dedup/diagnostics ConcurrentHashMap+AtomicLong) + AC-3 (BM10 ThreadLocal single-thread 가정 회귀 잠금) 3건 test.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn* --quiet", expected_exit=1, stdout_regex="FAILED"}
- verification_cmd: {posix: "./gradlew test --tests *shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn*", windows: ".\gradlew.bat test --tests *shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn*"}
- dod:
  - red 단계: 3개 test case 작성됨 (각 REQ AC 별 1개씩)
  - red 단계: 작성된 test 가 모두 실패(red) 함을 확인
- rollback: servlet5/src/test/java/com/snoworca/ThreadSafetyTest.java 등 신설 테스트 파일 삭제.
- estimated_effort: M
- tdd: {applicable: true, phase: "red", test_cases_count: 3}
- evaluation_topology: full

#### §3.PH-004.T-PH004-22 NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10) — TDD green (impl)

- id: T-PH004-22
- phase_id: PH-004
- title: NFR-CONV-001 스레드 안전성 + HttpUpgradeHandler ThreadLocal 잠금 (BM10) — TDD green (impl)
- type: code
- req_ids: [NFR-CONV-001]
- files: [servlet5/src/main/java/adapter/jakarta/servlet5/http/HttpUpgradeHandler.java,servlet5/src/test/java/com/snoworca/regression/BugMuseumTest.java]
- action: 어댑터 클래스의 인스턴스 필드를 모두 final 또는 thread-safe 타입으로 확인. diagnostics counter / dedup map 이 ConcurrentHashMap + AtomicLong 인지 PR review. BM10 회귀 테스트 보강 — HttpUpgradeHandler ThreadLocal pattern 의 현 동작이 통과 + 향후 비동기 호출 시 즉시 실패하도록 작성. 3 모듈 모두 적용.
- covers_ac: [AC-1, AC-2, AC-3]
- acceptance_tests: {shell: cmd="./gradlew test --tests *shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn*", windows: ".\gradlew.bat test --tests *shouldEnforceFinalFieldsAndConcurrentMapsAndBM10LockIn*"}
- dod:
  - green 단계: 3개 test case 가 모두 통과(green)
  - green 단계: 동일 test 가 모두 통과(green) 함을 확인
- rollback: git diff 로 구현 reverse — red task 로 복귀.
- estimated_effort: M
- depends_on_task: [T-PH004-21]
- tdd: {applicable: true, phase: "green", test_cases_count: 3}
- evaluation_topology: full

#### §3.PH-004.T-PH004-23 PH-004 폴백 정책 / strict 모드 / AdapterUnwrap / 로깅 / diagnostics 통합 회귀 + 단위 테스트 매트릭스 검증

- id: T-PH004-23
- phase_id: PH-004
- title: PH-004 폴백 정책 / strict 모드 / AdapterUnwrap / 로깅 / diagnostics 통합 회귀 + 단위 테스트 매트릭스 검증
- type: review
- req_ids: [FR-CONV-001,FR-CONV-002,FR-CONV-003,FR-CONV-004,FR-CONV-005,FR-CONV-010,FR-CONV-011,FR-CONV-012,FR-CONV-013,FR-CONV-014,NFR-CONV-001]
- files: [servlet5/src/test/java/,servlet6/src/test/java/,servlet61/src/test/java/]
- action: PH-004 의 11개 페어 (22 task) 결과를 단위 테스트 매트릭스로 종합 검증. SRS §6.2.2 표의 모든 비대칭 메서드 그룹별로 default + strict 양쪽 동작 검증. FR-CONV-001 의 6.0/6.1 신규 추상 메서드 구현 완료 확인. FR-CONV-002 (위임 우선) 는 PH-005 의 FR-TEST-002 Reflection Contract Test 가 검증. 미달 시 해당 PH-004 페어의 green task 재진입.
- acceptance_tests: {checklist: 4 items}
- verification_cmd: {posix: "./gradlew test", windows: ".\gradlew.bat test"}
- dod:
  - PH-004 의 22 페어 task 결과가 단위 테스트 매트릭스로 통합 검증
  - SRS §6.2.2 표의 모든 비대칭 메서드 그룹의 default + strict 동작 검증
  - 3개 모듈 모두 ./gradlew test 통과
- rollback: 본 review 가 fail 한 페어의 green task 로 재진입.
- estimated_effort: M
- tdd: {applicable: false, phase: "n/a", exempt_reason: "auto-exempt by task type"}
- evaluation_topology: file_op

### §3.PH-005 테스트 인프라

**Goal**: 단위 테스트 (Mockito) + Reflection Contract Test + Tomcat embed IT01~IT15 + 크로스버전 + JaCoCo 게이트  
**Depends on**: PH-004  
**Task count**: 4

#### §3.PH-005.T-PH005-01 어댑터 클래스당 단위 테스트 클래스 (Mockito RETURNS_SMART_NULLS) + 모든 위임 메서드 verify + 폴백 정책 분기 검증

- id: T-PH005-01
- phase_id: PH-005
- title: 어댑터 클래스당 단위 테스트 클래스 (Mockito RETURNS_SMART_NULLS) + 모든 위임 메서드 verify + 폴백 정책 분기 검증
- type: code
- req_ids: [FR-TEST-001]
- files: [servlet5/src/test/java/adapter/jakarta/servlet5/,servlet5/src/test/java/adapter/javax/servlet5/,servlet5/src/test/java/com/snoworca/,servlet6/src/test/java/adapter/jakarta/servlet6/,servlet6/src/test/java/adapter/javax/servlet6/,servlet61/src/test/java/adapter/jakarta/servlet61/,servlet61/src/test/java/adapter/javax/servlet61/]
- action: 현 src/test/java/com/snoworca/ 의 2개 테스트 클래스를 servlet5 로 이동 후 전면 재작성. 각 어댑터 클래스당 1개 이상 단위 테스트 클래스 신설. mock 은 withSettings().defaultAnswer(RETURNS_SMART_NULLS) 로 생성. 모든 위임 메서드를 verify(origin).동명메서드(...) 로 검증. ArgumentCaptor 로 인자 변환 검증. 폴백 정책 (FR-CONV-010) 의 모든 정책 코드별 분기를 default + strict 양쪽으로 검증. servlet6 / servlet61 모듈도 동일 적용.
- acceptance_tests: {shell: cmd="./gradlew test --quiet && ./gradlew :servlet5:test :servlet6:test :servlet61:test --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --info", windows: ".\gradlew.bat test --info"}
- dod:
  - 3개 모듈 각각에 어댑터 클래스당 1개 이상의 단위 테스트 클래스 존재
  - 모든 위임 메서드가 verify 로 검증
  - 폴백 정책 (B/D/E/F) 의 default + strict 분기 모두 검증
  - mock 이 RETURNS_SMART_NULLS 로 생성됨 (stub 누락 시 즉시 검출)
- rollback: 신규 테스트 클래스 삭제.
- estimated_effort: L
- tdd: {applicable: false, phase: "n/a", exempt_reason: "Integration test harness/fixture authoring; the test fixtures themselves constitute the TDD acceptance for FR-FIX/FR-CONV implementation phases. Meta-TDD on test infrastructure is impractical and not "}
- evaluation_topology: file_op

#### §3.PH-005.T-PH005-02 common/src/testFixtures 에 DelegationContractTest driver 작성 + 각 버전 모듈에서 호출

- id: T-PH005-02
- phase_id: PH-005
- title: common/src/testFixtures 에 DelegationContractTest driver 작성 + 각 버전 모듈에서 호출
- type: code
- req_ids: [FR-TEST-002]
- files: [common/build.gradle,common/src/testFixtures/java/com/snoworca/contract/DelegationContractTest.java,servlet5/src/test/java/com/snoworca/contract/Servlet5ContractTest.java,servlet6/src/test/java/com/snoworca/contract/Servlet6ContractTest.java,servlet61/src/test/java/com/snoworca/contract/Servlet61ContractTest.java]
- action: common/build.gradle 에 apply plugin: 'java-test-fixtures' 추가. common/src/testFixtures/java/com/snoworca/contract/DelegationContractTest.java 신설 — (1) 어댑터 spec 등록 API, (2) 인터페이스 메서드 enumerate, (3) Object 메서드 + 변환 정책 메서드 blacklist, (4) sample args 합성, (5) 어댑터 호출 후 origin mock verify, (6) verifyNoMoreInteractions. 각 버전 모듈의 ServletNContractTest 가 driver 를 호출하여 자기 어댑터 spec 등록.
- acceptance_tests: {shell: cmd="./gradlew :servlet5:test --tests *Servlet5ContractTest* :servlet6:test --tests *Servlet6ContractTest* :servlet61:test --tests *Servlet61ContractTest* --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew test --tests *ContractTest*", windows: ".\gradlew.bat test --tests *ContractTest*"}
- dod:
  - DelegationContractTest driver 가 common testFixtures 에 존재
  - 3개 버전 모듈 각각이 driver 를 호출하는 ServletNContractTest 보유
  - 모든 어댑터 인터페이스의 모든 추상 메서드에 대해 위임 검증 자동 수행
- rollback: testFixtures 디렉토리 삭제 + 각 모듈의 ServletNContractTest 삭제.
- estimated_effort: L
- tdd: {applicable: false, phase: "n/a", exempt_reason: "Integration test harness/fixture authoring; the test fixtures themselves constitute the TDD acceptance for FR-FIX/FR-CONV implementation phases. Meta-TDD on test infrastructure is impractical and not "}
- evaluation_topology: file_op

#### §3.PH-005.T-PH005-06 각 모듈에 JaCoCo 플러그인 + 커버리지 게이트 (라인 95% / 분기 100% for converter, 90% for adapter)

- id: T-PH005-06
- phase_id: PH-005
- title: 각 모듈에 JaCoCo 플러그인 + 커버리지 게이트 (라인 95% / 분기 100% for converter, 90% for adapter)
- type: infra
- req_ids: [NFR-TEST-001]
- files: [common/build.gradle,gradle.properties,servlet5/build.gradle,servlet6/build.gradle,servlet61/build.gradle]
- action: 각 모듈 build.gradle 에 apply plugin: 'jacoco' + jacocoTestReport + jacocoTestCoverageVerification 블록 추가. violationRules — adapter.{jakarta,javax}.** 위임 코드 라인 ≥95%/분기 ≥90%, converter + common 라인 ≥95%/분기 ≥100%, 예외 변환 try/catch 라인 100%. ./gradlew check 가 본 task 자동 실행.
- acceptance_tests: {shell: cmd="./gradlew :servlet5:jacocoTestCoverageVerification :servlet6:jacocoTestCoverageVerification :servlet61:jacocoTestCoverageVerification :common:jacocoTestCoverageVerification --quiet", expected_exit=0}
- verification_cmd: {posix: "./gradlew jacocoTestCoverageVerification", windows: ".\gradlew.bat jacocoTestCoverageVerification"}
- dod:
  - 4개 모듈에 jacoco 플러그인 + violationRules 보유
  - ./gradlew check 시 커버리지 게이트 실행
  - PR 미달 시 빌드 실패
- rollback: 각 build.gradle 의 jacoco 블록 제거.
- estimated_effort: M
- tdd: {applicable: false, phase: "n/a", exempt_reason: "CI matrix configuration; correctness validated by GHA workflow successful run, not unit tests."}
- evaluation_topology: infra

#### §3.PH-005.T-PH005-07 PH-005 의 6개 task 결과 통합 검토 — 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과

- id: T-PH005-07
- phase_id: PH-005
- title: PH-005 의 6개 task 결과 통합 검토 — 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과
- type: review
- req_ids: [FR-TEST-001,FR-TEST-002,NFR-TEST-001]
- files: []
- action: PH-005 의 6개 task 완료 후 ./gradlew check + ./gradlew integrationTest 통합 실행. 단위 + contract + integration + cross-version + jacoco 게이트 모두 통과 확인. 미달 시 해당 task 재진입.
- acceptance_tests: {checklist: 3 items}
- verification_cmd: {posix: "./gradlew check integrationTest", windows: ".\gradlew.bat check integrationTest"}
- dod:
  - ./gradlew check integrationTest 가 5개 모듈에서 모두 통과
  - PH-005 의 6개 task 결과가 통합 검증
- rollback: 본 검토 fail 시 해당 task 재진입.
- estimated_effort: S
- tdd: {applicable: false, phase: "n/a", exempt_reason: "auto-exempt by task type"}
- evaluation_topology: file_op

### §3.PH-006 CI 매트릭스 + JaCoCo

**Goal**: GitHub Actions ci.yml (build-matrix + integration-tests + coverage)  
**Depends on**: PH-005  
**Task count**: 1

#### §3.PH-006.T-PH006-03 JaCoCo 보고서 PR 댓글 + Codecov 업로드 + 게이트 95%/100% 적용 검증

- id: T-PH006-03
- phase_id: PH-006
- title: JaCoCo 보고서 PR 댓글 + Codecov 업로드 + 게이트 95%/100% 적용 검증
- type: infra
- req_ids: [NFR-TEST-001]
- files: [.github/workflows/ci.yml]
- action: T-PH006-01 의 coverage job 이 JaCoCo XML 보고서를 생성하고 Codecov 또는 GitHub PR 댓글로 업로드함을 검증. 게이트 95%/100% 미달 시 PR 빌드 실패 확인. PR 머지 차단 게이트 활성화 (GitHub branch protection rules).
- acceptance_tests: {file_state: build.gradle exists=true} | {shell: cmd="./gradlew jacocoTestReport", expected_exit=0}
- verification_cmd: null
- dod:
  - 각 PR 의 coverage job 이 모듈별 JaCoCo XML 생성
  - Codecov 또는 PR 댓글에 coverage 표시
  - 의도적 게이트 미달 PR 이 빌드 실패
  - GitHub branch protection rules 에 coverage 게이트 등록
- rollback: ci.yml 의 coverage job 또는 branch protection rules 제거.
- estimated_effort: S
- tdd: {applicable: false, phase: "n/a", exempt_reason: "CI matrix configuration; correctness validated by GHA workflow successful run, not unit tests."}
- evaluation_topology: infra

### §3.PH-007 Maven Central 게시

**Goal**: maven-publish + signing + BOM POM + README 호환성 매트릭스 + CHANGELOG + 0.x alias (D8 옵션 B) + GPG/Sonatype + 1.0.0 staging upload  
**Depends on**: PH-006  
**Task count**: 5

#### §3.PH-007.T-PH007-01 각 모듈 build.gradle 에 maven-publish + signing 플러그인 + publication 정의

- id: T-PH007-01
- phase_id: PH-007
- title: 각 모듈 build.gradle 에 maven-publish + signing 플러그인 + publication 정의
- type: code
- req_ids: [FR-MOD-007,IR-REL-001]
- files: [bom/build.gradle,common/build.gradle,servlet5/build.gradle,servlet6/build.gradle,servlet61/build.gradle]
- action: 5개 모듈 build.gradle 에 apply plugin: 'maven-publish' + apply plugin: 'signing'. 각 모듈에 publication 정의 — publishing { publications { mavenJava(MavenPublication) { artifactId 'jakarta-to-javax-servlet-adapter-{name}', from components.java (또는 components.javaPlatform for bom), pom { name/description/url/licenses/developers/scm } } } repositories { maven { name 'OSSRH', url ossrhUrl, credentials } } }. signing 블록 — useGpgCmd 또는 useInMemoryPgpKeys + sign publishing.publications.mavenJava. version 1.0.0 동기.
- acceptance_tests: {shell: cmd="./gradlew publishToMavenLocal --quiet && ls ~/.m2/repository/com/clipsoft/jakarta-to-javax-servlet-adapter-*/1.0.0/ | wc -l", expected_exit=0, stdout_regex="^[5-9]"}
- verification_cmd: {posix: "./gradlew publishToMavenLocal", windows: ".\gradlew.bat publishToMavenLocal"}
- dod:
  - 5개 모듈 모두 maven-publish + signing 플러그인 적용
  - ./gradlew publishToMavenLocal 결과로 5개 jar (POM + GPG signature 포함) 생성
  - 5개 artifactId 가 jakarta-to-javax-servlet-adapter-{common,servlet5,servlet6,servlet61,bom} 형식
  - 모든 모듈 version=1.0.0 동기
- rollback: 각 build.gradle 의 publishing/signing 블록 제거.
- estimated_effort: M
- tdd: {applicable: false, phase: "n/a", exempt_reason: "Release artifact authoring (CHANGELOG/README badges/version bump); validated by file_state checks and Maven Central staging verification, not behavior tests."}
- evaluation_topology: file_op

#### §3.PH-007.T-PH007-02 README.md 전면 개정 — 호환성 매트릭스 + Cookie attribute 손실 + setStatus reason phrase 폐기 + getValue alias + strict 모드 + 0.x 마이그레이션

- id: T-PH007-02
- phase_id: PH-007
- title: README.md 전면 개정 — 호환성 매트릭스 + Cookie attribute 손실 + setStatus reason phrase 폐기 + getValue alias + strict 모드 + 0.x 마이그레이션
- type: doc
- req_ids: [FR-REL-001]
- files: [README.md]
- action: README.md 전면 개정. (1) 호환성 매트릭스 표: -servlet5 (5.0.x, javax 4.0.x, Java 8, Tomcat 10.0.x), -servlet6 (6.0.x, javax 4.0.x, Java 11, Tomcat 10.1.x), -servlet61 (6.1.x, javax 4.0.x, Java 17, Tomcat 11.x). (2) Cookie attribute API 정보 손실 정책. (3) setStatus reason phrase 폐기. (4) HttpSession.getValue/putValue attribute API 자동 폴백. (5) strict 모드 활성화 방법. (6) 0.0.1 사용자 마이그레이션 안내 (D8 옵션 B 결과).
- acceptance_tests: {file_state: undefined exists=undefined}
- verification_cmd: null
- dod:
  - README.md 에 호환성 매트릭스 표 존재
  - Cookie attribute / setStatus reason phrase / getValue alias / strict 모드 / 0.x 마이그레이션 5개 항목 모두 문서화
  - 한국어 또는 영어 일관 (현 README = 영어)
- rollback: README.md 의 신규 섹션 제거.
- estimated_effort: M
- tdd: {applicable: false, phase: "n/a", exempt_reason: "auto-exempt by task type"}
- evaluation_topology: file_op

#### §3.PH-007.T-PH007-03 CHANGELOG.md 작성 — 0.0.1 → 1.0.0 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 추가 + 폴백 정책

- id: T-PH007-03
- phase_id: PH-007
- title: CHANGELOG.md 작성 — 0.0.1 → 1.0.0 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 추가 + 폴백 정책
- type: doc
- req_ids: [FR-REL-001]
- files: [CHANGELOG.md]
- action: CHANGELOG.md 신설 또는 갱신 — ## [1.0.0] - 2026-MM-DD 섹션에 (1) 결함 수정 9건 (FR-FIX-001~009 의 결함 ID + 1줄 요약), (2) 신규 multi-module 구조, (3) 6.0/6.1 동시 지원 추가 (AbstractMethodError 회피), (4) 폴백 정책 변경, (5) 새 진입점 (com.snoworca.ServletAdapter5/6/61 별칭). Keep a Changelog 1.1.0 형식.
- acceptance_tests: {file_state: undefined exists=undefined}
- verification_cmd: null
- dod:
  - CHANGELOG.md 가 신설되거나 1.0.0 섹션 추가
  - 결함 수정 9건 + multi-module 구조 + 6.0/6.1 지원 + 폴백 정책 변경 + 신규 진입점 5개 항목 명시
  - Keep a Changelog 형식
- rollback: CHANGELOG.md 의 1.0.0 섹션 제거.
- estimated_effort: S
- tdd: {applicable: false, phase: "n/a", exempt_reason: "auto-exempt by task type"}
- evaluation_topology: file_op

#### §3.PH-007.T-PH007-04 마이그레이션 가이드 작성 + D8 옵션 B (servlet5 내 deprecated alias 패키지) 구현

- id: T-PH007-04
- phase_id: PH-007
- title: 마이그레이션 가이드 작성 + D8 옵션 B (servlet5 내 deprecated alias 패키지) 구현
- type: code
- req_ids: [MIG-REL-001]
- files: [docs/migration/0.x-to-1.0.md,servlet5/src/main/java/adapter/jakarta/servlet/,servlet5/src/main/java/adapter/javax/servlet/,servlet5/src/main/java/adapter/servletElementConverter/]
- action: D8 의 확정 — 옵션 B (servlet5 모듈 내 @Deprecated alias 패키지) 구현. servlet5 모듈에 adapter.jakarta.servlet.*, adapter.javax.servlet.*, adapter.servletElementConverter.* (0.x 패키지명) deprecated alias 클래스 추가 — 각 alias 가 adapter.jakarta.servlet5.* 등의 실제 클래스를 상속 또는 위임. @Deprecated(forRemoval = true, since = '1.0.0') + javadoc 에 v2.0 제거 예고. docs/migration/0.x-to-1.0.md 신설 — import 변경 안내 + 의존 좌표 변경 + alias 사용 예시 + v2.0 제거 일정.
- acceptance_tests: {shell: cmd="./gradlew :servlet5:compileJava --quiet && find servlet5/src/main/java/adapter/jakarta/servlet -maxdepth 4 -name '*.java' | wc -l", expected_exit=0}
- verification_cmd: {posix: "./gradlew :servlet5:compileJava && cat docs/migration/0.x-to-1.0.md | head -20", windows: ".\gradlew.bat :servlet5:compileJava && type docs\migration\0.x-to-1.0.md"}
- dod:
  - D8 옵션 B 구현 완료 — servlet5 모듈에 0.x 패키지명 @Deprecated alias 클래스
  - 0.0.1 사용자가 import 변경 없이 1.0.0 의존 업그레이드 시 컴파일 success
  - docs/migration/0.x-to-1.0.md 마이그레이션 가이드 작성
  - 각 alias 가 @Deprecated(since='1.0.0', forRemoval=true) + javadoc 'Use adapter.jakarta.servlet5.*' 명시
- rollback: alias 클래스 제거. 마이그레이션 가이드는 보존.
- estimated_effort: L
- tdd: {applicable: false, phase: "n/a", exempt_reason: "Deprecated alias subclasses are pure delegation wrappers to renamed adapter.jakarta.servlet5.* classes; correctness validated by ./gradlew :servlet5:compileJava on legacy 0.x import shape + javadoc in"}
- evaluation_topology: file_op

#### §3.PH-007.T-PH007-07 v1.0.0 git tag + GitHub Release 작성 + 최종 회귀 (BM + IT + cross-version 전수)

- id: T-PH007-07
- phase_id: PH-007
- title: v1.0.0 git tag + GitHub Release 작성 + 최종 회귀 (BM + IT + cross-version 전수)
- type: pr
- req_ids: [FR-REL-001,IR-REL-001,MIG-REL-001]
- files: [CHANGELOG.md,README.md]
- action: 모든 PH-002~PH-006 완료 후 main 브랜치에서 git tag v1.0.0 + push. T-PH007-05 의 release.yml 이 자동 트리거되어 publish. GitHub Releases UI 에서 v1.0.0 release 작성 — CHANGELOG + 호환성 매트릭스 + 5개 artifact 좌표 + 마이그레이션 가이드 링크. 최종 회귀 — ./gradlew test integrationTest jacocoTestCoverageVerification + BM01~BM10 + IT01~IT15 + cross-version 동등성 모두 통과.
- acceptance_tests: {checklist: 0 items}
- verification_cmd: {posix: "git tag --list v1.0.0 && ./gradlew test integrationTest jacocoTestCoverageVerification", windows: "git tag --list v1.0.0 && .\gradlew.bat test integrationTest jacocoTestCoverageVerification"}
- dod:
  - v1.0.0 git tag 가 main 에 생성·푸시됨
  - GitHub Release v1.0.0 작성 + CHANGELOG + 호환성 매트릭스 + 마이그레이션 가이드 링크
  - Maven Central 에 5개 artifact 게시 완료
  - 최종 회귀 (BM01~BM10 + IT01~IT15 + cross-version) 모두 통과
  - PR review + 머지 완료
- rollback: git tag 삭제. 단 Maven Central 1.0.0 은 영구.
- estimated_effort: M
- tdd: {applicable: false, phase: "n/a", exempt_reason: "auto-exempt by task type"}
- evaluation_topology: file_op

## §4 REQ ↔ Task 역색인

| req_id | stability | task_ids | ac_covered/ac_total |
|---|---|---|---|
| FR-CONV-001 | evolving | T-PH004-17, T-PH004-18, T-PH004-19, T-PH004-20, T-PH004-23 | 2/2 |
| FR-CONV-002 | evolving | T-PH004-23 | 2/2 |
| FR-CONV-003 | evolving | T-PH004-01, T-PH004-02, T-PH004-03, T-PH004-04, T-PH004-23 | 4/4 |
| FR-CONV-004 | evolving | T-PH004-03, T-PH004-04, T-PH004-23 | 3/3 |
| FR-CONV-005 | evolving | T-PH004-05, T-PH004-06, T-PH004-23 | 3/3 |
| FR-CONV-006 | evolving | T-PH004-07, T-PH004-08 | 3/3 |
| FR-CONV-007 | evolving | T-PH004-07, T-PH004-08 | 9/9 |
| FR-CONV-008 | evolving | T-PH004-07, T-PH004-08 | 2/2 |
| FR-CONV-009 | evolving | T-PH004-07, T-PH004-08 | 7/7 |
| FR-CONV-010 | evolving | T-PH004-13, T-PH004-14, T-PH004-23 | 3/3 |
| FR-CONV-011 | evolving | T-PH004-09, T-PH004-10, T-PH004-23 | 4/4 |
| FR-CONV-012 | evolving | T-PH004-11, T-PH004-12, T-PH004-23 | 3/3 |
| FR-CONV-013 | evolving | T-PH004-15, T-PH004-16, T-PH004-23 | 4/4 |
| FR-CONV-014 | evolving | T-PH004-13, T-PH004-14, T-PH004-19, T-PH004-20, T-PH004-23 | 3/3 |
| FR-FIX-001 | evolving | T-PH003-21 | 4/4 |
| FR-FIX-002 | evolving | T-PH003-21 | 4/4 |
| FR-FIX-003 | evolving | T-PH003-21 | 3/3 |
| FR-FIX-004 | evolving | T-PH003-09, T-PH003-10, T-PH003-21 | 4/4 |
| FR-FIX-005 | evolving | T-PH003-11, T-PH003-12, T-PH003-21 | 3/3 |
| FR-FIX-006 | evolving | T-PH003-13, T-PH003-14, T-PH003-21 | 2/2 |
| FR-FIX-007 | evolving | T-PH003-15, T-PH003-16, T-PH003-21 | 3/3 |
| FR-FIX-008 | evolving | T-PH003-17, T-PH003-18, T-PH003-21 | 4/4 |
| FR-FIX-009 | evolving | T-PH003-19, T-PH003-20, T-PH003-21 | 2/2 |
| FR-MOD-007 | evolving | T-PH007-01 | 2/2 |
| FR-REL-001 | evolving | T-PH007-02, T-PH007-03, T-PH007-07 | 3/3 |
| FR-TEST-001 | evolving | T-PH005-01, T-PH005-07 | 4/4 |
| FR-TEST-002 | evolving | T-PH005-02, T-PH005-07 | 4/4 |
| FR-TEST-004 | evolving | T-PH003-21 | 5/5 |
| IR-REL-001 | evolving | T-PH007-01, T-PH007-07 | 3/3 |
| MIG-REL-001 | evolving | T-PH007-04, T-PH007-07 | 8/8 |
| NFR-CONV-001 | evolving | T-PH004-21, T-PH004-22, T-PH004-23 | 3/3 |
| NFR-TEST-001 | evolving | T-PH005-06, T-PH005-07, T-PH006-03 | 3/3 |

## §5 위험 · 미해결

### 5.1 위험

| risk_id | severity | description | mitigation | affected_task_ids |
|---|---|---|---|---|
| undefined | high |  | servlet6 / servlet61 분리 유지 (D2) 로 AbstractMethodError 회피. FR-CONV-001 의 sendRedirect(loc, sc, clearBuffer) 명시 override (T-PH004-19/20 green) 로 6.1 추상 메서드 누락 차단. | T-PH002-05, T-PH002-06, T-PH004-17, T-PH004-18, T-PH004-19, T-PH004-20 |
| undefined | high |  | OQ-003 의 외부 트래킹 (T-PH001-03) 을 PH-001 부터 시작하여 PH-007 진입 전까지 완비. Sonatype/GPG 미준비 시 PH-007 차단 명시. | T-PH001-03, T-PH007-05, T-PH007-06 |
| undefined | medium |  | README 호환성 매트릭스 + 정보 손실 5개 항목 (T-PH007-02) + 마이그레이션 가이드 (T-PH007-04) 작성. 0.0.1 사용자가 자기 환경 호환성을 사전 확인 가능. | T-PH007-02, T-PH007-04 |
| undefined | medium |  | servlet6/61 사본의 결함 수정 동기화 누락 위험. T-PH003-21 의 통합 회귀 검토에서 3개 모듈 모두 BM01~BM10 통과 강제 (TDD green 페어 단위로 verify). | T-PH003-03, T-PH003-04, T-PH003-05, T-PH003-06, T-PH003-07, T-PH003-08, T-PH003-09, T-PH003-10, T-PH003-11, T-PH003-12, T-PH003-13, T-PH003-14, T-PH003-15, T-PH003-16, T-PH003-17, T-PH003-18, T-PH003-19, T-PH003-20, T-PH003-21 |
| undefined | medium |  | ./gradlew check integrationTest 시간 + Tomcat embed 3가지 버전 시작·정지로 CI wall clock 5분 초과 가능. T-PH006-02 의 시간 측정 + Gradle cache 최적화로 mitigation. | T-PH006-01, T-PH006-02 |
| undefined | low |  | Java Toolchain 으로 JDK 8/11/17 자동 다운로드 — Windows CI 셀에서 네트워크/인증서 이슈 가능. T-PH006-02 의 첫 PR 검증에서 발견 후 조치. | T-PH006-01, T-PH006-02 |
| undefined | medium |  | TDD red+green 페어 분해 시 test_case 누락으로 AC 미커버 위험. Phase 4 validator C21~C23 자동 검증 (test_case.id 정규식 / ac_refs ⊆ inventory.ac_ids / red task 마다 ac_total 완전 커버) + coverage.ac_test_map 매트릭스로 사전 차단. | T-PH003-01, T-PH003-03, T-PH003-05, T-PH003-07, T-PH003-09, T-PH003-11, T-PH003-13, T-PH003-15, T-PH003-17, T-PH003-19, T-PH004-01, T-PH004-03, T-PH004-05, T-PH004-07, T-PH004-09, T-PH004-11, T-PH004-13, T-PH004-15, T-PH004-17, T-PH004-19, T-PH004-21 |
| undefined | low |  | TDD green task 가 red task 의존성 깨질 위험 (페어 순서 역전). depends_on_task DAG 검증 + green task 의 covers_ac 가 동일 페어 red 와 일치하는지 validator C24 자동 검사. | T-PH003-02, T-PH003-04, T-PH003-06, T-PH003-08, T-PH003-10, T-PH003-12, T-PH003-14, T-PH003-16, T-PH003-18, T-PH003-20, T-PH004-02, T-PH004-04, T-PH004-06, T-PH004-08, T-PH004-10, T-PH004-12, T-PH004-14, T-PH004-16, T-PH004-18, T-PH004-20, T-PH004-22 |

### 5.2 Open Questions

| id | question | resolve_at_phase | origin_priority |
|---|---|---|---|
| OQ-001 | [L-OPEN-1] planner Phase 5 mutation 169건 미실행 (priority=low) | best-effort | low |
| OQ-002 | [L-OPEN-2] 36 REQ block Verification Evidence 표 부재 (priority=low (자동 해소)) | best-effort | low (자동 해소) |
| OQ-003 | [L-OPEN-3] src/test/java 2 test 파일 미이동 (PH-005) | PH-005 | PH-005 |
| OQ-004 | [L-OPEN-4] ConverterSupport.java 미사용 (priority=low) | best-effort | low |
| OQ-005 | [L-OPEN-5] gradle-wrapper.jar 신규 + distributionUrl 변경 | commit-time | 커밋 시점 |
| OQ-006 | [L-OPEN-6] gradle.properties toolchain paths | PH-006 | PH-006 |
| OQ-007 | [L-OPEN-7] T-PH002-07 artifactId prefix mismatch | PH-007 | PH-007 |
| OQ-008 | [L-OPEN-8] T-PH003-01 sidecar.tdd ac_refs ↔ SRS FR-TEST-004 AC 매핑 모순 | PH-007 | low (planner 책임, PH-007) |
| OQ-009 | [L-OPEN-9] BugMuseumTest BM10/META AC-3 parallel test latent ThreadLocal race | PH-005 | PH-005 |
| OQ-010 | [L-OPEN-10] T-PH003-03 sidecar test_symbol ↔ round 2 body 의미 미스매치 (INFO) | PH-007 | PH-007 |
| OQ-011 | [L-OPEN-11] T-PH003-04 까칠 리뷰 LOW 2: read(byte[]) overload pre-existing 비대칭 + readLine override @Override 누락 (priority=low (pre-existing 또는 stylistic, FR-FIX-001 범위 외, 후속 refactor task 또는 자체 cleanup 시점 | best-effort | low (pre-existing 또는 stylistic, FR-FIX-001 범위 외, 후속 refactor task 또는 자체 cleanup 시점) |
| OQ-012 | [L-OPEN-12] T-PH003-05 sidecar action AC-3 label "jakarta_javax_symmetry" ↔ SRS AC-3 "BM02 회귀 통과" 의미 미스매치 (LOW S1) | PH-007 | PH-007 (planner 책임) |
| OQ-013 | [L-OPEN-13] T-PH003-05 AC-4 IT08 통합테스트 proxy 와 실제 Tomcat embed HTTP 멀티파트 의미 간극 (LOW S1) | PH-005 | PH-005 (IT08 통합테스트 작성 시점) |
| OQ-014 | [L-OPEN-14] T-PH003-05 AC-4 direction A 의 SOE 가 direction B 진입 차단 → red 에서 verify-first 패턴 무효 (HIGH S2, green 후 자동 해소) (priority=auto-resolved (T-PH003-06 green 후)) | best-effort | auto-resolved (T-PH003-06 green 후) |
| OQ-015 | [L-OPEN-15] T-PH003-05 AC-1 assertion 메시지 ("adapter must not throw...") ↔ 클래스 javadoc 약속 시그니처 ("expected: ... got: StackOverflowError") 불일치 (HIGH S2, stylistic) | PH-007 | PH-007 (refactor 시점) |
| OQ-016 | [L-OPEN-16] T-PH003-06 까칠 LOW 2: catch(Exception) fallback 제거 정보성 + servlet6/61 PartDefectTest 미러링 부재 (개선 제안) (priority=low (3 모듈 byte-for-byte 일치로 회귀 검출 가능)) | best-effort | low (3 모듈 byte-for-byte 일치로 회귀 검출 가능) |
| OQ-017 | [L-OPEN-17] T-PH003-07 sidecar AC-3 label "origin_method_invoked" ↔ SRS AC-3 "BM05 회귀 통과" 부분 표현 (S1 MINOR_GAP, 테스트 코드 자체는 정확) | PH-007 | PH-007 (planner 책임) |
| OQ-018 | [L-OPEN-18] T-PH003-08 까칠 LOW 1: servlet6/61 측 ServletResponseDefectTest mirror 부재 (L-OPEN-16 과 동질, 회귀는 servlet5 만 커버) (priority=low (3 모듈 byte-for-byte 일치로 검출 가능)) | best-effort | low (3 모듈 byte-for-byte 일치로 검출 가능) |

### 5.3 unreferenced_reqs

(none — all 32 in-scope REQ are mapped to ≥1 task)

### 5.4 deferred_ac

| req_id | ac_id | reason | user_decision_id |
|---|---|---|---|
| IR-REL-001 | L-OPEN-7 | [L-OPEN-7] T-PH002-07 artifactId prefix mismatch | v03-carryover-PH-007 |
| IR-REL-001 | L-OPEN-8 | [L-OPEN-8] T-PH003-01 sidecar.tdd ac_refs ↔ SRS FR-TEST-004 AC 매핑 모순 | v03-carryover-PH-007 |
| IR-REL-001 | L-OPEN-10 | [L-OPEN-10] T-PH003-03 sidecar test_symbol ↔ round 2 body 의미 미스매치 (INFO) | v03-carryover-PH-007 |
| IR-REL-001 | L-OPEN-12 | [L-OPEN-12] T-PH003-05 sidecar action AC-3 label "jakarta_javax_symmetry" ↔ SRS AC-3 "BM02 회귀 통과" 의미 미스매치 (LOW S1) | v03-carryover-PH-007 |
| IR-REL-001 | L-OPEN-15 | [L-OPEN-15] T-PH003-05 AC-1 assertion 메시지 ("adapter must not throw...") ↔ 클래스 javadoc 약속 시그니처 ("expected: ... got: StackOverflowError") 불일치 (HIGH S2, stylistic) | v03-carryover-PH-007 |
| IR-REL-001 | L-OPEN-17 | [L-OPEN-17] T-PH003-07 sidecar AC-3 label "origin_method_invoked" ↔ SRS AC-3 "BM05 회귀 통과" 부분 표현 (S1 MINOR_GAP, 테스트 코드 자체는 정확) | v03-carryover-PH-007 |

### 5.5 TDD 결정 (accept-as-exempt / add-test-task)

| task_id | decision | reason | user_decision_id |
|---|---|---|---|
| T-PH005-01 | accept-as-exempt | Integration test harness/fixture authoring; the test fixtures themselves constitute the TDD acceptance for FR-FIX/FR-CONV implementation phases. Meta-TDD on test infrastructure is impractical and not  | v03-carryover |
| T-PH005-02 | accept-as-exempt | Integration test harness/fixture authoring; the test fixtures themselves constitute the TDD acceptance for FR-FIX/FR-CONV implementation phases. Meta-TDD on test infrastructure is impractical and not  | v03-carryover |
| T-PH007-01 | accept-as-exempt | Release artifact authoring (CHANGELOG/README badges/version bump); validated by file_state checks and Maven Central staging verification, not behavior tests. | v03-carryover |
| T-PH007-04 | accept-as-exempt | Deprecated alias subclasses are pure delegation wrappers to renamed adapter.jakarta.servlet5.* classes; correctness validated by ./gradlew :servlet5:compileJava on legacy 0.x import shape + javadoc in | v03-carryover |

## §6 부록

### 6.1 사이드카 JSON 경로 / md_sha256

- sidecar: `docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v03.sidecar.json`
- md_sha256: `9b3125fa9ac5c537740e55ee5e594f086e57a76daa7889eebcc45ebcd0d8a595`

### 6.2 검증 스크립트 실행

```bash
node ~/.claude/skills/kiwi-planner/validator.mjs \
  docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v03.plan.md \
  docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v03.sidecar.json \
  --target v1.0.0 \
  --inventory-file docs/analysis/kiwi-planner-2026-05-19-jakarta-adapter-v1-0-0-v03/inventory.json \
  --out docs/plans/2026-05-19-jakarta-adapter-v1-0-0-v03.validator.json
```

### 6.3 mcp_call_log 요약

- 호출 수 (계획 단계): 0
- mutation 예정 (Phase 5):
  - 단계 1: add_trace_link × 46 task (각 task 의 trace_links[] 합산 multiset)
  - 단계 2: add_verification_evidence × 32 REQ (coverage[].req_id 마다 1건)

## §7 evaluation_topology 메타 (kiwi-coder 호출 토폴로지 힌트)

각 task 의 `evaluation_topology` 필드는 kiwi-coder 가 호출 시 적용할 검증 깊이를 시그널한다 (validator 는 unknown field 허용).

| 값 | 적용 task | 호출 토폴로지 | 호출 수/페어 |
|---|---|---|---|
| `simplified` | PH-003 잔여 6 페어 (FR-FIX-004~009 red+green) = 12 task | 시니어 + S4 only + green 시니어 + 정형 Sonnet + 까칠 skip | 5 |
| `full` | PH-004 변환 의미론 페어 22 task + (T-PH004-23 review 1) | 시니어 + S1~S4 + green 시니어 + 정형 Sonnet + 까칠 Opus | 8 |
| `file_op` | review/doc/file_op/pr/issue/TDD-exempt code = 8 task (T-PH003-21, T-PH004-23, T-PH005-01/02, T-PH007-01/02/03/04, T-PH005-07, T-PH007-07) | 시니어 + 정형 sonnet 검증만 | 2 |
| `infra` | T-PH005-06, T-PH006-03 (CI / JaCoCo / cross-JDK matrix) | 시니어 + sonnet 검증 + shell 직접 실행 검증 | 3 |

