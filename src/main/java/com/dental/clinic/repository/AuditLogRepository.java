package com.dental.clinic.repository;

import com.dental.clinic.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByUsernameAndEntityType(String username, String entityType, Pageable pageable);
}

/*
 * This file contains the AuditLogRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
