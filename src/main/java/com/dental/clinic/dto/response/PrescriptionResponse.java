package com.dental.clinic.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.dental.clinic.dto.request.PrescriptionItemDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private Long treatmentId;
    private Long prescribedById;
    private String prescribedByName;
    private String medicineList;
    private String dosageInstructions;
    private Integer durationDays;
    private String notes;
    private boolean active;
    private List<PrescriptionItemDto> items;
    private LocalDateTime createdAt;
}

/*
 * This file contains the PrescriptionResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
