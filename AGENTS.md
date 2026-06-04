# AGENTS.md — AI Agent Reference

> This document is intended for AI coding agents. It describes the project's
> architecture, conventions, build pipeline, testing strategy, and code style
> rules. **Read this before making any changes.**

---

## 1. Project Identity

| Key               | Value                                              |
| ----------------- | -------------------------------------------------- |
| Name              | Banking Demo API                                   |
| GroupId            | `com.example`                                      |
| ArtifactId        | `demo`                                             |
| Java version      | **25**                                             |
| Spring Boot       | **4.0.6**                                          |
| Spring Modulith   | **2.0.6**                                          |
| Build tool        | Maven (wrapper: `./mvnw`, Maven 3.9.9)             |
| Database          | PostgreSQL (prod/local), H2 in-memory (tests)      |
| Messaging         | Apache Kafka (Confluent CP Kafka 7.5.0)            |
| Base package      | `com.example.demo`                                 |

---

## 2. Critical Build Rules

### ⚠️ ALWAYS run `./mvnw clean verify` before:
- Committing code
- Creating a pull request
- Building Docker images
- Declaring a task "done"

This single command runs **all** of the following checks in order:

| Phase     | Plugin                     | What it does                                          |
| --------- | -------------------------- | ----------------------------------------------------- |
| compile   | maven-compiler-plugin      | Compiles with Lombok annotation processing            |
| test      | surefire (implicit)        | Runs unit tests (`*Test.java`)                        |
| test      | jacoco:report              | Generates unit test coverage report                   |
| verify    | spotless:check             | Enforces code formatting (Eclipse JDT 4.39)           |
| verify    | javadoc:javadoc            | Validates Javadoc (failOnWarnings=true)               |
| package   | spring-boot-maven-plugin   | Packages executable JAR                               |

> **If `verify` fails, the code is not ready.** Fix formatting with
> `./mvnw spotless:apply`, fix Javadoc warnings, or fix test failures before
> proceeding.

### Quick Reference Commands

```bash
# Full verification (REQUIRED before commit/build)
./mvnw clean verify

# Auto-fix formatting issues
./mvnw spotless:apply

# Run only unit tests
./mvnw test

# Run locally with local profile (auto-starts DB via Docker Compose)
make local
# equivalent to: ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Build and run full Docker stack
make docker

# Tear down Docker stack
make down
```

---

## 3. Architecture

### Hexagonal Architecture + Spring Modulith

The project uses **Hexagonal Architecture** (Ports & Adapters) organized into
**Spring Modulith modules**. Each business module is a top-level package under
`com.example.demo`:

```
com.example.demo/
├── account/          # Account management module (closed)
├── transaction/      # Transaction processing module (closed)
├── user/             # User management module (closed)
├── common/           # Shared kernel (OPEN — accessible by all modules)
└── DemoApplication.java
```

### Module Internal Structure

Every module follows this exact layering:

```
<module>/
├── package-info.java                          # @ApplicationModule metadata
├── application/
│   ├── port/
│   │   ├── in/                                # Inbound ports (use case interfaces)
│   │   │   ├── <Verb><Noun>UseCase.java       # e.g., CreateAccountUseCase
│   │   │   └── package-info.java
│   │   └── out/                               # Outbound ports (repository interfaces)
│   │       └── <Noun>Repository.java          # e.g., AccountRepository
│   └── service/
│       └── <Verb><Noun>Service.java           # Use case implementations
├── domain/
│   ├── model/
│   │   ├── <Entity>.java                      # Rich domain models (not JPA entities!)
│   │   └── package-info.java
│   ├── event/                                 # Domain events (transaction module)
│   │   └── package-info.java
│   └── exception/
│       └── package-info.java
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/
    │   │   │   ├── <Noun>Controller.java      # REST controllers
    │   │   │   └── dto/                       # Request/Response DTOs
    │   │   └── event/                         # Kafka event listeners
    │   └── out/
    │       ├── persistence/
    │       │   ├── Postgres<Noun>Adapter.java  # Implements outbound port
    │       │   ├── entity/
    │       │   │   └── <Noun>JpaEntity.java   # JPA entities (separate from domain!)
    │       │   └── repository/
    │       │       └── SpringData<Noun>Repository.java  # Spring Data interfaces
    │       └── event/                         # Outbox event publishing
    └── config/                                # Module-specific configuration
```

