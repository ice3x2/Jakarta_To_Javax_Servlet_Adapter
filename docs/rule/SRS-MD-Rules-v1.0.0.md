# SRS-MD Authoring Rules v1.0.0

부제: **확장 ISO/IEC/IEEE 29148 기반 Git-native Markdown SRS 작성 규칙**

---

## 0. 문서 메타데이터

| Field | Value |
|---|---|
| Document ID | SRS-MD-RULES |
| Document Type | authoring_rules |
| Version | 1.0.0 |
| Status | stable |
| Audience | developers, reviewers, maintainers, AI coding agents |
| Primary Use | SRS 작성, 검토, 검색, 상태 갱신, 구현 검증 |
| Source Format | GitHub Flavored Markdown |
| Canonical Location | `docs/rule/SRS-MD-Rules-v1.0.0.md` |
| Last Updated | 2026-05-10 |

---

## 1. 목적

이 문서는 저장소 안에서 Software Requirements Specification, 이하 SRS, 를 Markdown으로 작성하고 유지하기 위한 규칙을 정의한다.

이 규칙의 목적은 다음과 같다.

1. SRS 문서를 사람이 읽기 쉬운 Markdown으로 유지한다.
2. 요구사항 블록을 스크립트와 AI coding agent가 안정적으로 찾고 해석할 수 있게 한다.
3. 기능 스코프, 목표 버전, 상태, 수용 기준, 검증 증거, 관련 문서, 이슈, 변경 근거를 한 요구사항 단위 안에서 관리한다.
4. 구현 완료와 검증 완료를 구분한다.
5. Git diff와 code review에 적합한 최소 변경 단위로 요구사항을 관리한다.
6. 별도의 전역 도구 없이 저장소 내부 문서와 선택적 repo-local script만으로 운영할 수 있게 한다.

이 규칙은 ISO/IEC/IEEE 29148의 요구사항 공학 원칙을 Markdown 운영에 맞게 축약하고 확장한 것이다. 이 문서는 공식 표준의 대체물이 아니라, 소프트웨어 개발 저장소에서 사용할 수 있는 실무형 작성 규약이다.

---

## 2. 핵심 원칙

### 2.1 Markdown 원본 원칙

SRS Markdown 문서가 요구사항의 원본이다.

```text
SRS Markdown = source of truth
Index Markdown = navigation and summary
Repo-local scripts = validation and convenience tools
Git history = change history
Issue / PR / tests / docs = trace and evidence
```

요구사항을 관리하기 위해 별도의 데이터베이스, 서버, 전역 CLI, 양방향 변환 체계를 전제로 하지 않는다.

### 2.2 제한형 Markdown 원칙

SRS 문서는 자유 형식 Markdown이 아니다. 사람은 자연스럽게 읽을 수 있어야 하지만, 요구사항 블록은 스크립트가 파싱할 수 있는 제한형 Markdown 구조를 따른다.

### 2.3 단일 요구사항 원칙

하나의 Requirement Block은 하나의 검증 가능한 요구사항만 표현한다. 여러 동작, 여러 정책, 여러 시스템 경계가 섞이면 요구사항을 분리한다.

### 2.4 구현 상태와 검증 상태 분리 원칙

코드가 작성되었다는 사실과 요구사항이 검증되었다는 사실은 다르다. 이 규칙은 `implemented`와 `verified` 상태를 분리한다.

### 2.5 최소 변경 원칙

SRS를 수정할 때는 관련 Requirement Block만 변경한다. 전체 문서 재작성, 섹션 순서 변경, 대량 formatting 변경은 피한다.

### 2.6 추적 가능성 원칙

요구사항은 가능한 한 GitHub Issue, Pull Request, 테스트, 구현 파일, 연구/분석 문서, 기술 문서, 다른 요구사항과 연결한다.

### 2.7 검증 증거 우선 원칙

요구사항을 `verified`로 표시하려면 수용 기준이 충족되었고, 그 사실을 뒷받침하는 증거가 있어야 한다.

---

## 3. 용어

| Term | Definition |
|---|---|
| SRS | Software Requirements Specification. 시스템 또는 소프트웨어가 만족해야 하는 요구사항 명세. |
| SRS Index | 전체 SRS 문서 세트를 탐색하기 위한 인덱스 문서. 기본 파일은 `docs/spec/00.index.md`. |
| Scope SRS | 기능 스코프별 요구사항 문서. 예: `docs/spec/10.auth.srs.md`. |
| Requirement Block | 하나의 요구사항을 표현하는 Markdown 블록. `### {ID} — {Title}` heading으로 시작한다. |
| Requirement ID | 요구사항의 전역 고유 식별자. 예: `FR-AUTH-001`. |
| Scope | 요구사항을 묶는 기능, 모듈, 도메인, 컴포넌트, 패키지 또는 서브시스템 단위. |
| Target | 요구사항이 목표로 하는 버전, release, milestone, phase, objective. |
| Status | 요구사항의 구현 및 검증 진행 상태. |
| Acceptance Criteria | 요구사항 충족 여부를 판정하기 위한 수용 기준. |
| Verification Evidence | 요구사항이 충족되었음을 뒷받침하는 테스트, PR, 코드, 리뷰, 분석, 데모, 운영 증거. |
| Trace Links | 요구사항과 이슈, PR, 테스트, 문서, 코드, 다른 요구사항 사이의 관계. |
| Research / Analysis | 요구사항을 뒷받침하는 조사, 비교, 분석, 실험, 의사결정 근거. |
| Implementation Notes | 구현자가 알아야 할 설계, 제약, 주의점. 요구사항 자체는 아니다. |

---

## 4. 권장 저장소 구조

```text
docs/
├─ rule/
│  └─ SRS-MD-Rules-v1.0.0.md
│
├─ spec/
│  ├─ 00.index.md
│  ├─ 10.auth.srs.md
│  ├─ 20.user.srs.md
│  ├─ 30.payment.srs.md
│  ├─ 40.observability.srs.md
│  └─ 90.appendix.md
│
├─ analysis/
│  ├─ session-timeout-analysis.md
│  └─ payment-provider-comparison.md
│
├─ tech/
│  ├─ auth-token-design.md
│  └─ payment-webhook-design.md
│
└─ adr/
   ├─ 0001-use-session-cookie.md
   └─ 0002-payment-provider-selection.md

scripts/
└─ spec/
   ├─ validate-spec.js
   ├─ list-by-target.js
   ├─ list-by-status.js
   ├─ update-status.js
   ├─ summarize-target.js
   ├─ check-links.js
   └─ extract-requirements.js
```

각 경로의 책임은 다음과 같다.

| Path | Responsibility |
|---|---|
| `docs/rule/SRS-MD-Rules-v1.0.0.md` | SRS-MD authoring, parsing, validation, agent workflow rules |
| `docs/spec/00.index.md` | 전체 SRS 문서 세트의 진입점, target 목록, scope 문서 링크, 요약 |
| `docs/spec/*.srs.md` | 기능 스코프별 요구사항 원본 |
| `docs/spec/90.appendix.md` | 공통 용어, enum, 상태 정의, cross-scope map, 보조 규칙 |
| `docs/analysis/*.md` | 조사, 비교, 분석, 실험 결과, 근거 문서 |
| `docs/tech/*.md` | 기술 설계, API 설계, 데이터 모델, 운영 정책 |
| `docs/adr/*.md` | 구조적 의사결정 기록 |
| `scripts/spec/*.js` | 문서 검증, 검색, 요약, 상태 갱신 보조 스크립트 |

---

## 5. 파일 명명 규칙

### 5.1 SRS Index

인덱스 문서 파일명은 다음을 권장한다.

```text
docs/spec/00.index.md
```

### 5.2 Scope SRS

Scope SRS 파일명은 다음 형식을 따른다.

```text
docs/spec/{NN}.{scope-slug}.srs.md
```

예:

```text
docs/spec/10.auth.srs.md
docs/spec/20.agent-loop.srs.md
docs/spec/30.llm-provider.srs.md
docs/spec/40.observability.srs.md
```

규칙:

1. `{NN}`은 정렬용 두 자리 숫자다.
2. `{scope-slug}`는 lowercase kebab-case를 사용한다.
3. 파일명은 scope의 의미를 드러내야 한다.
4. 한 파일은 하나의 primary scope를 대표한다.
5. cross-scope 요구사항은 가장 책임이 큰 scope 문서에 둔다.

---

## 6. Markdown 문법 규칙

### 6.1 기준 문법

SRS 문서는 GitHub Flavored Markdown, 이하 GFM, 을 기준으로 작성한다.

### 6.2 허용 문법

| Markdown Feature | Allowed | Use |
|---|---:|---|
| ATX headings, `#` to `####` | yes | 문서, 섹션, 요구사항 구조 |
| Pipe tables | yes | metadata, evidence, trace links |
| Task lists | yes | Acceptance Criteria |
| Bullet lists | yes | 설명, note, analysis links |
| Ordered lists | yes | 절차 설명 |
| Fenced code blocks | yes | API 예시, 설정 예시, pseudo-code |
| Inline code | yes | enum, 파일명, endpoint, identifier |
| Markdown links | yes | 문서, issue, PR, 외부 자료 링크 |

