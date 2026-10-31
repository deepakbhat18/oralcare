package com.dental.clinic.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoctorNoteRequest {
    @NotNull private Long patientId;
    private Long treatmentId;
    @NotBlank private String noteText;
    private String category;
}

/*
 * This file contains the DoctorNoteRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
