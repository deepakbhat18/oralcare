package com.dental.clinic.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class BillRequest {
    @NotNull private Long patientId;
    private Long treatmentId;
    @NotNull @Positive private Double amount;
    private Double discount;
    private Double tax;
    private LocalDateTime dueDate;
    private String paymentMethod;
    private String notes;
}

/*
 * This file contains the BillRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
