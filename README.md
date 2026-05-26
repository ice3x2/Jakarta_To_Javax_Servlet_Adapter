# Jakarta to Javax Servlet Adapter

> **🇰🇷 한국어 문서로 바로가기 → [한국어 안내](#한국어-안내)**

---

## English

A bidirectional adapter that lets legacy `javax.servlet` (Servlet API 4.0.1)
applications run inside Web Application Servers that implement the
`jakarta.servlet` namespace (Servlet 5 / 6 / 6.1).

`HttpServletRequest`, `HttpServletResponse`, `ServletContext`, `HttpSession`,
`Cookie`, and the rest of the standard servlet surface can be converted in
either direction at the boundary, so an application written against `javax.servlet`
can be hosted on a Tomcat 10+ / Jetty 11+ runtime without changing application code.

> **Group / Coordinates**: `com.snoworca:jakarta-to-javax-servlet-adapter-*:3.0.0`

---

### Why this exists

The Jakarta EE 9 namespace migration (`javax.*` → `jakarta.*`) is the single
largest breaking change in the servlet API since its inception. Every container
released from 2021 onwards (Tomcat 10+, Jetty 11+, Undertow 2.3+, WildFly 27+)
ships only the `jakarta.servlet` namespace. Applications, libraries, and
frameworks that still compile against `javax.servlet-api:4.0.1` — including
many internal codebases, legacy SDKs, and unmaintained third-party libraries —
cannot be loaded by these containers without a source-level rewrite.

This adapter sits at the boundary between the container and the application,
converting the request/response/context types on demand. The application code
stays unchanged.

---

### Compatibility matrix

The project ships three runtime modules, each targeting a specific Jakarta
Servlet API generation. All three share the same `common` module and adapt the
same `javax.servlet-api:4.0.1` source surface.

| Module      | Jakarta Servlet API | javax Servlet API | Minimum JDK | Validated container |
|-------------|---------------------|-------------------|-------------|---------------------|
| `servlet5`  | 5.0.0               | 4.0.1             | Java 8      | Tomcat 10.0.x       |
| `servlet6`  | 6.0.0               | 4.0.1             | Java 11     | Tomcat 10.1.x       |
| `servlet61` | 6.1.0               | 4.0.1             | Java 17     | Tomcat 11.x         |

All three modules are built with `sourceCompatibility = 1.8` /
`targetCompatibility = 1.8` for the bytecode level; the **Minimum JDK**
column above reflects the runtime floor required by the corresponding
Jakarta Servlet API.

Cross-version dependencies between `servlet5`, `servlet6`, and `servlet61`
are forbidden by the `validateNoVersionModuleCrossDeps` Gradle gate
(`CON-MOD-001`) — pick exactly **one** version module per deployment.

---

### Module layout

```
jakarta-to-javax-servlet-adapter/
├── common/                  # Shared converters, support utilities
├── servlet5/                # Jakarta Servlet 5.0 ↔ Javax 4.0
├── servlet6/                # Jakarta Servlet 6.0 ↔ Javax 4.0
├── servlet61/               # Jakarta Servlet 6.1 ↔ Javax 4.0
└── bom/                     # Maven BOM (dependencyManagement alignment)
```

Each version module contains:

- `adapter.jakarta.servletN.*` — wraps a `jakarta.servlet.*` instance, exposes
  it through the same shape so the rest of the adapter can treat all three
  servlet generations uniformly.
- `adapter.javax.servletN.*` — wraps a `javax.servlet.*` instance, exposes it
  to code that requires the legacy `javax.servlet` surface.
- `adapter.servletElementConverterN.*` — central converters that decide which
  wrapper to install given a runtime instance, including the `AdapterUnwrap`
  shortcut that avoids double wrapping when an already-wrapped object passes
  through the boundary a second time.
- `com.snoworca.ServletAdapterN` — convenience static facade (14 methods,
  delegates to `common.ServletAdapter`).

---

### Installation

#### Gradle (pick the module that matches your container)

```groovy
dependencies {
    // Choose ONE of the three version modules
    implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet5:3.0.0'   // Tomcat 10.0.x
    // implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet6:3.0.0'  // Tomcat 10.1.x
    // implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet61:3.0.0' // Tomcat 11.x

    // The legacy javax Servlet API is required at compile time
    // (provided by the adapter at runtime)
    compileOnly 'javax.servlet:javax.servlet-api:4.0.1'
}
```

#### Gradle (with BOM for multi-module alignment)

```groovy
dependencies {
    implementation platform('com.snoworca:jakarta-to-javax-servlet-adapter-bom:3.0.0')
    implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet6'
    compileOnly    'javax.servlet:javax.servlet-api:4.0.1'
}
```

#### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.snoworca</groupId>
      <artifactId>jakarta-to-javax-servlet-adapter-bom</artifactId>
      <version>3.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.snoworca</groupId>
    <artifactId>jakarta-to-javax-servlet-adapter-servlet6</artifactId>
  </dependency>
  <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

---

### Usage

#### jakarta → javax (primary direction)

This is the supported and recommended direction. The container hands your
filter/servlet a `jakarta.servlet.*` object; the adapter exposes the same
state as a `javax.servlet.*` view.

```java
import adapter.common.ServletAdapter;

public class ExampleServlet extends jakarta.servlet.http.HttpServlet {
    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest request,
                         jakarta.servlet.http.HttpServletResponse response)
            throws jakarta.servlet.ServletException, java.io.IOException {

        javax.servlet.http.HttpServletRequest  javaxRequest  = ServletAdapter.adaptToJavax(request);
        javax.servlet.http.HttpServletResponse javaxResponse = ServletAdapter.adaptToJavax(response);
        javax.servlet.ServletContext           javaxContext  = ServletAdapter.adaptToJavax(getServletContext());

        // Hand off to legacy javax.servlet code...
    }
}
```

#### javax → jakarta (reverse direction)

Supported but advisory. Useful when a legacy framework holds `javax.servlet.*`
references but the call target requires `jakarta.servlet.*`.

```java
public class ExampleServlet extends javax.servlet.http.HttpServlet {
    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request,
                         javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {

        jakarta.servlet.http.HttpServletRequest  jakartaRequest  = ServletAdapter.adaptToJakarta(request);
        jakarta.servlet.http.HttpServletResponse jakartaResponse = ServletAdapter.adaptToJakarta(response);
        jakarta.servlet.ServletContext           jakartaContext  = ServletAdapter.adaptToJakarta(getServletContext());
    }
}
```

#### Servlet filter pattern (typical adoption shape)

Wrap the request/response once at the filter boundary; everything downstream
sees the converted view.

```java
public class JakartaToJavaxFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest req,
                         jakarta.servlet.ServletResponse res,
                         jakarta.servlet.FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {

        javax.servlet.ServletRequest  javaxReq  = ServletAdapter.adaptToJavax(req);
        javax.servlet.ServletResponse javaxRes  = ServletAdapter.adaptToJavax(res);

        // Hand off to legacy javax-side application stack
        LegacyDispatcher.dispatch(javaxReq, javaxRes);

        chain.doFilter(req, res);
    }
}
```

---

### Behavior notes

The following items document deviations from a naive 1:1 wrapper. Read them
before adopting the adapter in production.

#### 1. Cookie attribute API — information loss policy

`jakarta.servlet.http.Cookie#setAttribute(String, String)` (added in Servlet
6.0) accepts arbitrary attribute names — including `SameSite`, custom
RFC 6265bis attributes, and forward-compatible flags. The
`javax.servlet.http.Cookie` class (Servlet 4.0.1) has **no equivalent API**.

Policy:

- When a `jakarta` cookie carrying `setAttribute(...)` values is adapted to
  `javax`, attributes that have **no matching first-class setter on
  `javax.servlet.http.Cookie`** are dropped silently and recorded in
  `ServletConverter` diagnostics rather than thrown.
- Attributes that *do* map (`Domain`, `Path`, `MaxAge`, `Secure`, `HttpOnly`,
  `Comment`, `Version`) are forwarded normally.
- The reverse direction (`javax` → `jakarta`) preserves only the canonical
  attributes — there is nothing to round-trip.

This is intentional and not a bug: a fully lossless adaptation would require
inventing a parallel attribute store on the `javax` view, which would diverge
from `javax.servlet-api:4.0.1` semantics.

#### 2. `HttpServletResponse#setStatus(int, String)` reason phrase — deprecated, no-op

The two-argument form `setStatus(int sc, String sm)` was deprecated in
Servlet 4.0 and removed from the wire in modern HTTP/2 stacks. When code
running on the `javax` side calls this method against an adapter view, the
reason phrase argument is **silently discarded** and only the status code is
forwarded to the underlying `jakarta` response (which exposes only
`setStatus(int)`).

This matches the behavior of every modern container (Tomcat 9+, Jetty 10+),
which already ignores the phrase on HTTP/2 connections.

#### 3. `HttpSession#getValue` / `putValue` / `removeValue` / `getValueNames` — automatic fallback to attribute API

The legacy `javax.servlet.http.HttpSession#getValue(String)` family
(deprecated since Servlet 2.2 and removed in Jakarta Servlet 6.0) is exposed
on the `javax` adapter view as **aliases** that delegate to the modern
attribute API:

| Legacy method               | Implemented as                                                |
|-----------------------------|---------------------------------------------------------------|
| `getValue(String name)`     | `getAttribute(name)`                                          |
| `putValue(String, Object)`  | `setAttribute(name, value)`                                   |
| `removeValue(String name)`  | `removeAttribute(name)`                                       |
| `getValueNames()`           | `Collections.list(getAttributeNames()).toArray(new String[0])`|

This lets unmodified pre-2002 servlet code continue to read and write session
state through the adapter even though the underlying Jakarta container no
longer exposes the deprecated methods. No `UnsupportedOperationException` is
thrown for these four methods — they are part of the supported surface.

#### 4. Strict mode — opt-in stricter compatibility checking

By default, the adapter favors graceful degradation: unsupported operations
fall back to no-ops or attribute-API equivalents (see #3) so legacy code
keeps running. For applications that prefer to **fail fast** when they touch
an unsupported method, strict mode can be enabled.

Activation — two equivalent mechanisms, system property wins:

```bash
# (a) JVM system property (takes precedence over init-param)
java -Dcom.snoworca.adapter.strict=true -jar your-app.jar
```

```xml
<!-- (b) ServletContext init-param in web.xml -->
<context-param>
    <param-name>com.snoworca.adapter.strict</param-name>
    <param-value>true</param-value>
</context-param>
```

When active, methods that are *not* on the strict-bypass allow-list (the
allow-list keeps four `ServletContext` defaults working — see
`ConverterSupport#STRICT_BYPASS_METHODS`) throw
`UnsupportedOperationException` instead of degrading silently. The session
`getValue` family in #3 is implemented as a real alias, so it remains
functional in strict mode.

Strict mode is a one-way switch within a JVM lifetime: once turned on by the
system property, the init-param cannot turn it off again.

#### 5. `AdapterUnwrap` — avoids double wrapping at the boundary

When an already-adapted object is passed back through the boundary in the
opposite direction, the converter recognises the wrapper marker and returns
the original underlying instance instead of wrapping it again. This is
important for object identity (e.g. `request == otherRequest` comparisons
inside the application) and avoids unbounded wrapping chains in
filter/dispatcher pipelines.

`AdapterUnwrap` is implemented on every adapter type and applied automatically
by `ServletConverter.adaptToJakarta(...)` / `adaptToJavax(...)`. Application
code does not need to interact with it directly.

---

### Verification

The release is validated by four orthogonal test layers (all green on
`./gradlew test`):

| Layer                  | Count          | Purpose                                                       |
|------------------------|----------------|---------------------------------------------------------------|
| Unit (Mockito)         | servlet5=182, servlet6=73, servlet61=78 | Per-method delegation correctness     |
| Integration (Tomcat embed) | IT01–IT15 × 3 modules | End-to-end request/response behavior under a real container |
| Reflection Contract    | 39 tests       | Every public method on each adapter is exercised at least once |
| Regression (BugMuseum) | BM01–BM10 × 3 modules | Lock-in for every defect ever fixed in the 0.x line     |

All mocks use `Mockito.mock(X.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS))`
so unstubbed methods fail fast with a useful pointer instead of returning
`null`.

---

### Building from source

```bash
./gradlew build                 # compile + test
./gradlew jacocoCoverageGate    # explicit coverage verification (opt-in)
```

The JaCoCo coverage gate (`jacocoTestCoverageVerification`) is **decoupled
from `./gradlew check`**. The gate is preserved as an explicit, opt-in task
per module:

```bash
./gradlew jacocoCoverageGate
```

The thresholds (line 0.70 / branch 0.60 for the common bundle; line 0.60 /
branch 0.50 for delegation classes; line 0.70 / branch 0.60 for converter
classes) will be tightened in future patch releases along with re-wiring.

---

### License

Apache License 2.0 — see [`LICENSE`](LICENSE).

---

## 한국어 안내

`javax.servlet` (Servlet API 4.0.1) 로 작성된 레거시 애플리케이션을
`jakarta.servlet` 네임스페이스 (Servlet 5 / 6 / 6.1) 를 구현한
웹 애플리케이션 서버에서 그대로 실행할 수 있게 해 주는 양방향 어댑터입니다.

`HttpServletRequest`, `HttpServletResponse`, `ServletContext`, `HttpSession`,
`Cookie` 등 표준 servlet 표면을 경계에서 양방향 변환하므로, `javax.servlet`
기반으로 작성된 애플리케이션을 Tomcat 10+ / Jetty 11+ 런타임에서
**소스 코드 변경 없이** 호스팅할 수 있습니다.

> **Group / Coordinates**: `com.snoworca:jakarta-to-javax-servlet-adapter-*:3.0.0`

---

### 왜 필요한가

Jakarta EE 9 의 네임스페이스 이전 (`javax.*` → `jakarta.*`) 은 servlet API
역사상 가장 큰 호환성 깨짐 변경입니다. 2021년 이후 출시된 모든 컨테이너
(Tomcat 10+, Jetty 11+, Undertow 2.3+, WildFly 27+) 는 `jakarta.servlet`
네임스페이스만 제공합니다. `javax.servlet-api:4.0.1` 에 컴파일된
애플리케이션, 라이브러리, 프레임워크 — 다수의 사내 코드베이스, 레거시 SDK,
유지보수 중단된 서드파티 라이브러리를 포함 — 는 이러한 컨테이너에서
**소스 재작성 없이는 로드 불가** 합니다.

본 어댑터는 컨테이너와 애플리케이션 경계에 위치하여, request / response /
context 타입을 필요한 시점에 변환합니다. 애플리케이션 코드는 무변경
유지됩니다.

---

### 호환성 매트릭스

본 프로젝트는 Jakarta Servlet API 세대별로 3개의 런타임 모듈을 제공합니다.
3 모듈 모두 동일한 `common` 모듈을 공유하며, 동일한 `javax.servlet-api:4.0.1`
표면을 어댑팅합니다.

| 모듈        | Jakarta Servlet API | javax Servlet API | 최소 JDK    | 검증 컨테이너    |
|-------------|---------------------|-------------------|-------------|------------------|
| `servlet5`  | 5.0.0               | 4.0.1             | Java 8      | Tomcat 10.0.x    |
| `servlet6`  | 6.0.0               | 4.0.1             | Java 11     | Tomcat 10.1.x    |
| `servlet61` | 6.1.0               | 4.0.1             | Java 17     | Tomcat 11.x      |

3 모듈 모두 바이트코드 레벨은 `sourceCompatibility = 1.8` /
`targetCompatibility = 1.8` 로 빌드됩니다. 위 표의 **최소 JDK** 컬럼은
해당 Jakarta Servlet API 가 요구하는 런타임 floor 입니다.

`servlet5`, `servlet6`, `servlet61` 간 교차 의존성은
`validateNoVersionModuleCrossDeps` Gradle 게이트 (`CON-MOD-001`) 로 금지됩니다.
배포당 정확히 **하나의** version 모듈만 선택하십시오.

---

### 모듈 구조

```
jakarta-to-javax-servlet-adapter/
├── common/                  # 공통 컨버터, 유틸리티
├── servlet5/                # Jakarta Servlet 5.0 ↔ Javax 4.0
├── servlet6/                # Jakarta Servlet 6.0 ↔ Javax 4.0
├── servlet61/               # Jakarta Servlet 6.1 ↔ Javax 4.0
└── bom/                     # Maven BOM (dependencyManagement 정렬용)
```

각 version 모듈의 내용:

- `adapter.jakarta.servletN.*` — `jakarta.servlet.*` 인스턴스를 wrap 하여
  동일한 shape 로 노출합니다. 어댑터 내부에서 3 세대를 균일하게 다룰 수
  있게 합니다.
- `adapter.javax.servletN.*` — `javax.servlet.*` 인스턴스를 wrap 하여
  레거시 `javax.servlet` 표면을 요구하는 코드에 노출합니다.
- `adapter.servletElementConverterN.*` — 런타임 인스턴스를 보고 어떤 wrapper
  를 설치할지 결정하는 중앙 컨버터. 이미 wrap 된 객체가 경계를 두 번 통과
  할 때 다시 wrap 하지 않는 `AdapterUnwrap` 단축 경로를 포함합니다.
- `com.snoworca.ServletAdapterN` — 편의용 정적 facade (14 메서드,
  `common.ServletAdapter` 에 위임).

---

### 설치

#### Gradle (컨테이너에 맞는 모듈 선택)

```groovy
dependencies {
    // 3 version 모듈 중 하나만 선택
    implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet5:3.0.0'   // Tomcat 10.0.x
    // implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet6:3.0.0'  // Tomcat 10.1.x
    // implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet61:3.0.0' // Tomcat 11.x

    // 레거시 javax Servlet API 는 컴파일 타임에 필요 (런타임은 어댑터가 제공)
    compileOnly 'javax.servlet:javax.servlet-api:4.0.1'
}
```

#### Gradle (멀티 모듈 정렬을 위한 BOM 사용)

```groovy
dependencies {
    implementation platform('com.snoworca:jakarta-to-javax-servlet-adapter-bom:3.0.0')
    implementation 'com.snoworca:jakarta-to-javax-servlet-adapter-servlet6'
    compileOnly    'javax.servlet:javax.servlet-api:4.0.1'
}
```

#### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.snoworca</groupId>
      <artifactId>jakarta-to-javax-servlet-adapter-bom</artifactId>
      <version>3.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.snoworca</groupId>
    <artifactId>jakarta-to-javax-servlet-adapter-servlet6</artifactId>
  </dependency>
  <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

---

### 사용법

#### jakarta → javax (주 방향)

지원되며 권장되는 방향입니다. 컨테이너는 필터/서블릿에 `jakarta.servlet.*`
객체를 넘기고, 어댑터는 동일한 상태를 `javax.servlet.*` view 로 노출합니다.

```java
import adapter.common.ServletAdapter;

public class ExampleServlet extends jakarta.servlet.http.HttpServlet {
    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest request,
                         jakarta.servlet.http.HttpServletResponse response)
            throws jakarta.servlet.ServletException, java.io.IOException {

        javax.servlet.http.HttpServletRequest  javaxRequest  = ServletAdapter.adaptToJavax(request);
        javax.servlet.http.HttpServletResponse javaxResponse = ServletAdapter.adaptToJavax(response);
        javax.servlet.ServletContext           javaxContext  = ServletAdapter.adaptToJavax(getServletContext());

        // 레거시 javax.servlet 코드에 위임...
    }
}
```

#### javax → jakarta (역방향)

지원되나 보조적 용도입니다. 레거시 프레임워크가 `javax.servlet.*` 참조를
보유하고 있으나 호출 대상이 `jakarta.servlet.*` 를 요구할 때 유용합니다.

```java
public class ExampleServlet extends javax.servlet.http.HttpServlet {
    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request,
                         javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {

        jakarta.servlet.http.HttpServletRequest  jakartaRequest  = ServletAdapter.adaptToJakarta(request);
        jakarta.servlet.http.HttpServletResponse jakartaResponse = ServletAdapter.adaptToJakarta(response);
        jakarta.servlet.ServletContext           jakartaContext  = ServletAdapter.adaptToJakarta(getServletContext());
    }
}
```

#### Servlet 필터 패턴 (일반적인 도입 형태)

필터 경계에서 request/response 를 한 번 wrap 하면, 그 이후 downstream
은 변환된 view 만 보게 됩니다.

```java
public class JakartaToJavaxFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest req,
                         jakarta.servlet.ServletResponse res,
                         jakarta.servlet.FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {

        javax.servlet.ServletRequest  javaxReq  = ServletAdapter.adaptToJavax(req);
        javax.servlet.ServletResponse javaxRes  = ServletAdapter.adaptToJavax(res);

        // 레거시 javax-side 애플리케이션 스택에 위임
        LegacyDispatcher.dispatch(javaxReq, javaxRes);

        chain.doFilter(req, res);
    }
}
```

---

### 동작 노트

다음 항목은 단순한 1:1 wrapper 가 아닌 의도된 차이점입니다. 프로덕션 도입
전에 반드시 읽어 두십시오.

#### 1. Cookie attribute API — 정보 손실 정책

`jakarta.servlet.http.Cookie#setAttribute(String, String)` (Servlet 6.0 추가)
는 `SameSite`, RFC 6265bis 의 커스텀 attribute, 향후 호환 flag 등 임의의
attribute 이름을 허용합니다. `javax.servlet.http.Cookie` (Servlet 4.0.1)
에는 **동등한 API 가 없습니다**.

정책:

- `jakarta` cookie 가 `setAttribute(...)` 값을 담은 채로 `javax` 로 어댑팅
  될 때, **`javax.servlet.http.Cookie` 에 1급 setter 가 없는 attribute**
  는 조용히 drop 되며 예외 대신 `ServletConverter` 진단에 기록됩니다.
- 매핑 가능한 attribute (`Domain`, `Path`, `MaxAge`, `Secure`, `HttpOnly`,
  `Comment`, `Version`) 는 정상 forwarding 됩니다.
- 역방향 (`javax` → `jakarta`) 은 canonical attribute 만 보존합니다 —
  round-trip 할 데이터가 없습니다.

이는 의도된 동작이며 버그가 아닙니다. 완전한 무손실 어댑팅은 `javax` view
에 별도 attribute store 를 만들어야 하며, 이는 `javax.servlet-api:4.0.1`
의 의미와 어긋납니다.

#### 2. `HttpServletResponse#setStatus(int, String)` reason phrase — deprecated, no-op

2-인자 형식 `setStatus(int sc, String sm)` 은 Servlet 4.0 에서 deprecate
되었고 modern HTTP/2 스택에서는 wire 에서 제거되었습니다. `javax` 측 코드
가 어댑터 view 에 이 메서드를 호출하면, reason phrase 인자는 **조용히
폐기되고** status code 만 underlying `jakarta` response (`setStatus(int)` 만
노출) 에 forwarding 됩니다.

이는 modern 컨테이너 (Tomcat 9+, Jetty 10+) 의 HTTP/2 connection 동작과
동일합니다.

#### 3. `HttpSession#getValue` / `putValue` / `removeValue` / `getValueNames` — attribute API 자동 fallback

레거시 `javax.servlet.http.HttpSession#getValue(String)` 계열 (Servlet 2.2
부터 deprecate, Jakarta Servlet 6.0 에서 제거됨) 은 `javax` 어댑터 view 에
서 modern attribute API 로 위임되는 **alias** 로 노출됩니다:

| 레거시 메서드               | 구현                                                          |
|-----------------------------|---------------------------------------------------------------|
| `getValue(String name)`     | `getAttribute(name)`                                          |
| `putValue(String, Object)`  | `setAttribute(name, value)`                                   |
| `removeValue(String name)`  | `removeAttribute(name)`                                       |
| `getValueNames()`           | `Collections.list(getAttributeNames()).toArray(new String[0])`|

이로써 underlying Jakarta 컨테이너가 deprecated 메서드를 더 이상 노출하지
않아도, 2002년 이전 작성된 servlet 코드가 무수정으로 세션 상태를 읽고 쓸
수 있습니다. 본 4개 메서드는 `UnsupportedOperationException` 을 던지지
않으며 지원되는 표면입니다.

#### 4. Strict mode — 엄격 호환성 검사 opt-in

기본 모드에서 어댑터는 우아한 degradation 을 선호합니다: 미지원 동작은
no-op 또는 attribute-API 등가물로 fallback (#3 참조) 하여 레거시 코드가
계속 동작합니다. **fail-fast** 를 선호하는 애플리케이션은 strict mode 를
활성화할 수 있습니다.

활성화 — 두 가지 동등 메커니즘. 시스템 프로퍼티가 우선:

```bash
# (a) JVM 시스템 프로퍼티 (init-param 보다 우선)
java -Dcom.snoworca.adapter.strict=true -jar your-app.jar
```

```xml
<!-- (b) ServletContext init-param (web.xml) -->
<context-param>
    <param-name>com.snoworca.adapter.strict</param-name>
    <param-value>true</param-value>
</context-param>
```

활성 시, strict-bypass allow-list 에 없는 메서드 (allow-list 는 4개
`ServletContext` default 를 동작 상태로 유지 — `ConverterSupport#STRICT_BYPASS_METHODS`
참조) 는 조용히 degrade 하지 않고 `UnsupportedOperationException` 을
던집니다. #3 의 세션 `getValue` 계열은 실제 alias 로 구현되어 있어
strict mode 에서도 정상 동작합니다.

Strict mode 는 JVM 수명 내에서 한 방향 스위치입니다: 시스템 프로퍼티로
한 번 켜지면 init-param 으로 끌 수 없습니다.

#### 5. `AdapterUnwrap` — 경계에서 이중 wrapping 회피

이미 어댑팅된 객체가 경계를 반대 방향으로 다시 통과할 때, 컨버터는
wrapper marker 를 인식하여 다시 wrap 하지 않고 underlying 원본 인스턴스
를 반환합니다. 이는 객체 identity (예: 애플리케이션 내부의
`request == otherRequest` 비교) 와 filter/dispatcher 파이프라인의 무한
wrapping chain 방지에 중요합니다.

`AdapterUnwrap` 은 모든 어댑터 타입에 구현되어 있으며
`ServletConverter.adaptToJakarta(...)` / `adaptToJavax(...)` 가 자동으로
적용합니다. 애플리케이션 코드가 직접 다룰 필요 없습니다.

---

### 검증

본 릴리즈는 4개의 직교 테스트 레이어로 검증됩니다 (`./gradlew test` 전수
green):

| 레이어                  | 카운트                                  | 목적                                                          |
|-------------------------|-----------------------------------------|---------------------------------------------------------------|
| Unit (Mockito)          | servlet5=182, servlet6=73, servlet61=78 | 메서드별 위임 정확성                                          |
| 통합 (Tomcat embed)     | IT01–IT15 × 3 모듈                      | 실제 컨테이너 내 end-to-end request/response 동작             |
| Reflection Contract     | 39 tests                                | 어댑터별 모든 public 메서드가 최소 1회 실행됨을 보장          |
| 회귀 (BugMuseum)        | BM01–BM10 × 3 모듈                      | 0.x 시리즈에서 fix 된 결함 전수 lock-in                       |

모든 mock 은 `Mockito.mock(X.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS))`
로 생성하므로, stub 누락 시 `null` 대신 즉시 유용한 포인터로 fail 합니다.

---

### 소스에서 빌드

```bash
./gradlew build                 # compile + test
./gradlew jacocoCoverageGate    # 커버리지 검증 (opt-in)
```

JaCoCo 커버리지 게이트 (`jacocoTestCoverageVerification`) 는 **`./gradlew check`
에서 분리** 되어 있습니다. 모듈별 명시적 opt-in task 로 유지:

```bash
./gradlew jacocoCoverageGate
```

임계값 (common 번들: line 0.70 / branch 0.60, delegation 클래스: line 0.60 /
branch 0.50, converter 클래스: line 0.70 / branch 0.60) 은 향후 패치
릴리즈에서 re-wiring 과 함께 강화될 예정입니다.

---

### 라이선스

Apache License 2.0 — [`LICENSE`](LICENSE) 참조.