### 6.3 제한 또는 금지 문법

| Markdown Feature | Rule | Reason |
|---|---|---|
| YAML front matter | 금지 | 원본 구조가 Markdown 본문 밖으로 분산됨 |
| Raw HTML | 금지 | 렌더러와 파서 간 결과 차이 증가 |
| Heading 내부 link | 금지 | Requirement ID와 title 파싱 불안정 |
| Heading 내부 emoji | 금지 | Requirement ID와 title 파싱 불안정 |
| Heading 내부 emphasis | 금지 | title 정규화 불안정 |
| Metadata table cell 줄바꿈 | 금지 | table parser 불안정 |
| Nested task list | 금지 | Acceptance Criteria 판정 불안정 |
| Metadata table 안의 복잡한 list | 금지 | 값 추출 불안정 |
| 동일 requirement 안의 중복 section heading | 금지 | block parser 불안정 |

### 6.4 공백 규칙

1. heading 앞에는 빈 줄 하나를 둔다.
2. table 앞에는 빈 줄 하나를 둔다.
3. fenced code block 앞뒤에는 빈 줄 하나를 둔다.
4. Requirement heading 바로 아래에는 metadata table을 둔다.
5. Requirement Block 사이에는 빈 줄 두 개 이하를 둔다.

---

## 7. SRS Index 문서 규칙

`docs/spec/00.index.md`는 전체 요구사항 문서 세트를 탐색하기 위한 문서다. 이 문서는 요구사항 원문을 길게 포함하지 않는다.

### 7.1 필수 구조

```md
# SRS Index

| Field | Value |
|---|---|
| Document Type | srs_index |
| Version | 1.0.0 |
| Active Target |  |
| Last Updated | YYYY-MM-DD |

## 1. Purpose

## 2. SRS Documents

## 3. Target Map

## 4. Scope Map

## 5. Status Summary

## 6. Requirement Type Summary

## 7. Completed Work Log

| Date | Target | Scope | Requirement IDs | Summary | Report Paths |
|---|---|---|---|---|---|

## 8. Cross-scope Dependencies

## 9. Open Questions

## 10. Reference Documents
```

### 7.2 SRS Documents 섹션

```md
## 2. SRS Documents

| Scope | Document | Prefix | Description |
|---|---|---|---|
| Auth | [10.auth.srs.md](./10.auth.srs.md) | AUTH | 인증, 세션, 권한 요구사항 |
| Payment | [30.payment.srs.md](./30.payment.srs.md) | PAY | 결제, webhook, 정산 요구사항 |
| Observability | [40.observability.srs.md](./40.observability.srs.md) | OBS | 로그, 메트릭, tracing 요구사항 |
```

규칙:

1. 모든 Scope SRS 문서는 이 표에 등록한다.
2. `Document`는 상대 링크를 사용한다.
3. `Prefix`는 Requirement ID의 scope segment와 일치해야 한다.
4. 문서를 삭제하거나 이름을 바꾸면 index를 함께 수정한다.

### 7.3 Target Map 섹션

```md
## 3. Target Map

| Target | Type | Status | Description |
|---|---|---|---|
| v0.1 | version | active | 기본 인증 및 사용자 기능 |
| v0.2 | version | planned | 결제 webhook 및 관측성 개선 |
| MVP | milestone | planned | 초기 릴리스에 필요한 최소 기능 |
```

Target status 값:

| Status | Meaning |
|---|---|
| `planned` | 아직 진행하지 않음 |
| `active` | 현재 진행 중 |
| `frozen` | 범위가 동결됨. 변경하려면 명시적 리뷰 필요 |
| `completed` | 해당 target의 요구사항이 완료됨 |
| `released` | release-readiness를 통과한 immutable baseline target |
| `archived` | 과거 target. 일반적으로 수정하지 않음 |

규칙:

1. index metadata table의 `Active Target` row는 항상 존재해야 한다.
2. `Active Target` 값이 비어 있으면 active target이 선택되지 않은 상태다.
3. `Active Target` row가 존재하지만 값이 비어 있으면 Target Map fallback을 수행하지 않는다.
4. `Active Target` 값이 비어 있지 않으면 Target Map의 `Target` 값 중 하나여야 한다.
5. `Active Target`과 같은 Target Map row의 `Status`는 `active`여야 한다.
6. 완료되어 release baseline으로 고정된 target은 `released`를 사용할 수 있으며, `completed`는 완료되었지만 release baseline으로 공식화되지 않은 target에 사용한다.
7. v1.2.0 hardening policy에서는 전체 Target Map에서 `active` status row를 최대 하나만 허용하며, multiple active row는 index consistency diagnostic 대상이다.

### 7.4 Completed Work Log 섹션

```md
## 7. Completed Work Log

| Date | Target | Scope | Requirement IDs | Summary | Report Paths |
|---|---|---|---|---|---|
| 2026-05-10 | v1.0.0 | CLI, MCP | IR-CLI-008, FR-MCP-008 | Active Target 조회/갱신 UX를 CLI와 MCP에 연결했다. | docs/reports/v1.0.0.md |
```

규칙:

1. Completed Work Log는 완료 판정의 source of truth가 아니라 index-level summary다.
2. 완료 판정은 Requirement Block의 `Status`, Acceptance Criteria, Verification Evidence, Change Notes를 우선한다.
3. `Date`는 `YYYY-MM-DD` 형식을 사용한다.
4. `Target`은 비어 있을 수 있으며, 비어 있는 row는 cross-target completed work로 해석한다.
5. `Scope`와 `Requirement IDs`는 comma-separated 값을 허용한다.
6. `Report Paths`는 선택 column이다. Parser는 legacy five-column Completed Work Log와 trailing `Report Paths`가 있는 six-column Completed Work Log를 모두 읽어야 한다.
7. `Report Paths` cell은 comma-separated repository-relative POSIX path list다. Blank cell은 빈 배열로 해석한다.
8. Report path token은 absolute path, `./` 또는 `../` prefix, `..` segment, URL scheme, backslash, pipe, comma, CR/LF, `#`, trim 후 빈 값을 사용할 수 없다.
9. Malformed report path는 `SRS-W024` warning으로 보고한다. Report path는 Completed Work Log summary metadata이며 Verification Evidence가 아니다.
10. table cell에는 pipe 문자 `|`를 넣지 않는다.

Target type 값:

| Type | Meaning |
|---|---|
| `version` | 제품 또는 소프트웨어 버전 |
| `release` | 배포 묶음 |
| `milestone` | 일정 또는 성과 기준 마일스톤 |
| `phase` | 개발 단계 |
| `objective` | 특정 목표 |
| `experiment` | 실험 또는 검증 단계 |

---

## 8. Scope SRS 문서 규칙

Scope SRS는 기능 스코프별 요구사항 원본이다.

### 8.1 필수 구조

```md
# Auth SRS

| Field | Value |
|---|---|
| Document Type | scope_srs |
| Scope | AUTH |
| Scope Name | Authentication |
| Version | 1.0.0 |
| Last Updated | YYYY-MM-DD |

## 1. Scope Overview

## 2. Scope Boundaries

## 3. Assumptions and Constraints

## 4. Requirements

## 5. Cross-scope Dependencies

## 6. Open Questions

## 7. Change Notes
```

### 8.2 Scope Overview

Scope Overview는 이 문서가 다루는 기능 영역을 설명한다.

작성 예:

```md
## 1. Scope Overview

이 문서는 사용자 인증, 로그인 세션, 토큰 갱신, 권한 확인과 관련된 요구사항을 정의한다.
```

### 8.3 Scope Boundaries

Scope Boundaries는 포함 범위와 제외 범위를 구분한다.

```md
## 2. Scope Boundaries

### In Scope

- 사용자 로그인
- 세션 만료 처리
- refresh token 회전
- 보호된 API 접근 제어

### Out of Scope

- 관리자 권한 모델
- 결제 권한 정책
- 외부 SSO 연동
```

### 8.4 Assumptions and Constraints

```md
## 3. Assumptions and Constraints

- 서버는 HTTPS 환경에서 운영된다.
- access token은 짧은 수명을 가진다.
- refresh token 저장 방식은 `docs/tech/auth-token-design.md`를 따른다.
```

### 8.5 Requirements

모든 Requirement Block은 `## 4. Requirements` 아래에 둔다.

### 8.6 Cross-scope Dependencies

다른 scope와의 주요 의존성을 문서 단위로 요약한다.

```md
## 5. Cross-scope Dependencies

| From | To | Relation | Notes |
|---|---|---|---|
| AUTH | USER | depends_on | 인증 후 사용자 요약 정보를 조회한다. |
| AUTH | OBS | requires | 인증 실패와 토큰 오류는 로그로 기록되어야 한다. |
```

