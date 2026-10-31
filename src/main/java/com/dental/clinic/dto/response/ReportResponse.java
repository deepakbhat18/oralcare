package com.dental.clinic.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String reportText;
    private String reportType;
    private String description;
    private LocalDateTime uploadedAt;
}

/*
 * This file contains the ReportResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