### Key Architecture Rules

1. **Domain models ≠ JPA entities.** Domain models live in `domain/model/` and
   are plain Java objects with business logic. JPA entities live in
   `infrastructure/adapter/out/persistence/entity/` and are persistence
   concerns only. Adapters map between the two.

2. **Dependencies flow inward.** `infrastructure` → `application` → `domain`.
   Never reference infrastructure classes from domain or application layers.

3. **Cross-module communication** uses only the target module's **inbound port
   interfaces** (`application/port/in/`). Never import from another module's
   `infrastructure` or `domain` packages.

4. **`common` module is open** (`Type.OPEN` in `package-info.java`). All other
   modules are closed — only their `application/port/in/` packages are
   accessible cross-module.

5. **Spring Modulith verifies these rules.** The `ModularityTests.java` test
   enforces module boundaries at build time. Violations will fail the build.

---

## 4. Spring Modulith Conventions

### Module Declaration

Every module has a `package-info.java`:

```java
// Closed module (default) — only exposes application/port/in
@org.springframework.modulith.ApplicationModule
package com.example.demo.account;

// Open module — all sub-packages are accessible
@org.springframework.modulith.ApplicationModule(type = Type.OPEN)
package com.example.demo.common;
```

### Module Tests

Each module has an `@ApplicationModuleTest` that bootstraps only that module:

```java
@ApplicationModuleTest
class AccountModuleTest {
    @MockitoBean
    GetUserUseCase getUserUseCase; // Mock cross-module dependencies
}
```

### Modularity Verification

The `ModularityTests` class verifies the entire module structure:

```bash
# Run modularity check
./mvnw test -Dtest=ModularityTests

# Generate PlantUML module diagrams → target/modulith-docs/
./mvnw test -Dtest=ModularityTests#createModuleDocumentation
```

---

## 5. Custom Annotations

| Annotation              | Location                           | Purpose                                                   |
| ------------------------ | ---------------------------------- | --------------------------------------------------------- |
| `@UseCase`               | `common.application.annotation`    | Marks a service as a use case; picked up by `BeanConfig` component scan |
| `@TransactionalUseCase`  | `common.application.annotation`    | Meta-annotated with `@UseCase`; AOP auto-wraps all methods in `@Transactional` via `TransactionAopConfig` |

**When creating a new service:**
- Use `@TransactionalUseCase` if the service modifies data (writes/updates)
- Use `@UseCase` for read-only query services
- **Do NOT use** `@Service` or `@Transactional` directly on use case classes

---

## 6. Code Style & Formatting

### Spotless (Eclipse JDT Formatter)

The project enforces formatting via **Spotless Maven Plugin** with Eclipse JDT
4.39:

| Rule                      | Setting                            |
| ------------------------- | ---------------------------------- |
| Formatter                 | Eclipse JDT 4.39                   |
| Import order              | `java|javax, org, com, <blank>`    |
| Remove unused imports     | Yes                                |
| Trim trailing whitespace  | Yes                                |
| End with newline          | Yes                                |
| Git files                 | `.gitattributes`, `.gitignore` also formatted (4-space indent) |
| Check phase               | `verify`                           |

```bash
# Check formatting (runs automatically during ./mvnw verify)
./mvnw spotless:check

# Auto-fix formatting
./mvnw spotless:apply
```

### Line Endings

`.gitattributes` enforces `LF` line endings for all `.java` files:
```
*.java text eol=lf
```

### Javadoc Requirements

The `maven-javadoc-plugin` runs with **`failOnWarnings=true`** during the
`verify` phase. This means:

