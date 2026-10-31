package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_doc_patient",   columnList = "patient_id"),
        @Index(name = "idx_doc_file_type", columnList = "file_type")
})
@SQLDelete(sql = "UPDATE documents SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false, unique = true)
    private String storedName;

    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType fileType;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @CreatedDate
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public enum DocumentType {
        PRESCRIPTION, REPORT, XRAY, CONSENT_FORM, INSURANCE, OTHER
    }
}

/*
 * This file contains the Document class which manages JPA persistence entities mapping database tables to Java object structures.
 * Concepts used: JPA/ORM annotations, database schema definitions, and Lombok builders.
 */
