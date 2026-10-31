package com.dental.clinic.dto.response;

import com.dental.clinic.model.Document;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentResponse {
    private Long             id;
    private Long             patientId;
    private String           patientName;
    private String           originalName;
    private Document.DocumentType fileType;
    private String           contentType;
    private Long             fileSize;
    private String           downloadUrl;
    private LocalDateTime    uploadedAt;
    private String           uploadedByName;
}

/*
 * This file contains the DocumentResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