---

## 9. Requirement Block 개요

Requirement Block은 SRS-MD의 최소 관리 단위다.

블록은 다음 heading으로 시작한다.

```md
### FR-AUTH-001 — 세션 만료 시 재로그인 요구
```

heading 다음에는 metadata table이 온다.

```md
| Field | Value |
|---|---|
| Type | functional |
| Target | v0.1 |
| Status | planned |
```

그 다음 고정 section들이 온다.

```md
#### Requirement

#### Rationale

#### Acceptance Criteria

#### Verification Evidence

#### Trace Links

#### Research / Analysis

#### Implementation Notes

#### Change Notes
```

### 9.1 Requirement Block의 경계

Requirement Block은 다음 중 하나를 만날 때 끝난다.

1. 다음 `### {RequirementID} — {Title}` heading
2. 현재 Scope SRS 파일의 끝

따라서 Requirement Block 안에서는 `###` heading을 사용하지 않는다. Requirement Block 내부 섹션은 반드시 `####` heading을 사용한다.

---

## 10. Requirement Heading 규칙

### 10.1 형식

요구사항 heading은 반드시 다음 형식을 따른다.

```md
### {RequirementID} — {Title}
```

예:

```md
### FR-AUTH-001 — 세션 만료 시 재로그인 요구
```

### 10.2 정규식

권장 정규식:

```regex
^###\s+([A-Z]{2,5}-[A-Z0-9][A-Z0-9-]{1,24}-[0-9]{3,4})\s+—\s+(.+)$
```

### 10.3 heading 규칙

1. `###` 세 단계 heading을 사용한다.
2. ID와 Title 사이에는 em dash `—`를 사용한다.
3. hyphen `-`, en dash `–`, colon `:`은 사용하지 않는다.
4. title은 한 줄이어야 한다.
5. title 안에 link, emoji, emphasis, inline code를 넣지 않는다.
6. title은 요구사항의 핵심 결과를 표현해야 한다.
7. title은 80자 이내를 권장한다.

---

## 11. Requirement ID 규칙

### 11.1 기본 형식

```text
{PREFIX}-{SCOPE}-{NNN}
```

예:

```text
FR-AUTH-001
SEC-AUTH-001
IR-PAY-001
PERF-SEARCH-001
OBS-AGENT-001
```

### 11.2 ID 정규식

```regex
^(FR|NFR|IR|DR|SEC|PERF|REL|OBS|OPS|MIG|CON)-[A-Z0-9][A-Z0-9-]{1,24}-[0-9]{3,4}$
```

### 11.3 Prefix와 Type 매핑

| Prefix | Type | Meaning |
|---|---|---|
| `FR` | `functional` | 기능 요구사항 |
| `NFR` | `non_functional` | 일반 비기능 요구사항 |
| `IR` | `interface` | 외부 API, 내부 API, UI boundary, integration interface |
| `DR` | `data` | 데이터 모델, 저장, 조회, 정합성, retention |
| `SEC` | `security` | 인증, 인가, 암호화, 보안 정책 |
| `PERF` | `performance` | latency, throughput, resource usage |
| `REL` | `reliability` | 장애 복구, 내결함성, 재시도, 가용성 |
| `OBS` | `observability` | logging, metrics, tracing, alerting |
| `OPS` | `operational` | 배포, 운영, 설정, runbook |
| `MIG` | `migration` | 데이터 또는 시스템 마이그레이션 |
| `CON` | `constraint` | 기술, 법적, 조직적, 환경적 제약 |

### 11.4 ID 운영 규칙

1. Requirement ID는 저장소 전체에서 전역 유일해야 한다.
2. 폐기된 ID를 재사용하지 않는다.
3. 같은 요구사항의 문구를 수정해도 ID는 유지한다.
4. 요구사항을 둘로 나누면 새 ID를 만든다.
5. 여러 요구사항을 병합하면 대표 ID 하나를 유지하고 나머지는 `discarded` 또는 `Superseded By`로 처리한다.
6. ID의 prefix와 metadata table의 `Type`은 일치해야 한다.
7. ID의 scope segment는 SRS Index의 Scope Map에 등록된 prefix와 일치해야 한다.

---

## 12. Metadata Table 규칙

### 12.1 기본 형식

Requirement heading 바로 아래에는 metadata table을 둔다.

```md
| Field | Value |
|---|---|
| Type | functional |
| Target | v0.1 |
| Status | planned |
| Priority | high |
| Tags | auth, session, security |
```

### 12.2 필수 Field

| Field | Required | Description |
|---|---:|---|
| `Type` | yes | 요구사항 타입. ID prefix와 일치해야 한다. |
| `Target` | yes | 목표 버전, release, milestone, phase, objective. |
| `Status` | yes | 요구사항 진행 상태. |

### 12.3 권장 Field

| Field | Required | Description |
|---|---:|---|
| `Priority` | no | 우선순위. |
| `Tags` | no | 검색과 분류를 위한 comma-separated tag. |
| `Scope` | no | scope code. 파일의 primary scope와 다르면 명시한다. |
| `Owner` | no | 담당자, 팀, 역할. |
| `Risk` | no | 위험도. |
| `Stability` | no | 요구사항 안정성. |
| `Verification Method` | no | 검증 방식. |
| `GitHub Issue` | no | 추적 이슈 URL. |
| `Related Docs` | no | 관련 분석/기술/결정 문서 링크. |
| `Source` | no | 요구사항 출처. |
| `Supersedes` | no | 이 요구사항이 대체하는 요구사항 ID. |
| `Superseded By` | no | 이 요구사항을 대체하는 요구사항 ID. |
| `Last Reviewed` | no | 마지막 검토일, `YYYY-MM-DD`. |

### 12.4 Metadata 값 규칙

1. Field 이름은 정확한 영어 key를 사용한다.
2. Field 값은 한 줄로 작성한다.
3. table cell 안에 줄바꿈을 넣지 않는다.
4. 값이 없으면 `-`를 사용한다.
5. `Tags`는 comma-separated 값으로 작성한다.
6. `Related Docs`는 Markdown link 목록으로 작성한다.
7. metadata table 안에서 복잡한 문장을 길게 쓰지 않는다.
8. metadata table에 없는 세부 설명은 본문 section에 작성한다.

---

## 13. Type 규칙

허용 Type 값:

| Type | Meaning |
|---|---|
| `functional` | 사용자가 보거나 시스템이 수행하는 기능 동작 |
| `non_functional` | 특정 품질 속성에 속하지 않는 일반 비기능 요구사항 |
| `interface` | API, UI, protocol, external system boundary |
| `data` | 데이터 구조, 저장, 조회, 정합성, retention |
| `security` | 인증, 인가, 비밀 관리, 보안 통제 |
| `performance` | 응답 시간, 처리량, 부하, 리소스 사용량 |
| `reliability` | 장애 복구, 재시도, 가용성, 일관성 유지 |
| `observability` | 로그, 메트릭, tracing, alerting |
| `operational` | 배포, 설정, 운영, runbook, admin task |
| `migration` | 데이터 또는 시스템 전환 |
| `constraint` | 구현 방식, 기술, 법률, 조직, 환경 제약 |

규칙:

1. Type 값은 소문자 snake_case를 사용한다.
2. Type은 ID prefix와 일치해야 한다.
3. 요구사항이 둘 이상의 Type에 걸치면 primary Type을 하나 선택하고 나머지는 `Tags` 또는 `Trace Links`로 보조한다.
4. 기능 동작과 품질 기준이 함께 있으면 요구사항을 분리한다.

---

## 14. Status 규칙

### 14.1 허용 Status 값

| Status | Meaning |
|---|---|
| `planned` | 요구사항이 정의되었으나 구현 전 |
| `in_progress` | 구현 중 |
| `blocked` | 구현 또는 검증이 차단됨 |
| `implemented` | 코드 구현은 완료되었으나 검증 증거가 충분하지 않음 |
| `verified` | 수용 기준이 충족되고 검증 증거가 연결됨 |
| `discarded` | 폐기됨. 물리 삭제 대신 사용 권장 |

### 14.2 상태 전이

허용되는 일반 전이:

```text
planned -> in_progress
planned -> discarded
in_progress -> blocked
blocked -> in_progress
in_progress -> implemented
implemented -> in_progress
implemented -> verified
verified -> in_progress
verified -> discarded
```

### 14.3 implemented 조건

`implemented`로 변경하려면 다음 중 하나 이상이 있어야 한다.

1. 관련 Pull Request 링크
2. 관련 구현 파일 링크 또는 경로
3. 관련 commit, branch, 또는 코드 위치
4. 구현자가 남긴 Implementation Notes

`implemented`는 “검증 완료”를 의미하지 않는다.

### 14.4 verified 조건

`verified`로 변경하려면 다음 조건을 모두 만족해야 한다.

