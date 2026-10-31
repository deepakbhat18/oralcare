package com.dental.clinic.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentRecordResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private Long treatmentPlanId;
    private String treatmentPlanName;
    private Long dentistId;
    private String dentistName;
    private String notes;
    private String proceduresPerformed;
    private Double cost;
    private String dentistRemarks;
    private LocalDateTime createdAt;
}

/*
 * This file contains the TreatmentRecordResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
