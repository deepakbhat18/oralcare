# 06 - Observability & Monitoring Guide

This document covers Actuator endpoints, Prometheus integrations, and custom system metrics.

---

## 1. Exposing Actuator Metrics
Spring Boot Actuator is configured to expose management details:
- **Exposed Paths**: `/api/actuator/health`, `/api/actuator/metrics`, `/api/actuator/prometheus`.
- **Exclusion Policy**: Expose routes publicly but block sensitive actions behind Spring Security authorization checks.

---

## 2. Prometheus Custom Metrics
The application records business performance metrics via Micrometer's `MeterRegistry`:
- `auth.failures.total`: Counts failed logins with reason tags (`LOCKED`, `BAD_CREDENTIALS`).
- `appointments.booked.total`: Tracks booking rates partitioned by `clinic` tags.
- **JVM & DB Statistics**: Automatically tracks database connection pool performance (HikariCP), garbage collection rates, cache hits, and query execution speeds out of the box.

---

## 3. Grafana Dashboard Integrations
To build dashboard visualizations:
- Set Prometheus as the data source.
- Configure query plots for booking volumes over time using `sum(rate(appointments_booked_total[5m]))`.
- Monitor lockouts using `auth_failures_total{reason="LOCKED"}`.