1. `#### Acceptance Criteria` 섹션이 존재한다.
2. Acceptance Criteria가 최소 1개 이상 존재한다.
3. 모든 Acceptance Criteria가 `- [x]` 상태다.
4. `#### Verification Evidence` 섹션이 존재한다.
5. Verification Evidence table에 최소 1개 이상의 evidence row가 있다.
6. Evidence row의 `Reference` 값이 비어 있지 않다.
7. 가능하면 각 AC가 evidence의 `Covers` column에서 직접 또는 `all`로 커버된다.

---

## 15. Priority, Risk, Stability 규칙

### 15.1 Priority

| Priority | Meaning |
|---|---|
| `critical` | target 달성을 위해 반드시 필요하며 누락 시 release 불가 |
| `high` | 핵심 기능 또는 품질에 중요 |
| `medium` | 일반 요구사항 |
| `low` | 후순위 요구사항 |
| `optional` | 선택적 개선 사항 |

### 15.2 Risk

| Risk | Meaning |
|---|---|
| `low` | 구현과 검증 리스크가 낮음 |
| `medium` | 일부 불확실성 존재 |
| `high` | 기술, 일정, 정책, 외부 의존 리스크가 큼 |
| `critical` | target 또는 시스템 안정성에 중대한 위험 |

`Risk = high` 또는 `Risk = critical`이면 `Research / Analysis` 또는 `Trace Links`에 근거 문서를 연결하는 것을 권장한다.

### 15.3 Stability

Canonical `Stability` values are:

| Stability | Meaning |
|---|---|
| `draft` | 아직 구현 계약으로 신뢰하면 안 되는 초안 |
| `evolving` | 구체화 중이지만 구현 후보로 검토 가능 |
| `stable` | 구현 가능할 정도로 안정됨 |
| `frozen` | target 범위에서 동결됨. 변경 시 리뷰 필요 |
| `deprecated` | 명시 조회는 가능하지만 새 작업 후보에서 제외됨 |

Legacy compatibility:

- `volatile`은 과거 문서 호환을 위한 legacy value다.
- 새 Requirement Block은 `volatile`을 생성하지 않아야 한다.
- validator는 unknown `Stability`를 error로 보고하고, legacy `volatile`은 migration warning으로 보고한다.
- `verified` 요구사항은 `Stability=draft`일 수 없다.
- non-discarded `draft` 요구사항이 active 또는 released target에 있으면 stability warning으로 보고한다.
- agent는 사용자가 명시적으로 override하지 않는 한 non-discarded `draft` 또는 `deprecated` 요구사항 구현을 시작하지 않아야 한다.

---

## 16. Target 규칙

Target은 요구사항이 속한 구현 목표, release, 버전, phase, milestone을 의미한다.

예:

```text
MVP
v0.1
v0.2
alpha-1
2026-Q2
phase-1
```

규칙:

1. 하나의 요구사항은 기본적으로 하나의 `Target`만 가진다.
2. 여러 target에 걸치는 요구사항은 가능한 한 분리한다.
3. 모든 Target 값은 `docs/spec/00.index.md`의 Target Map에 등록한다.
4. 미등록 Target은 validation warning이다.
5. `frozen` target에 속한 요구사항을 변경할 때는 변경 이유를 `Change Notes`에 남긴다.
6. target이 바뀌면 Requirement Block의 `Target` row만 바꾸지 말고, 필요하면 `Rationale` 또는 `Change Notes`도 갱신한다.

---

## 17. Tags 규칙

Tags는 검색과 분류를 돕는 보조 속성이다.

예:

```md
| Tags | auth, session, security |
```

규칙:

1. comma-separated 값을 사용한다.
2. lowercase kebab-case를 권장한다.
3. tag 앞뒤 공백은 trim한다.
4. tag는 상태나 target을 대체하지 않는다.
5. tag는 5개 이하를 권장한다.
6. 중복 tag를 사용하지 않는다.
7. 같은 의미의 tag는 하나로 통일한다. 예: `login`과 `sign-in` 중 하나 선택.

---

## 18. Requirement Section 규칙

### 18.1 고정 section 목록

Requirement Block 내부 section은 다음 heading을 사용한다.

| Section | Required | Purpose |
|---|---:|---|
| `#### Requirement` | yes | 요구사항 statement |
| `#### Rationale` | recommended | 요구사항이 필요한 이유 |
| `#### Acceptance Criteria` | yes | 검증 가능한 수용 기준 |
| `#### Verification Evidence` | required for `implemented`, `verified` | 구현 또는 검증 증거 |
| `#### Trace Links` | recommended | 이슈, PR, 코드, 문서, 다른 요구사항 관계 |
| `#### Research / Analysis` | optional | 조사, 비교, 분석, 실험 근거 |
| `#### Implementation Notes` | optional | 구현자가 참고할 세부 사항 |
| `#### Change Notes` | optional | 요구사항 변경 이력 요약 |

### 18.2 section heading 규칙

1. section heading은 반드시 `####`를 사용한다.
2. section heading 이름을 바꾸지 않는다.
3. 같은 Requirement Block 안에서 같은 section을 중복 작성하지 않는다.
4. 필수 section이 비어 있으면 validation error 또는 warning이 발생한다.

---

## 19. Requirement Statement 작성 규칙

### 19.1 기본 문장 패턴

한국어 문서에서는 다음 패턴을 권장한다.

```text
시스템은 {조건/상황}에서 {행위/결과/제약}을/를 {해야 한다/제공해야 한다/보장해야 한다}.
```

영어 문서에서는 다음 패턴을 권장한다.

```text
The system shall {behavior/result/constraint} when {condition/context}.
```

### 19.2 좋은 예

```md
#### Requirement

시스템은 access token이 만료된 요청에 대해 HTTP 401 응답을 반환해야 한다.
```

좋은 이유:

1. 시스템 행위가 명확하다.
2. 조건이 명확하다.
3. 기대 결과가 명확하다.
4. 테스트 가능하다.

### 19.3 나쁜 예

```md
#### Requirement

시스템은 토큰을 적절히 처리해야 한다.
```

나쁜 이유:

1. “적절히”의 의미가 불명확하다.
2. 어떤 토큰인지 불명확하다.
3. 어떤 결과를 기대하는지 불명확하다.
4. 검증 기준을 만들기 어렵다.

### 19.4 금지 또는 경고 표현

다음 표현은 가능하면 쓰지 않는다.

| Expression | Reason |
|---|---|
| 적절히 | 판단 기준 불명확 |
| 충분히 | 측정 기준 없음 |
| 빠르게 | 성능 기준 없음 |
| 안전하게 | 보안 기준 없음 |
| 사용자 친화적으로 | UX 기준 없음 |
| 대부분의 경우 | 조건 범위 불명확 |
| 가능하면 | 필수 여부 불명확 |
| 필요하면 | 조건 불명확 |
| 나중에 | target 불명확 |
| 일단 | 임시 요구인지 불명확 |

이 표현이 필요하면 Acceptance Criteria 또는 Measurement Criteria에서 기준을 구체화한다.

---

## 20. 좋은 요구사항 품질 기준

요구사항은 다음 기준을 만족해야 한다.

| Quality | Rule |
|---|---|
| Atomic | 하나의 요구사항은 하나의 검증 가능한 요구만 담는다. |
| Verifiable | 테스트, 분석, 검사, 시연, 리뷰 중 하나 이상으로 확인 가능하다. |
| Unambiguous | 서로 다른 해석이 나오지 않는다. |
| Necessary | 실제 시스템 목표, 품질, 제약, 사용자 가치, 운영 필요와 연결된다. |
| Feasible | 현재 기술, 일정, 환경에서 구현 가능하다. |
| Bounded | 적용 범위와 제외 범위가 명확하다. |
| Consistent | 다른 요구사항과 충돌하지 않는다. |
| Traceable | issue, PR, test, code, docs, 다른 requirement와 연결 가능하다. |
| Reviewable | reviewer가 변경 범위와 검증 기준을 빠르게 판단할 수 있다. |
| Maintainable | Git diff에서 최소 변경으로 추적 가능하다. |

---

## 21. Acceptance Criteria 규칙

### 21.1 기본 형식

Acceptance Criteria는 반드시 task list로 작성한다.

```md
#### Acceptance Criteria

- [ ] AC-1: 만료된 access token으로 `GET /api/me`를 호출하면 HTTP 401을 반환한다.
- [ ] AC-2: 유효한 access token으로 `GET /api/me`를 호출하면 HTTP 200과 사용자 요약 정보를 반환한다.
- [ ] AC-3: 만료된 token으로 재요청해도 세션이 복구되지 않는다.
```

### 21.2 AC ID 규칙

1. 각 항목은 `AC-{N}:` 형식을 권장한다.
2. 번호는 Requirement Block 안에서 1부터 증가한다.
3. 중간에 항목을 삭제하면 번호를 재정렬해도 된다.
4. Verification Evidence의 `Covers` column에서 AC ID를 참조할 수 있다.

