# 08 - C4 Architecture Diagrams

This document details the architectural layout of the OralCare Dental SaaS backend.

---

## 1. System Context Diagram
Shows how the system fits into the dental clinic IT landscape.

```mermaid
graph TD
    User[Patient / Dentist / Receptionist] -->|HTTPS / REST API| OralCare[OralCare System Backend]
    OralCare -->|SMTP / Secure Connection| Mail[Email Delivery Server]
    OralCare -->|JDBC / SSL| DB[(PostgreSQL Database)]
    OralCare -->|Redis Protocol| Cache[(Redis Cache / Session Store)]
```

---

## 2. Container Diagram
Decomposes the OralCare system into its core containers.

```mermaid
graph TB
    subgraph OralCare SaaS
        Controller[Spring Boot Controllers] -->|Delegates| Service[Spring Boot Services]
        Service -->|Data Access| Repo[Spring Boot Repositories]
        Filter[Tenant Scoping Filter] -->|Intercepts| Controller
    end
    Client[Web Browser / Mobile Client] -->|REST / JSON| Filter
    Repo -->|SQL| DB[(PostgreSQL Database)]
    Service -->|Metrics| Prometheus[Prometheus Scraper]
```
