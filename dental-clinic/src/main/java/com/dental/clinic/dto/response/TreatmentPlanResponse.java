package com.dental.clinic.dto.response;
import com.dental.clinic.model.TreatmentPlan;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TreatmentPlanResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long dentistId;
    private String dentistName;
    private String planName;
    private String description;
    private List<String> sessions;
    private TreatmentPlan.TreatmentStatus status;
    private Double estimatedCost;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalSessions;
    private Integer completedSessions;
    private LocalDateTime createdAt;
}

/*
 * This file contains the TreatmentPlanResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
