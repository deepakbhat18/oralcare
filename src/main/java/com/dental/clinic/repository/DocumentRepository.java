package com.dental.clinic.repository;

import com.dental.clinic.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByPatientId(Long patientId, Pageable pageable);

    Page<Document> findByPatientIdAndFileType(Long patientId, Document.DocumentType fileType, Pageable pageable);

    long countByPatientId(Long patientId);
}

/*
 * This file contains the DocumentRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
