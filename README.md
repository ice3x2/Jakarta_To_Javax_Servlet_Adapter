# Jakarta to Javax Servlet Adapter

A bidirectional adapter that lets legacy `javax.servlet` (Servlet API 4.0.1)
applications run inside Web Application Servers that implement the
`jakarta.servlet` namespace (Servlet 5/6/6.1).

`HttpServletRequest`, `HttpServletResponse`, `ServletContext`, `HttpSession`,
`Cookie`, and the rest of the standard servlet surface can be converted in
either direction at the boundary, so an application written against `javax.servlet`
can be hosted on a Tomcat 10+/Jetty 11+ runtime without changing application code.

> **Group / Coordinates**: `com.clipsoft:jakarta-to-javax-servlet-adapter-*:1.0.0`

---

## Compatibility matrix

The project ships three runtime modules, each targeting a specific Jakarta
Servlet API generation. All three share the same `common` module and adapt the
same `javax.servlet-api:4.0.1` source surface.

| Module      | Jakarta Servlet API | javax Servlet API | Minimum JDK | Validated container |
|-------------|---------------------|-------------------|-------------|---------------------|
| `servlet5`  | 5.0.0               | 4.0.1             | Java 8      | Tomcat 10.0.x       |
| `servlet6`  | 6.0.0               | 4.0.1             | Java 11     | Tomcat 10.1.x       |
| `servlet61` | 6.1.0               | 4.0.1             | Java 17     | Tomcat 11.x         |

All three modules are built with `sourceCompatibility = 1.8` / `targetCompatibility = 1.8`
for the bytecode level; the **minimum JDK** column above reflects the runtime
floor required by the corresponding Jakarta Servlet API.

Cross-version dependencies between `servlet5`, `servlet6`, and `servlet61` are
forbidden by the `validateNoVersionModuleCrossDeps` Gradle gate
(CON-MOD-001) — pick exactly **one** version module per deployment.

---

## Installation

### Gradle (pick the module that matches your container)

```groovy
dependencies {
    // Choose ONE of the three version modules
    implementation 'com.clipsoft:jakarta-to-javax-servlet-adapter-servlet5:1.0.0'   // Tomcat 10.0.x
    // implementation 'com.clipsoft:jakarta-to-javax-servlet-adapter-servlet6:1.0.0'  // Tomcat 10.1.x
    // implementation 'com.clipsoft:jakarta-to-javax-servlet-adapter-servlet61:1.0.0' // Tomcat 11.x

    // The legacy javax Servlet API is required at compile time (provided by the adapter at runtime)
    compileOnly 'javax.servlet:javax.servlet-api:4.0.1'
}
```

### Gradle (with BOM for multi-module alignment)

```groovy
dependencies {
    implementation platform('com.clipsoft:jakarta-to-javax-servlet-adapter-bom:1.0.0')
    implementation 'com.clipsoft:jakarta-to-javax-servlet-adapter-servlet6'
    compileOnly    'javax.servlet:javax.servlet-api:4.0.1'
}
```

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.clipsoft</groupId>
      <artifactId>jakarta-to-javax-servlet-adapter-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.clipsoft</groupId>
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

## Usage

### jakarta &rarr; javax (primary direction)

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

### javax &rarr; jakarta (reverse direction)

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

---

## Behavior notes

The following five items document deviations from a naive 1:1 wrapper. Read
them before adopting the adapter in production.

### 1. Cookie attribute API — information loss policy

`jakarta.servlet.http.Cookie#setAttribute(String, String)` (added in Servlet 6.0)
accepts arbitrary attribute names — including `SameSite`, custom RFC 6265bis
attributes, and forward-compatible flags. The `javax.servlet.http.Cookie`
class (Servlet 4.0.1) has **no equivalent API**.

Policy:

- When a `jakarta` cookie carrying `setAttribute(...)` values is adapted to
  `javax`, attributes that have **no matching first-class setter on `javax.servlet.http.Cookie`**
  are dropped silently and recorded in `ServletConverter` diagnostics rather
  than thrown.
- Attributes that *do* map (`Domain`, `Path`, `MaxAge`, `Secure`, `HttpOnly`,
  `Comment`, `Version`) are forwarded normally.
- The reverse direction (`javax` &rarr; `jakarta`) preserves only the canonical
  attributes — there is nothing to round-trip.

This is intentional and not a bug: a fully lossless adaptation would require
inventing a parallel attribute store on the `javax` view, which would diverge
from `javax.servlet-api:4.0.1` semantics.