### 21.3 작성 규칙

1. 최소 1개 이상 작성한다.
2. 하나의 AC는 하나의 기대 결과만 표현한다.
3. 가능하면 조건, 입력, 기대 결과를 포함한다.
4. 비기능 요구사항은 측정 가능한 기준을 포함한다.
5. `verified` 상태에서는 모든 AC가 `- [x]`여야 한다.
6. AC를 삭제해서 `verified` 조건을 맞추면 안 된다. 기준이 바뀐 경우 `Change Notes`에 이유를 남긴다.

### 21.4 기능 요구사항 AC 예

```md
- [ ] AC-1: 비로그인 사용자가 보호된 API를 호출하면 HTTP 401을 반환한다.
- [ ] AC-2: 로그인 사용자가 동일 API를 호출하면 HTTP 200을 반환한다.
- [ ] AC-3: 401 응답에는 인증 실패 사유를 노출하지 않는다.
```

### 21.5 성능 요구사항 AC 예

```md
- [ ] AC-1: p95 응답 시간이 300ms 이하임을 부하 테스트로 확인한다.
- [ ] AC-2: 동시 사용자 500명 조건에서 오류율이 1% 이하임을 확인한다.
```

### 21.6 보안 요구사항 AC 예

```md
- [ ] AC-1: refresh token은 평문으로 로그에 기록되지 않는다.
- [ ] AC-2: refresh token 저장소에는 hash 또는 암호화된 값만 저장된다.
- [ ] AC-3: token rotation 실패 시 기존 token은 재사용 불가 상태가 된다.
```

---

## 22. Verification Evidence 규칙

### 22.1 기본 형식

```md
#### Verification Evidence

| Evidence ID | Type | Reference | Covers | Notes |
|---|---|---|---|---|
| VE-1 | Test | `src/test/AuthSessionExpirationTest.java` | AC-1, AC-2 | 세션 만료 API 테스트 |
| VE-2 | PR | https://github.com/org/repo/pull/456 | all | 구현 및 테스트 PR |
```

### 22.2 Evidence Type

| Type | Meaning |
|---|---|
| `Test` | 테스트 파일, 테스트 케이스, 테스트 실행 결과 |
| `PR` | 구현 또는 검증 Pull Request |
| `Issue` | 요구사항 추적 issue |
| `Code` | 핵심 구현 파일 또는 코드 위치 |
| `Review` | 리뷰 결과, 승인, 체크리스트 |
| `Analysis` | 분석 문서, 실험 결과, benchmark |
| `Demo` | 데모 영상, 스크린샷, 시연 기록 |
| `Monitoring` | 운영 로그, 메트릭, alert, dashboard |
| `Manual` | 수동 검증 기록 |

### 22.3 Evidence ID 규칙

1. Evidence ID는 `VE-{N}` 형식을 권장한다.
2. 번호는 Requirement Block 안에서 1부터 증가한다.
3. `Covers`에는 `AC-1`, `AC-2`, `all`, 또는 `-`를 사용한다.
4. `verified` 상태에서는 `Covers = all` 또는 모든 AC가 evidence에 의해 커버되는 것을 권장한다.

### 22.4 Evidence Reference 규칙

1. local file은 inline code path로 작성한다. 예: `` `src/auth/session.ts` ``.
2. GitHub Issue 또는 PR은 URL로 작성한다.
3. 문서는 Markdown link로 작성한다.
4. 존재하지 않는 파일 경로를 작성하지 않는다.
5. 허위 URL을 작성하지 않는다.
6. 증거가 아직 없으면 상태를 `verified`로 바꾸지 않는다.

---

## 23. Trace Links 규칙

Trace Links는 요구사항과 외부 산출물 또는 다른 요구사항의 관계를 기록한다.

### 23.1 기본 형식

```md
#### Trace Links

| Type | Reference | Relation | Notes |
|---|---|---|---|
| Issue | https://github.com/org/repo/issues/123 | tracks | 세션 만료 요구사항 추적 |
| PR | https://github.com/org/repo/pull/456 | implements | 구현 PR |
| Requirement | IR-FE-ROUTER-001 | depends_on | 프론트엔드 라우팅 처리 의존 |
| Doc | [세션 정책 분석](../analysis/session-timeout-analysis.md) | informed_by | 정책 결정 근거 |
```

### 23.2 Trace Type

| Type | Meaning |
|---|---|
| `Issue` | GitHub Issue, Linear ticket, Jira ticket 등 |
| `PR` | Pull Request |
| `Commit` | Git commit |
| `Code` | source file 또는 코드 위치 |
| `Test` | test file 또는 test result |
| `Doc` | 분석 문서, 기술 문서, 운영 문서 |
| `ADR` | architecture decision record |
| `Requirement` | 다른 요구사항 ID |
| `External` | 외부 표준, 외부 문서, vendor 문서 |

### 23.3 Relation 값

| Relation | Meaning |
|---|---|
| `tracks` | issue 또는 ticket이 요구사항을 추적한다. |
| `implements` | PR, commit, code가 요구사항을 구현한다. |
| `verifies` | test, review, analysis, demo가 요구사항을 검증한다. |
| `depends_on` | 이 요구사항이 다른 요구사항에 의존한다. |
| `blocks` | 이 요구사항이 다른 요구사항을 차단한다. |
| `conflicts_with` | 다른 요구사항과 충돌한다. |
| `refines` | 다른 요구사항을 더 구체화한다. |
| `generalizes` | 다른 요구사항을 더 일반화한다. |
| `supersedes` | 기존 요구사항을 대체한다. |
| `superseded_by` | 다른 요구사항에 의해 대체된다. |
| `informed_by` | 문서, 분석, 외부 근거에서 영향을 받았다. |
| `related_to` | 일반 관련 관계. |

### 23.4 관계 규칙

1. `Requirement` reference는 실제 존재하는 Requirement ID여야 한다.
2. 자기 자신을 참조하지 않는다.
3. `depends_on` 관계가 cycle을 만들면 warning 이상으로 처리한다.
4. `conflicts_with` 관계는 `Open Questions` 또는 `Change Notes`에 해소 방안을 남긴다.
5. `supersedes` 또는 `superseded_by`를 사용하면 metadata의 `Supersedes` 또는 `Superseded By`도 갱신하는 것을 권장한다.

---

## 24. Research / Analysis 규칙

### 24.1 목적

Research / Analysis는 요구사항을 뒷받침하는 조사, 실험, 비교, 기술 분석, 의사결정 근거를 연결한다.

### 24.2 기본 형식

```md
#### Research / Analysis

- [세션 만료 정책 분석](../analysis/session-timeout-analysis.md)
- [JWT와 opaque token 비교](../analysis/token-strategy-comparison.md)
- [토큰 저장 방식 ADR](../adr/0001-use-session-cookie.md)
```

### 24.3 규칙

1. Research / Analysis는 필수는 아니다.
2. `Risk = high`, `Risk = critical`, 또는 `Stability = draft`이면 작성하는 것을 권장한다.
3. 외부 시스템, vendor API, 보안 정책, 성능 수치에 의존하면 근거 문서를 연결한다.
4. 분석 문서의 결론이 바뀌면 연결된 요구사항을 재검토한다.
5. 연구 내용 자체를 Requirement Statement에 길게 넣지 않는다.

---

## 25. Implementation Notes 규칙

Implementation Notes는 구현자가 참고할 세부 사항이다. 이 섹션은 요구사항 자체가 아니다.

```md
#### Implementation Notes

- 백엔드 middleware에서 token 만료를 감지한다.
- 프론트엔드 redirect 처리는 `IR-FE-ROUTER-001`에서 다룬다.
- 테스트는 만료 token fixture를 사용한다.
```

규칙:

1. Requirement Statement에 들어갈 필수 동작을 Implementation Notes에 숨기지 않는다.
2. 구현 방식이 강제 조건이면 별도 `constraint` requirement로 분리한다.
3. 구현 상세가 바뀌어도 요구사항의 의미가 바뀌지 않아야 한다.
4. 구현 note는 과도하게 길어지면 `docs/tech/*.md`로 분리한다.

---

## 26. Change Notes 규칙

Change Notes는 요구사항 변경 이유와 영향을 간단히 기록한다. 상세 변경 이력은 Git이 담당한다.

```md
#### Change Notes

| Date | Change | Reason |
|---|---|---|
| 2026-05-07 | Target을 `v0.1`에서 `v0.2`로 변경 | refresh token 정책 확정 지연 |
| 2026-05-10 | AC-3 추가 | 재요청 시 세션 복구 방지 필요 |
```

규칙:

1. 모든 사소한 문구 수정을 기록할 필요는 없다.
2. Target, Status, Acceptance Criteria, Requirement Statement의 의미가 바뀌면 기록한다.
3. `frozen` target의 요구사항을 수정하면 Change Notes를 작성한다.
4. 변경 이유는 구체적으로 작성한다.

