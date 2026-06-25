# 10 - Technical Interview Guide

This guide compiles design explanations, trade-offs, and architecture questions for senior-level project walkthroughs.

---

## 1. Core Architectural Questions

### Q1: Why did you implement Refresh Token Rotation (RTR)?
- **Answer**: JWT access tokens are short-lived. Refresh tokens are long-lived. If an attacker intercepts a refresh token, they can silently generate access tokens indefinitely. RTR rotates the refresh token with each refresh call.
- **Key mechanism**: Marking rotated tokens as used. If an old token is presented again (replay attack), the backend revokes the user's entire session family, locking out the attacker and prompting the legitimate user to sign in again.

### Q2: How did you implement dynamic API versioning?
- **Answer**: Rather than duplicating paths manually in every controller, I created `VersionMappingConfig`, extending Spring's `RequestMappingHandlerMapping`. This automatically mounts `/v1` and `/v2` prefixes to all controller endpoints dynamically, keeping codebase clean and version boundaries flexible.

### Q3: How is multi-tenant clinic isolation enforced?
- **Answer**: Isolation is scoped during token validation. The `JwtAuthenticationFilter` reads the tenant's `clinicId` from the JWT claims, storing it in a thread-safe `TenantContext` ThreadLocal. Standard controllers and services validate entity attributes against this context. Finally, filter blocks clear the thread-local storage to prevent cross-request bleed.

### Q4: Why did you choose Testcontainers over H2 for integration tests?
- **Answer**: H2 is an in-memory database that often lacks dialect features (such as JSON queries, Postgres indexing, or constraint rules). Testcontainers runs a real instance of PostgreSQL inside Docker, guaranteeing that tests execute under identical conditions to production.

---

## 2. Design Decisions & Trade-offs
- **Postgres + Flyway**: Schema evolution is tracked version-by-version.
- **Actuator + Prometheus**: Decoupled scraping limits overhead.
- **Magic Byte Validation**: Moving file validation checks to byte signature level protects against HTTP headers spoofing.
- **Lombok Builder & DTO Mappings**: Keeps model representations clean.
