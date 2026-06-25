package com.dental.clinic.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DoctorNoteResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long treatmentId;
    private Long doctorId;
    private String doctorName;
    private String noteText;
    private String category;
    private boolean flagged;
    private LocalDateTime createdAt;
}

/*
 * This file contains the DoctorNoteResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