---

## 27. 완전한 Requirement Block 예시

```md
### FR-AUTH-001 — 세션 만료 시 재로그인 요구

| Field | Value |
|---|---|
| Type | functional |
| Target | v0.1 |
| Status | in_progress |
| Priority | high |
| Tags | auth, session, security |
| Risk | medium |
| Stability | stable |
| Verification Method | test, inspection |
| GitHub Issue | https://github.com/org/repo/issues/123 |
| Related Docs | [세션 정책 분석](../analysis/session-timeout-analysis.md), [토큰 설계](../tech/auth-token-design.md) |

#### Requirement

시스템은 access token이 만료된 요청에 대해 HTTP 401 응답을 반환해야 한다.

#### Rationale

만료된 인증 상태로 보호된 API 접근이 계속 허용되면 보안 및 사용자 상태 일관성 문제가 발생한다.

#### Acceptance Criteria

- [ ] AC-1: 만료된 access token으로 `GET /api/me`를 호출하면 HTTP 401을 반환한다.
- [ ] AC-2: HTTP 401 응답은 refresh token 값을 노출하지 않는다.
- [ ] AC-3: 만료된 access token으로 재요청해도 세션이 복구되지 않는다.

#### Verification Evidence

| Evidence ID | Type | Reference | Covers | Notes |
|---|---|---|---|---|
| VE-1 | Test | `src/test/AuthSessionExpirationTest.java` | AC-1, AC-3 | 세션 만료 API 테스트 |
| VE-2 | Review | https://github.com/org/repo/pull/456 | AC-2 | 보안 응답 필드 리뷰 |

#### Trace Links

| Type | Reference | Relation | Notes |
|---|---|---|---|
| Issue | https://github.com/org/repo/issues/123 | tracks | 세션 만료 요구사항 |
| PR | https://github.com/org/repo/pull/456 | implements | 구현 PR |
| Doc | [세션 정책 분석](../analysis/session-timeout-analysis.md) | informed_by | 정책 결정 근거 |

#### Research / Analysis

- [세션 정책 분석](../analysis/session-timeout-analysis.md)
- [토큰 설계](../tech/auth-token-design.md)

#### Implementation Notes

- 인증 middleware에서 access token 만료를 판단한다.
- refresh token 재발급 흐름은 별도 요구사항에서 다룬다.

#### Change Notes

| Date | Change | Reason |
|---|---|---|
| 2026-05-07 | 최초 작성 | 인증 scope v0.1 정의 |
```

---

## 28. 비기능 요구사항 작성 규칙

비기능 요구사항은 가능한 한 측정 가능한 기준을 포함한다.

### 28.1 Performance

```md
### PERF-SEARCH-001 — 검색 응답 시간 기준

| Field | Value |
|---|---|
| Type | performance |
| Target | v0.2 |
| Status | planned |
| Priority | high |
| Tags | search, performance |
| Verification Method | test, analysis |

#### Requirement

시스템은 10,000개 문서 인덱스에서 일반 검색 요청의 p95 응답 시간을 500ms 이하로 유지해야 한다.

#### Rationale

검색 응답이 느리면 구현자가 요구사항을 탐색하는 흐름이 끊긴다.

#### Acceptance Criteria

- [ ] AC-1: 10,000개 문서 fixture를 사용한 benchmark에서 p95 응답 시간이 500ms 이하이다.
- [ ] AC-2: benchmark 결과가 `docs/analysis/search-benchmark.md`에 기록된다.
```

### 28.2 Security

```md
### SEC-AUTH-001 — 인증 토큰 로그 노출 금지

| Field | Value |
|---|---|
| Type | security |
| Target | v0.1 |
| Status | planned |
| Priority | critical |
| Tags | auth, token, logging |
| Verification Method | test, inspection |

#### Requirement

시스템은 access token과 refresh token 원문을 application log에 기록하지 않아야 한다.

#### Acceptance Criteria

- [ ] AC-1: 인증 실패 로그에 token 원문이 포함되지 않는다.
- [ ] AC-2: debug log에도 token 원문이 포함되지 않는다.
- [ ] AC-3: 관련 logging test가 존재한다.
```

### 28.3 Observability

```md
### OBS-AUTH-001 — 인증 실패 메트릭 기록

| Field | Value |
|---|---|
| Type | observability |
| Target | v0.1 |
| Status | planned |
| Priority | medium |
| Tags | auth, metrics |
| Verification Method | test, monitoring |

#### Requirement

시스템은 인증 실패 이벤트를 원인별 metric으로 기록해야 한다.

#### Acceptance Criteria

- [ ] AC-1: 만료 token 실패는 `auth_failure_total{reason="expired"}`로 기록된다.
- [ ] AC-2: 잘못된 서명 실패는 `auth_failure_total{reason="invalid_signature"}`로 기록된다.
- [ ] AC-3: metric label에는 token 원문 또는 사용자 비밀 정보가 포함되지 않는다.
```

---

## 29. Requirement 분리와 병합 규칙

### 29.1 분리해야 하는 경우

다음 경우에는 요구사항을 분리한다.

1. 서로 다른 actor가 관련된다.
2. 서로 다른 시스템 boundary가 관련된다.
3. 서로 다른 verification method가 필요하다.
4. 기능 동작과 성능 기준이 섞여 있다.
5. 하나의 AC 목록이 너무 길어진다.
6. 서로 다른 target에 배치되어야 한다.
7. 한 부분은 구현됐고 다른 부분은 차단된 상태다.

### 29.2 병합할 수 있는 경우

다음 경우에는 병합할 수 있다.

1. 두 요구사항이 사실상 동일한 동작을 요구한다.
2. 서로 분리되어 있어도 검증 기준이 완전히 같다.
3. 하나가 다른 하나의 단순 반복이다.
4. 유지하면 중복 구현 또는 충돌을 유발한다.

### 29.3 병합 처리

대표 요구사항을 유지하고 제거되는 요구사항은 `discarded` 상태로 둔다.

```md
| Status | discarded |
| Superseded By | FR-AUTH-001 |
```

대표 요구사항의 Trace Links에는 다음을 추가한다.

```md
| Requirement | FR-AUTH-009 | supersedes | 중복 요구사항 병합 |
```

---

## 30. 삭제와 폐기 규칙

요구사항 삭제는 기본적으로 물리 삭제가 아니라 `discarded` 상태 변경으로 처리한다.

물리 삭제가 가능한 경우:

1. 요구사항이 잘못 추가되었다.
2. 관련 issue, PR, test, code, evidence가 없다.
3. 다른 요구사항이 참조하지 않는다.
4. Git history 외에 별도 추적 가치가 없다.

그 외에는 다음처럼 폐기한다.

```md
| Status | discarded |
```

폐기 이유는 `Change Notes`에 남긴다.

```md
#### Change Notes

| Date | Change | Reason |
|---|---|---|
| 2026-05-07 | Status를 `discarded`로 변경 | `FR-AUTH-001`에 병합됨 |
```

---

## 31. 스크립트 파싱 계약

이 섹션은 repo-local script 또는 AI agent가 SRS 문서를 해석할 때 따라야 하는 계약이다.

### 31.1 파서가 인식해야 하는 파일

기본 glob:

```text
docs/spec/**/*.srs.md
```

인덱스 문서:

```text
docs/spec/00.index.md
```

### 31.2 파서가 추출해야 하는 최소 필드

| Field | Source |
|---|---|
| `id` | requirement heading |
| `title` | requirement heading |
| `file` | file path |
| `type` | metadata table |
| `target` | metadata table |
| `status` | metadata table |
| `priority` | metadata table |
| `tags` | metadata table |
| `githubIssue` | metadata table |
| `relatedDocs` | metadata table |
| `requirement` | `#### Requirement` section |
| `acceptanceCriteria` | task list under `#### Acceptance Criteria` |
| `verificationEvidence` | table under `#### Verification Evidence` |
| `traceLinks` | table under `#### Trace Links` |

### 31.3 파싱 단계

1. `docs/spec/00.index.md`에서 index metadata, Target Map, Scope Map을 읽는다.
2. `docs/spec/**/*.srs.md` 파일 목록을 찾는다.
3. 각 파일에서 Requirement heading을 찾는다.
4. heading부터 다음 Requirement heading 전까지를 block으로 추출한다.
5. block의 첫 table을 metadata table로 읽는다.
6. `####` section heading별 본문을 추출한다.
7. Acceptance Criteria task list를 읽는다.
8. Verification Evidence와 Trace Links table을 읽는다.
9. validation rule을 적용한다.

v1.2.0 hardening target에서는 parser가 fenced code block 내부 heading-like text를 무시하고, Requirement Block boundary를 다음 valid requirement heading, 다음 관련 top-level `##` section, 또는 파일 끝으로 제한한다. 같은 Requirement Block 안의 중복 section heading, nested Acceptance Criteria, forbidden heading content, malformed table row도 diagnostic으로 보고되어야 한다.

