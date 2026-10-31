package com.dental.clinic.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TreatmentRecordRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long appointmentId;

    private Long treatmentPlanId;

    @NotBlank(message = "Treatment notes are required")
    private String notes;

    @NotBlank(message = "Procedures performed list is required")
    private String proceduresPerformed;

    @Min(value = 0, message = "Cost must be zero or positive")
    private Double cost;

    private String dentistRemarks;
}

/*
 * This file contains the TreatmentRecordRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
