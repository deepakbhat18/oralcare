package com.dental.clinic.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class TreatmentPlanRequest {
    @NotNull private Long patientId;
    @NotNull private Long dentistId;
    @NotBlank private String planName;
    private String description;
    private List<String> sessions;
    private Double estimatedCost;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalSessions;
}

/*
 * This file contains the TreatmentPlanRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