### 31.4 파서가 하지 말아야 할 일

1. SRS 문서 전체를 자동 재정렬하지 않는다.
2. formatting을 임의로 바꾸지 않는다.
3. section heading 이름을 자동 번역하지 않는다.
4. Markdown 의미를 추론해서 숨은 상태를 만들지 않는다.
5. Requirement Statement를 임의로 요약해서 원문을 대체하지 않는다.
6. 존재하지 않는 링크나 evidence를 생성하지 않는다.

---

## 32. 검증 스크립트 명세

Repo-local script는 필수가 아니지만, 저장소에서 제공하면 다음 동작을 권장한다.

### 32.1 validate-spec

```bash
node scripts/spec/validate-spec.js
```

검증 항목:

| Code | Severity | Title |
|---|---|---|
| `SRS-E001` | error | Malformed requirement heading |
| `SRS-E002` | error | Duplicate requirement ID |
| `SRS-E003` | error | Required metadata field missing |
| `SRS-E004` | error | Type does not match requirement ID prefix |
| `SRS-E005` | error | Invalid requirement status |
| `SRS-E006` | error | Invalid requirement priority |
| `SRS-E007` | error | Invalid requirement risk |
| `SRS-E008` | error | Acceptance Criteria section missing |
| `SRS-E009` | error | Verified requirement has unchecked acceptance criteria |
| `SRS-E010` | error | Verified requirement lacks checked AC or evidence |
| `SRS-E011` | error | Invalid requirement stability |
| `SRS-E012` | error | Trace requirement reference missing |
| `SRS-E013` | error | Target Map table missing |
| `SRS-E014` | error | Scope Map table missing |
| `SRS-E015` | error | Scope prefix is not registered |
| `SRS-E016` | error | Scope document is missing |
| `SRS-E017` | error | Active Target is not registered |
| `SRS-E018` | error | Duplicate requirement section |
| `SRS-E019` | error | Nested acceptance criterion |
| `SRS-E020` | error | Forbidden requirement heading content |
| `SRS-E021` | error | Malformed metadata table row |
| `SRS-E022` | error | Duplicate Target Map target |
| `SRS-E023` | error | Duplicate Scope Map prefix |
| `SRS-E024` | error | Multiple active targets |
| `SRS-E025` | error | Scope document file missing |
| `SRS-E026` | error | Release target is empty |
| `SRS-E027` | error | Acceptance Criteria coverage gap |
| `SRS-E028` | error | Evidence reference missing |
| `SRS-E029` | error | Evidence URL invalid |
| `SRS-E030` | error | Command evidence violates policy |
| `SRS-E031` | error | Trace link target is broken |
| `SRS-E032` | error | Stale mutation snapshot |
| `SRS-E033` | error | Verified draft requirement |
| `SRS-W001` | warning | Rationale section missing |
| `SRS-W002` | warning | Target is not registered |
| `SRS-W003` | warning | Related Docs local link missing |
| `SRS-W004` | warning | GitHub Issue URL format invalid |
| `SRS-W005` | warning | Heading dash is not an em dash |
| `SRS-W006` | warning | Discouraged wording used |
| `SRS-W007` | warning | Too many tags |
| `SRS-W008` | warning | High risk requirement lacks Research / Analysis |
| `SRS-W009` | warning | Frozen target changed without Change Notes |
| `SRS-W010` | warning | Active Target row is not active |
| `SRS-W011` | warning | Completed Work Log date is invalid |
| `SRS-W012` | warning | Completed Work Log target is not registered |
| `SRS-W013` | warning | Completed Work Log scope is not registered |
| `SRS-W014` | warning | Completed Work Log requirement is missing |
| `SRS-W015` | warning | Completed Work Log requirement is not completed |
| `SRS-W016` | warning | Malformed Verification Evidence table row |
| `SRS-W017` | warning | Malformed Trace Links table row |
| `SRS-W018` | warning | Unregistered scope SRS document |
| `SRS-W019` | warning | Status Summary drift |
| `SRS-W020` | warning | Requirement Type Summary drift |
| `SRS-W021` | warning | Release readiness warning |
| `SRS-W022` | warning | Legacy volatile stability |
| `SRS-W023` | warning | Draft requirement in active or released target |
| `SRS-W024` | warning | Malformed Completed Work Log report path |
| `SRS-W040` | warning | Target Goal block conflict between index and appendix |

v1.2.0 hardening target에서는 위 diagnostic code table을 code-level diagnostic registry와 contract-tested 또는 generated relationship으로 맞춘다. Registry entry는 code, severity, title, message template, source rule, since 값을 포함해야 하며, 구현에서 emit하는 모든 diagnostic code는 registry에 등록되어야 한다.

v1.2.0 hardening target에서는 index consistency diagnostic도 확장한다. Duplicate Target Map target, duplicate Scope Map prefix, multiple active target rows, missing scope document file, unregistered `.srs.md` file, Status Summary drift, Requirement Type Summary drift는 diagnostic으로 보고되어야 한다.

### 32.2 list-by-target

```bash
node scripts/spec/list-by-target.js v0.1
```

출력 권장:

```text
Target: v0.1

planned
- FR-AUTH-001 세션 만료 시 재로그인 요구

in_progress
- SEC-AUTH-001 인증 토큰 로그 노출 금지

verified
- OBS-AUTH-001 인증 실패 메트릭 기록
```

JSON 출력 권장:

```bash
node scripts/spec/list-by-target.js v0.1 --json
```

### 32.3 list-by-status

```bash
node scripts/spec/list-by-status.js in_progress
```

### 32.4 update-status

```bash
node scripts/spec/update-status.js FR-AUTH-001 implemented
```

규칙:

1. Requirement ID로 block을 찾는다.
2. metadata table의 `Status` row만 수정한다.
3. `verified` 전환 시 Acceptance Criteria와 Verification Evidence를 검증한다.
4. 실패 시 문서를 변경하지 않는다.
5. 문서 전체 formatting을 바꾸지 않는다.

### 32.5 summarize-target

```bash
node scripts/spec/summarize-target.js v0.1
```

출력 항목:

1. target별 total count
2. status별 count
3. type별 count
4. blocked 요구사항 목록
5. implemented but not verified 목록
6. verified 목록
7. missing evidence 목록

### 32.6 extract-requirements

```bash
node scripts/spec/extract-requirements.js --json
```

기계 처리용 JSON을 출력한다. 이 출력은 원본이 아니라 파생 결과다.

---

## 33. AI Coding Agent 작업 규칙

### 33.1 구현 전

AI coding agent는 구현 전에 다음을 수행한다.

1. `docs/spec/00.index.md`를 확인한다.
2. 작업과 관련된 Scope SRS 문서를 찾는다.
3. 관련 Requirement Block을 읽는다.
4. `Requirement`, `Rationale`, `Acceptance Criteria`, `Trace Links`, `Implementation Notes`를 확인한다.
5. 여러 요구사항이 관련되면 target과 status를 기준으로 구현 순서를 정한다.

### 33.2 구현 중

1. Acceptance Criteria를 구현 기준으로 사용한다.
2. 명세에 없는 큰 동작을 추가하지 않는다.
3. 요구사항이 모호하면 Requirement Block에 `Open Question` 또는 `Change Notes`를 추가하는 방향을 우선 고려한다.
4. 구현상 발견한 제약이 요구사항 의미를 바꾸면 SRS를 함께 갱신한다.

### 33.3 구현 후

1. 관련 test, PR, code path를 Verification Evidence에 추가한다.
2. 충족된 Acceptance Criteria를 `- [x]`로 갱신한다.
3. 코드 구현은 되었지만 검증 증거가 부족하면 `implemented`로 둔다.
4. 모든 AC가 충족되고 evidence가 있으면 `verified`로 변경할 수 있다.
5. 변경 이유가 중요하면 `Change Notes`에 남긴다.

### 33.4 금지 행동

AI coding agent는 다음을 하지 않는다.

1. SRS 문서 전체를 임의로 재작성하지 않는다.
2. Requirement ID를 재사용하지 않는다.
3. Acceptance Criteria를 삭제해서 `verified` 조건을 맞추지 않는다.
4. 허위 issue, PR, test, code path, evidence를 만들지 않는다.
5. 존재하지 않는 local file link를 작성하지 않는다.
6. heading, metadata table, section heading 이름을 임의로 바꾸지 않는다.
7. YAML front matter 또는 raw HTML을 추가하지 않는다.
8. `done` 같은 비허용 status를 사용하지 않는다.

---

## 34. Review 규칙

SRS 변경을 review할 때 다음을 확인한다.

