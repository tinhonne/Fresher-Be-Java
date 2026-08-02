# AGENTS.md
## 1. Project Overview
- Project name: Bank App
- Purpose: Provide a RESTful API for customer, account, and user management with JWT-based authentication.
- Current status: Educational/portfolio project under development. The repository has basic functionality, one Spring context-load test, and documented production-readiness limitations.
- Primary users: Developers or reviewers using the project for educational/portfolio purposes; intended end-user audience is [NEEDS CONFIRMATION].
## 2. Technology Stack
- Language: Java 21
- Framework: Spring Boot 3.5.15, Spring Web, Spring Data JPA, Spring Security
- Database: MySQL 8.0 with Hibernate/JPA; schema currently managed using `spring.jpa.hibernate.ddl-auto=update`
- Build tool: Maven with Maven Wrapper
- Testing: JUnit 5 and Spring Boot Test; current coverage is limited to a Spring context-load test
- Main libraries: MapStruct 1.5.5.Final, Lombok 1.18.34, Jakarta Bean Validation, Nimbus JOSE JWT, Spring Security Crypto, and MySQL Connector/J
## 3. Project Structure
- `src/main/java/com/example/demo/controller`: REST controllers and HTTP endpoint handling
- `src/main/java/com/example/demo/service`: Business service interfaces
- `src/main/java/com/example/demo/service/impl`: Business logic and service implementations
- `src/main/java/com/example/demo/repository`: Spring Data JPA repositories
- `src/main/java/com/example/demo/entity`: JPA entities, shared persistence fields, and entity enums
- `src/main/java/com/example/demo/dto/request`: Incoming API request DTOs and validation constraints
- `src/main/java/com/example/demo/dto/response`: Outgoing API DTOs, standard response envelopes, and pagination responses
- `src/main/java/com/example/demo/mapper`: MapStruct entity/DTO mapper interfaces
- `src/main/java/com/example/demo/exception`: Application exceptions, error codes, and centralized exception handling
- `src/main/java/com/example/demo/specification`: JPA Specifications for dynamic customer filtering
- `src/main/java/com/example/demo/config`: Spring configuration, including the security filter chain and JWT decoder
- `src/main/resources/application.properties`: Application, datasource, JPA, and JWT-related configuration; never reproduce secret values from this file
- `src/test/java`: Automated tests; currently contains only `DemoApplicationTests`
- `target`: Generated Maven output; do not edit or treat as maintained source
Do not introduce a new folder structure without approval.
## 4. Architecture
- Architecture style: Layered Spring Boot architecture with controllers, service interfaces, service implementations, repositories, entities, DTOs, and mappers.
- Request flow: HTTP request → controller → service interface/implementation → repository → JPA entity → MySQL; responses are mapped to DTOs and normally wrapped in `ApiResponse<T>`, with `PageResponse<T>` used for paginated results.
- Dependency direction: Controllers depend on service interfaces; service implementations depend on repositories and MapStruct mappers; repositories depend on entities. DTOs define API boundaries and entities should not be returned directly from controllers.
- Mapping strategy: Use Spring-managed MapStruct mapper interfaces to convert request DTOs to entities, update existing entities, and convert entities to response DTOs. Mapper implementations are generated during Maven compilation.
Follow the existing architecture and coding patterns.
## 5. Commands
- Build: `./mvnw clean verify`
- Run tests: `./mvnw test`
- Run one test: `./mvnw test -Dtest=DemoApplicationTests`
- Run application: `./mvnw spring-boot:run`
- Lint or static analysis: `[NEEDS CONFIRMATION]`
On Windows, the equivalent wrapper executable may be `mvnw.cmd`; the canonical project command is [NEEDS CONFIRMATION].
Do not invent commands. Use only commands verified for this project.
## 6. Project-Specific Rules
- Preserve the standard `ApiResponse<T>` response envelope and use `PageResponse<T>` for paginated responses unless an approved API change requires otherwise.
- Keep HTTP handling in controllers and business rules in service implementations; access persistence through Spring Data repositories rather than directly from controllers.
- Use request and response DTOs at API boundaries and MapStruct for entity/DTO conversion.
- Apply Jakarta Bean Validation to incoming DTOs and use the existing `AppException`, `ErrorCode`, and `GlobalExceptionHandler` error-handling pattern.
- Customer identity numbers and account numbers are unique. Preserve both service-level checks and database uniqueness constraints.
- Customer deletion is currently a soft deletion implemented by changing its status, and deletion must be rejected when the customer has an active account.
- The meanings of numeric customer and account status values are `[NEEDS CONFIRMATION]`; do not introduce or reinterpret status numbers without approval.
- New user passwords must be BCrypt encoded and must never be returned, logged, or stored as plaintext.
- Preserve HS512 compatibility between JWT creation and Spring Security JWT decoding.
- Do not expose or duplicate datasource credentials, JWT signing material, or other secret configuration. Secret values already present in configuration must not be copied into code, documentation, tests, or reports.
- Treat `src/main/resources/application.properties`, `pom.xml`, and persisted entity mappings as sensitive configuration or schema-impacting files; changes require explicit approval and impact review.
- Do not rely on Hibernate `ddl-auto=update` as a production migration strategy or add a migration tool without approval.
- No Docker configuration is currently present. Do not claim Docker support or invent Docker commands; intended Docker support is `[NEEDS CONFIRMATION]`.
- Test coverage is currently minimal. Add focused tests for changed behavior when implementation is approved, without assuming Testcontainers or other unconfigured infrastructure.
## 7. Default Working Mode: Plan Only
- Always start in PLAN-ONLY mode.
- Inspect the codebase using read-only operations.
- Do not create, modify, delete, move, rename, or format files.
- Do not install dependencies or change project configuration.
- Do not run migrations, code generators, or destructive commands.
- Do not treat the initial task description as permission to edit code.
- First provide an implementation plan and wait for approval.
The plan must include:
1. Understanding of the task.
2. Current behavior and relevant files.
3. Proposed implementation steps.
4. Files expected to change.
5. Risks and assumptions.
6. Verification and testing strategy.
Only edit code after the user explicitly says:
`APPROVED: IMPLEMENT THE PLAN`
## 8. Implementation Rules
After approval:
- Modify only the approved scope.
- Preserve existing behavior unless the task requires otherwise.
- Prefer small and focused changes.
- Follow existing naming and coding conventions.
- Do not add dependencies without approval.
- Do not refactor unrelated code.
- Do not hide errors or bypass validation.
- Never expose credentials, tokens, or secrets.
- Stop and request approval if the scope must expand.
## 9. Testing and Verification
- Run the smallest relevant tests first.
- Run the full test suite when appropriate.
- Do not change tests merely to make failing code pass.
- Clearly report tests that could not be executed.
- Never claim success without verification evidence.
## 10. Git Rules
- Do not create or switch branches unless requested.
- Do not commit, push, merge, or rebase unless requested.
- Do not discard existing user changes.
- Review `git status` and `git diff` before reporting completion.
- Include only task-related changes.
## 11. Completion Report
After implementation, report:
1. What was changed.
2. Files that were modified.
3. Tests and commands executed.
4. Verification results.
5. Remaining risks or follow-up work.
6. Changes requiring manual review.
## 12. Database MCP rules
- Use the MySQL MCP for read-only inspection.
- Never execute INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE, or DDL statements.
- Never run SELECT * without a restrictive WHERE clause and LIMIT.
- Limit sample-data queries to at most 50 rows.
- Prefer metadata commands such as DESCRIBE, SHOW CREATE TABLE, and SHOW INDEX.
- Use EXPLAIN before executing potentially expensive JOIN, GROUP BY, ORDER BY, or aggregate queries.
- Do not run full-table scans on large tables.
- Do not retrieve passwords, tokens, secrets, personal data, or other sensitive fields.
- Prefer COUNT estimates and schema inspection over retrieving complete datasets.
- Ask for confirmation before running a query that may scan a large table.