- **All public classes must have Javadoc class-level comments**
- **All public methods must have Javadoc** with `@param` and `@return` tags
- **All `@param` tags must match actual parameter names**
- Javadoc warnings will **fail the build**

Use this format (note the tab-based indentation matching the project style):

```java
/**
 * Brief description of the class.
 */
public class MyService {

    /**
     * Brief description of the method.
     *
     * @param paramName
     *            description of the parameter
     * @return description of the return value
     */
    public ReturnType myMethod(ParamType paramName) { ... }
}
```

### Indentation

- **Tabs** for Java indentation (Eclipse JDT default)
- **4 spaces** for git config files (`.gitattributes`, `.gitignore`)

---

## 7. Dependency Injection

- **Constructor injection only.** No `@Autowired` on fields.
- Explicit constructors with Javadoc (do not rely solely on Lombok's
  `@AllArgsConstructor` for injectable beans).
- Lombok `@Getter` is used on domain models; avoid `@Setter` on domain models.

---

## 8. Testing Strategy

### Test Pyramid

| Layer                | Test Type         | Suffix    | Framework          | Runs During      |
| -------------------- | ----------------- | --------- | ------------------ | ---------------- |
| Domain model         | Unit              | `Test`    | JUnit 5            | `mvn test`       |
| Application service  | Unit              | `Test`    | JUnit 5 + Mockito  | `mvn test`       |
| Controller (web)     | Slice             | `Test`    | `@WebMvcTest` + MockMvc | `mvn test`  |
| Persistence adapter  | Slice             | `Test`    | `@DataJpaTest`     | `mvn test`       |
| Module integration   | Integration       | `Test`    | `@ApplicationModuleTest` | `mvn test` |
| Modularity structure | Verification      | `Tests`   | Spring Modulith    | `mvn test`       |

### Code Coverage Requirement

**All code changes must achieve nearly 100% test coverage.** After running `./mvnw clean verify`, always check the JaCoCo report generated in `target/site/jacoco/jacoco.csv` (or `index.html`) to verify that all lines and branches are covered. Extend tests if coverage drops below 100%.

### Test Database

Tests use **H2 in-memory database** in PostgreSQL compatibility mode
(configured in `src/test/resources/application.yml`):

```yaml
spring:
  datasource:
    url: "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS events_schema"
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Test Naming Conventions

```java
// Unit test: method name starts with "should"
@Test
void shouldCreateAccountAndSaveToRepository() { ... }

