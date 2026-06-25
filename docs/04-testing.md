# 04 - Testing Guide

This document describes the testing strategy, tools, and execution steps used to validate OralCare's business and controller layers.

---

## 1. Test Setup & Architecture
- **Postgres Database**: Handled by **Testcontainers** during the Maven `test` lifecycle phase. Starts a lightweight container running `postgres:15-alpine`.
- **Cache Isolation**: Redundant cache execution overhead is bypassed in tests by configuring `spring.cache.type=none` in the active `test` profile.
- **Spring Context Execution**: Uses `@SpringBootTest` with a randomized port to prevent binding issues.

---

## 2. Dynamic DB Property Injection
We bind Testcontainers database credentials and connection properties dynamically:
```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
}
```

---

## 3. Best Practices
1. **No Shared State**: Ensure databases are truncated or reset between tests using `@BeforeEach`.
2. **Mocking MockMvc**: Use MockMvc for controller tests to execute endpoints in a mock servlet context.
3. **Security Context Mocking**: Inject authentication states using `@WithMockUser` to verify controller authorization constraints.