### 2. `HttpServletResponse#setStatus(int, String)` reason phrase — deprecated, no-op

The two-argument form `setStatus(int sc, String sm)` was deprecated in
Servlet 4.0 and removed from the wire in modern HTTP/2 stacks. When code
running on the `javax` side calls this method against an adapter view, the
reason phrase argument is **silently discarded** and only the status code is
forwarded to the underlying `jakarta` response (which exposes only
`setStatus(int)`).

This matches the behavior of every modern container (Tomcat 9+, Jetty 10+),
which already ignores the phrase on HTTP/2 connections.

### 3. `HttpSession#getValue` / `putValue` / `removeValue` / `getValueNames` — automatic fallback to attribute API

The legacy `javax.servlet.http.HttpSession#getValue(String)` family
(deprecated since Servlet 2.2 and removed in Jakarta Servlet 6.0) is exposed
on the `javax` adapter view as **aliases** that delegate to the modern
attribute API:

| Legacy method                | Implemented as                                |
|------------------------------|------------------------------------------------|
| `getValue(String name)`      | `getAttribute(name)`                           |
| `putValue(String, Object)`   | `setAttribute(name, value)`                    |
| `removeValue(String name)`   | `removeAttribute(name)`                        |
| `getValueNames()`            | `Collections.list(getAttributeNames()).toArray(new String[0])` |

This lets unmodified pre-2002 servlet code continue to read and write session
state through the adapter even though the underlying Jakarta container no
longer exposes the deprecated methods. No `UnsupportedOperationException` is
thrown for these four methods — they are part of the supported surface.

### 4. Strict mode — opt-in stricter compatibility checking

By default, the adapter favors graceful degradation: unsupported operations
fall back to no-ops or attribute-API equivalents (see #3) so legacy code keeps
running. For applications that prefer to **fail fast** when they touch an
unsupported method, strict mode can be enabled.

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

### 5. Migration from 0.x

0.0.x releases shipped as a single artifact and did not differentiate between
Servlet 5, 6, and 6.1 containers. 1.0.0 splits the runtime into three
version-specific modules.

Migration steps:

1. **Identify your container's Jakarta Servlet API generation** (Tomcat 10.0
   &rarr; Servlet 5, Tomcat 10.1 &rarr; Servlet 6, Tomcat 11 &rarr; Servlet 6.1).
   Pick the matching `-servletN` module from the [Compatibility matrix](#compatibility-matrix).
2. **Replace the old single-artifact coordinate** with the new version-specific
   coordinate (e.g. `jakarta-to-javax-servlet-adapter-servlet6:1.0.0`).
3. **Re-read sections 1–4 above.** 0.0.x silently ignored cookie attributes
   and `setStatus` reason phrases; 1.0.0 documents those as policy and adds an
   opt-in strict mode (#4) for callers that want explicit failures.
4. **If your code relied on `HttpSession#getValue` / `putValue`** continuing
   to work against a Jakarta Servlet 6.0+ container, no change is required —
   the alias in #3 makes those calls behave the same as on Servlet 4.0.1.
5. **Java version**: confirm your JVM matches the **Minimum JDK** column for
   the selected module (8 / 11 / 17).

There is no automatic upgrade path from 0.0.x — the coordinate change is
required.

---

## Building from source

```bash
./gradlew build                 # compile + test (coverage gate not included)
./gradlew jacocoCoverageGate    # explicit coverage verification (opt-in)
```

### Coverage gate (1.0.0 note)

In 1.0.0 the JaCoCo coverage gate (`jacocoTestCoverageVerification`) is
**decoupled from `./gradlew check`**. The gate is preserved as an explicit,
opt-in task per module:

```bash
./gradlew jacocoCoverageGate
```

Rationale: adapter unit tests are deferred from 1.0.0 (delegation classes are
currently exercised only via the `DelegationContractTest` driver). Running
the gate as part of `check` would fail the release build until those unit
tests are added. 1.0.1 will add adapter unit tests and re-wire
`check.dependsOn(jacocoTestCoverageVerification)` so the gate runs on every
CI build again.

The thresholds (line 0.70 / branch 0.60 for the common bundle; line 0.60 /
branch 0.50 for delegation classes; line 0.70 / branch 0.60 for converter
classes) remain unchanged and will be tightened in 1.0.1 along with the
re-wiring.

---

## License

Apache License 2.0 — see `LICENSE`.
