package com.dental.clinic.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class PrescriptionRequest {
    @NotNull private Long patientId;
    private Long treatmentId;
    private String medicineList;
    private String dosageInstructions;
    private Integer durationDays;
    private String notes;
    private Long doctorId;
    private List<PrescriptionItemDto> items;
}

/*
 * This file contains the PrescriptionRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