@Test
void shouldThrowEntityNotFoundExceptionWhenUserNotFound() { ... }
```

### Mocking

- Use `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` for
  pure unit tests
- Use `@MockitoBean` (Spring Boot) for replacing beans in `@WebMvcTest`,
  `@DataJpaTest`, or `@ApplicationModuleTest` contexts
- Mock **cross-module dependencies** at the inbound port level

### Kafka in Tests

Kafka logging is suppressed in test configuration. The `KafkaErrorHandlerIntegrationTest`
uses Testcontainers (`org.testcontainers:kafka:1.19.7`) for Kafka integration
tests.

---

## 9. Database & Schema

### Schema Management

- **Hibernate `ddl-auto: validate`** in production — Hibernate verifies schema
  but never modifies it
- **`schema.sql`** runs on startup (`spring.sql.init.mode=always`) to create
  tables via `CREATE TABLE IF NOT EXISTS`
- Schema changes must be made in `src/main/resources/schema.sql`
- Tests use `ddl-auto: create-drop` with H2

### Database Structure

| Table                       | Schema           | Module       |
| --------------------------- | ---------------- | ------------ |
| `accounts` (via JPA)        | `public`         | account      |
| `users` (via JPA)           | `public`         | user         |
| `transaction_records`       | `public`         | transaction  |
| `outbox_events`             | `events_schema`  | transaction  |
| `processed_events`          | `events_schema`  | transaction  |

### Concurrency

- **Optimistic locking** via `@Version` on JPA entities (e.g., `AccountJpaEntity`)
- **Pessimistic locking** via `lockAndLoad()` on `AccountRepository` for
  balance mutations (SELECT ... FOR UPDATE)

---

## 10. Event System (Transactional Outbox)

The transaction module implements the **Transactional Outbox Pattern**:

1. Business operation + outbox event are saved in a single DB transaction
   (`OutboxEventPublisherAdapter`)
2. `OutboxEventScheduler` polls the outbox table and publishes events to Kafka
3. `KafkaTransactionEventListener` consumes events with idempotency guard
   (`processed_events` table)

### Event Types

Events are in `transaction.domain.event/`:
- `TransactionPendingEvent`
- `TransactionCompletedEvent`
- `TransactionFailedEvent`

---

## 11. REST API Conventions

### Base URL Structure

```
/api/accounts/**     → AccountController
/api/transactions/** → TransactionController
/api/transfers/**    → TransferController
/api/users/**        → UserController
```

### Authentication

User identity is passed via `X-User-Id` request header (no Spring Security).

### OpenAPI / Swagger

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI spec**: `http://localhost:8080/api-docs`
- All controllers use `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`
- All DTOs use `@Schema` annotations

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps exceptions to HTTP
status codes:

| Exception                       | HTTP Status |
| ------------------------------- | ----------- |
| `EntityNotFoundException`       | 404         |
| `DomainException`               | 400         |
| `IllegalArgumentException`      | 400         |
| `IllegalStateException`         | 400         |
| `SecurityException`             | 403         |
| `MethodArgumentNotValidException` | 400       |

### DTO Conventions

- Request DTOs: Java `record` types with Jakarta Validation annotations
- Response DTOs: Java `record` types
- DTOs live in `infrastructure/adapter/in/web/dto/`
- Mapping from domain to DTO is done in the controller (simple `toResponse()`
  private methods)

---

## 12. Docker & Deployment

### Local Development

```bash
# Start infrastructure (PostgreSQL + Kafka + Kafka UI) then run app
make local
# This activates the "local" Spring profile which uses compose-local.yaml
# Spring Boot Docker Compose support auto-starts/stops containers
```

### Full Docker Stack

```bash
# Build JAR, then build and start all containers
make docker

# Tear down
make down
```

### Docker Compose Services

**`compose.yaml`** (full stack):
| Service    | Image                        | Port       |
| ---------- | ---------------------------- | ---------- |
| `app`      | Built from Dockerfile        | 8080:8080  |
| `postgres` | `postgres:latest`            | 5432:5432  |
| `kafka`    | `confluentinc/cp-kafka:7.5.0`| 9092:9092  |
| `kafka-ui` | `provectuslabs/kafka-ui`     | 8081:8080  |

**`compose-local.yaml`** (infrastructure only):
| Service    | Image                        | Port       |
| ---------- | ---------------------------- | ---------- |
| `postgres` | `postgres:latest`            | 5432:5432  |
| `kafka`    | `confluentinc/cp-kafka:7.5.0`| 9092:9092  |
| `kafka-ui` | `provectuslabs/kafka-ui`     | 8081:8080  |

### Spring Profiles

| Profile   | Purpose                    | Config File                        |
| --------- | -------------------------- | ---------------------------------- |
| (default) | Base config                | `application.yml`                  |
| `local`   | Local dev (compose-local)  | `application-local.properties`     |
| `docker`  | Containerized deployment   | `application-docker.properties`    |

---

## 13. File Naming Conventions

| Type                  | Naming Pattern                        | Example                          |
| --------------------- | ------------------------------------- | -------------------------------- |
| Use case interface    | `<Verb><Noun>UseCase.java`            | `CreateAccountUseCase.java`      |
| Use case command      | Inner `record` in use case interface  | `CreateAccountCommand`           |
| Service impl          | `<Verb><Noun>Service.java`            | `CreateAccountService.java`      |
| Controller            | `<Noun>Controller.java`              | `AccountController.java`         |
| Domain model          | `<Noun>.java`                        | `Account.java`                   |
| JPA entity            | `<Noun>JpaEntity.java`              | `AccountJpaEntity.java`          |
| Repository port       | `<Noun>Repository.java`             | `AccountRepository.java`         |
| Spring Data repo      | `SpringData<Noun>Repository.java`   | `SpringDataAccountRepository.java` |
| Persistence adapter   | `Postgres<Noun>Adapter.java`        | `PostgresAccountAdapter.java`    |
| Request DTO           | `<Noun>Request.java`                | `TransactionRequest.java`        |
| Response DTO          | `<Noun>Response.java`               | `AccountResponse.java`           |
| Unit test             | `<ClassName>Test.java`               | `CreateAccountServiceTest.java`  |
| Module test           | `<Module>ModuleTest.java`            | `AccountModuleTest.java`         |
| Package declaration   | `package-info.java`                  |                                  |

---

## 14. Adding a New Feature — Checklist

When adding a new entity/feature to an existing module:

1. [ ] Create/update the **domain model** in `<module>/domain/model/`
2. [ ] Create the **inbound port** (use case interface + command record) in
       `<module>/application/port/in/`
3. [ ] Create the **outbound port** (repository interface) in
       `<module>/application/port/out/` if persistence is needed
4. [ ] Implement the **service** in `<module>/application/service/`, annotated
       with `@UseCase` or `@TransactionalUseCase`
5. [ ] Create the **JPA entity** in
       `<module>/infrastructure/adapter/out/persistence/entity/`
6. [ ] Create the **Spring Data repository** in
       `<module>/infrastructure/adapter/out/persistence/repository/`
7. [ ] Implement the **persistence adapter** in
       `<module>/infrastructure/adapter/out/persistence/`
8. [ ] Create **request/response DTOs** in
       `<module>/infrastructure/adapter/in/web/dto/`
9. [ ] Create the **controller** in
       `<module>/infrastructure/adapter/in/web/`
10. [ ] Add `@Operation`, `@ApiResponse`, `@Tag`, `@Parameter` annotations
11. [ ] Add complete **Javadoc** to all public classes and methods
12. [ ] Update `schema.sql` if new tables/columns are needed
13. [ ] Add `package-info.java` to any new packages with Javadoc
14. [ ] Write **unit tests** for domain model, service, controller, and adapter
15. [ ] Write **module integration test** if needed
16. [ ] Check **JaCoCo report** (`target/site/jacoco/jacoco.csv`) and ensure **100% coverage** (extend tests if needed)
17. [ ] Run `./mvnw spotless:apply` to fix formatting
18. [ ] Run `./mvnw clean verify` — **must pass before commit**

When creating a **new module**:

1. [ ] Create the top-level package `com.example.demo.<module>/`
2. [ ] Add `package-info.java` with `@ApplicationModule` annotation
3. [ ] Follow the internal structure described in Section 3
4. [ ] Ensure the module test `ModularityTests` still passes

---

## 15. Common Pitfalls

| Pitfall                                      | Fix                                                     |
| -------------------------------------------- | ------------------------------------------------------- |
| Importing another module's internal classes   | Use only `application/port/in/` interfaces               |
| Missing Javadoc on public API                 | Add `/** */` with `@param` and `@return` tags            |
| Using `@Service` instead of `@UseCase`        | Use `@UseCase` or `@TransactionalUseCase`                |
| Using `@Transactional` on service classes     | Use `@TransactionalUseCase` — AOP handles transactions   |
| Putting business logic in JPA entities        | Put logic in domain models, map in adapters              |
| Formatting errors in CI                       | Run `./mvnw spotless:apply` before committing            |
| `@Autowired` field injection                  | Use constructor injection                                |
| Tests failing with DB errors                  | Tests use H2 — check `src/test/resources/application.yml`|
| Using `@Setter` on domain models              | Domain models should encapsulate state mutations          |
| Skipping tests with `-DskipTests`             | Only acceptable for `make docker` (production images)    |