| Check | Question |
|---|---|
| ID | ID가 전역 유일한가? |
| Type | ID prefix와 Type이 일치하는가? |
| Scope | 올바른 scope 문서에 있는가? |
| Target | Target Map에 등록된 target인가? |
| Status | 실제 구현/검증 상태와 일치하는가? |
| Statement | 단일 요구사항이며 모호하지 않은가? |
| AC | 검증 가능한 수용 기준인가? |
| Evidence | status에 맞는 증거가 있는가? |
| Trace | 관련 issue, PR, test, docs가 연결되었는가? |
| Diff | 불필요한 대량 formatting 변경이 없는가? |
| Conflict | 다른 요구사항과 충돌하지 않는가? |

---

## 35. Baseline과 Release 운영

### 35.1 Baseline

Baseline은 특정 시점의 SRS 상태를 의미한다. Git tag 또는 release branch로 관리하는 것을 권장한다.

예:

```bash
git tag srs-v0.1-baseline
```

### 35.2 Target Freeze

Target이 `frozen`이면 다음 규칙을 적용한다.

1. 새 요구사항 추가는 review가 필요하다.
2. Acceptance Criteria 변경은 Change Notes가 필요하다.
3. Status 변경은 허용하되 evidence 조건을 지켜야 한다.
4. Target 이동은 reviewer 확인을 권장한다.

### 35.3 Release 확인

특정 target을 release 후보로 판단하려면 다음을 확인한다.

1. `blocked` 요구사항이 없는가?
2. `planned` 또는 `in_progress` 요구사항이 release scope에 남아 있는가?
3. `implemented` 상태 요구사항 중 검증이 필요한 것이 있는가?
4. `verified` 요구사항의 evidence가 유효한가?
5. critical/high priority 요구사항이 모두 처리되었는가?

---

## 36. Anti-patterns

### 36.1 거대한 요구사항

나쁜 예:

```md
### FR-AUTH-001 — 인증 시스템 구현
```

문제:

1. 너무 넓다.
2. AC가 과도하게 많아진다.
3. 일부 구현 완료와 일부 미완료를 구분하기 어렵다.

개선:

```md
### FR-AUTH-001 — 로그인 성공 시 access token 발급
### FR-AUTH-002 — 세션 만료 시 재로그인 요구
### FR-AUTH-003 — refresh token 회전 처리
```

### 36.2 측정 불가능한 품질 요구사항

나쁜 예:

```md
시스템은 빠르게 응답해야 한다.
```

개선:

```md
시스템은 10,000개 문서 인덱스에서 일반 검색 요청의 p95 응답 시간을 500ms 이하로 유지해야 한다.
```

### 36.3 검증 없는 완료 처리

나쁜 예:

```md
| Status | verified |

#### Acceptance Criteria

- [ ] AC-1: 로그인 실패 시 HTTP 401을 반환한다.
```

문제:

1. AC가 체크되지 않았다.
2. evidence가 없다.
3. verified 조건을 만족하지 않는다.

### 36.4 구현 note에 요구사항 숨기기

나쁜 예:

```md
#### Requirement

시스템은 로그인 기능을 제공해야 한다.

#### Implementation Notes

- 실패 시 401을 반환해야 한다.
- token은 30분 후 만료되어야 한다.
```

개선:

1. 401 응답 요구사항을 별도 requirement로 분리한다.
2. token 만료 정책을 별도 security 또는 functional requirement로 분리한다.

---

## 37. 최소 템플릿

새 요구사항을 추가할 때 다음 템플릿을 사용한다.

```md
### FR-SCOPE-001 — 요구사항 제목

| Field | Value |
|---|---|
| Type | functional |
| Target | v0.1 |
| Status | planned |
| Priority | medium |
| Tags | example |
| Risk | medium |
| Stability | evolving |
| Verification Method | test |
| GitHub Issue | - |
| Related Docs | - |

#### Requirement

시스템은 {조건}에서 {기대 동작}을 수행해야 한다.

#### Rationale

이 요구사항이 필요한 이유를 작성한다.

#### Acceptance Criteria

- [ ] AC-1: {조건}이면 {기대 결과}가 발생한다.

#### Verification Evidence

| Evidence ID | Type | Reference | Covers | Notes |
|---|---|---|---|---|

#### Trace Links

| Type | Reference | Relation | Notes |
|---|---|---|---|

#### Research / Analysis

- -

#### Implementation Notes

- -

#### Change Notes

| Date | Change | Reason |
|---|---|---|
```

---

## 38. 확장 ISO/IEC/IEEE 29148 매핑

이 규칙은 요구사항 공학 표준의 실무 목적을 Git-native Markdown에 맞게 다음과 같이 대응시킨다.

| Requirements Engineering Concern | SRS-MD Representation |
|---|---|
| Requirement identification | Requirement ID heading |
| Requirement statement | `#### Requirement` |
| Requirement attributes | metadata table |
| Scope context | Scope SRS document and Scope Overview |
| Requirement rationale | `#### Rationale` |
| Verification planning | `Verification Method` and `#### Acceptance Criteria` |
| Verification evidence | `#### Verification Evidence` |
| Traceability | `#### Trace Links` |
| Change management | Git history and `#### Change Notes` |
| Baseline management | Git tag, branch, release, target freeze |
| Stakeholder review | Pull Request review and Review evidence |
| Requirements quality | Section 20 quality criteria |
| Iterative refinement | Status, Stability, Change Notes |

---

## 39. Reference Basis

이 문서는 다음 공개 문서를 참고 기준으로 삼는다.

- ISO/IEC/IEEE 29148:2018, Systems and software engineering — Life cycle processes — Requirements engineering: https://www.iso.org/standard/72089.html
- IEEE/ISO/IEC 29148-2018 standard page: https://standards.ieee.org/ieee/29148/6937/
- GitHub Flavored Markdown Spec: https://github.github.com/gfm/
- GitHub Docs, Basic writing and formatting syntax: https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax
- GitHub Docs, Organizing information with tables: https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/organizing-information-with-tables

---

## 40. AGENTS.md / CLAUDE.md 추가 문장

아래 managed block을 저장소 루트의 `AGENTS.md`, `CLAUDE.md`, 또는 둘 다에 추가한다. 기존 block의 version이 다르거나 legacy unversioned heading이면 heading부터 suffix marker까지 현재 block으로 교체한다.

```md
# SpecKiwi SRS 워크플로 v1.3

This repository uses `docs/spec/` as the required source of truth for requirements.

Before making any code, test, CLI, MCP, or documentation change, agents MUST:
1. Read `docs/spec/00.index.md`.
2. Find the relevant Requirement ID in the scope SRS files.
3. Mention the Requirement ID in the work summary.
4. If no matching requirement exists, stop and ask whether to create/update an SRS requirement first.

Requirement metadata has two separate lifecycle fields:
- `Status` tracks implementation and verification progress.
- `Stability` tracks requirement maturity and change-control maturity.

Agents MUST stop before implementing a non-discarded requirement with `Stability=draft` or `Stability=deprecated` unless the user explicitly overrides that workflow.

TDD principle:
- Agents MUST follow TDD for behavior changes: write or update a failing automated test for the relevant Requirement ID before implementation, make the smallest change to pass, then refactor while keeping tests green.
- If no meaningful automated test can be written, agents MUST stop before implementation and explain the exception and alternative verification evidence.

Agents MUST NOT:
- Implement behavior that is not covered by an SRS requirement.
- Create an alternate requirements source outside `docs/spec/`.
- Change requirement IDs manually.
- Mark requirements as verified without evidence.

When SpecKiwi MCP tools are available, agents MUST use them for requirement lookup and safe SRS updates. If MCP is unavailable, use the `speckiwi` CLI.

Current work status workflow:
1. Read the active target with MCP `get_active_target`, or CLI `speckiwi active-target --json` if MCP is unavailable.
2. If `activeTarget` is empty, report that no active target is set and ask which target to use before making target-scoped changes.
3. Read `summary.countsByStatus`, `summary.countsByStability`, `summary.stabilityBlockers`, `summary.stabilityWarnings`, and `summary.newWorkCandidates` before selecting work.
4. Read open work with MCP `list_requirements` for `status=in_progress`, `status=blocked`, and `status=implemented`; CLI fallback is `speckiwi list --status <status> --json`.
5. Check missing verification evidence through `summary` or MCP `summarize_target` before saying work is complete.
6. Read recent completed work with MCP `list_completed_work`; CLI fallback is `speckiwi completed-work --json`.

Completed Work Log is a read-only summary for agents. Requirement Block status, Acceptance Criteria, Verification Evidence, and Change Notes remain the source of truth for completion.

<!-- /SpecKiwi SRS 워크플로 -->
```

---

## 41. 문서 종료

이 문서는 SRS-MD Authoring Rules v1.0.0의 기준 문서다. 저장소별 상황에 맞는 세부 enum, scope prefix, target naming은 `docs/spec/00.index.md`와 `docs/spec/90.appendix.md`에서 확장할 수 있다. 단, Requirement Block의 heading 형식, metadata table, status 의미, Acceptance Criteria, Verification Evidence, Trace Links 규칙은 호환성을 위해 유지해야 한다.
