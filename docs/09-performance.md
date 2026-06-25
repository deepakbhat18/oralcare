# 09 - Performance Optimization Guide

This document details database indexing and JPA query tuning strategies to support responsive tenant scoping.

---

## 1. Eliminating JPA N+1 Select Problems
By default, fetching lazy-loaded relations (like `@ManyToOne` fields) inside lists can trigger N additional SQL requests.
- **Solution**: Implement JPQL `JOIN FETCH` or `@EntityGraph` mappings to load child nodes in a single query.
- **Example in Bill Repository**:
  ```java
  @Query("SELECT b FROM Bill b JOIN FETCH b.patient JOIN FETCH b.clinic WHERE b.clinic.id = :clinicId")
  List<Bill> findAllByClinicId(@Param("clinicId") Long clinicId);
  ```

---

## 2. High-Frequency SQL Indexing
To keep query times constant under high multi-tenant load, build database indices over foreign keys and filters:
- `idx_payment_bill`: Speeds up ledger processing (`bill_id`).
- `idx_bill_clinic`: Enhances multi-clinic dashboard and reports scoping.
- `idx_appointment_dentist_time`: Accelerates calendar overlap validation queries.
