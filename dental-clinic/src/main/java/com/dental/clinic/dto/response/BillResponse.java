package com.dental.clinic.dto.response;

import com.dental.clinic.model.Bill;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long treatmentId;
    private Double amount;
    private Double discount;
    private Double tax;
    private Double totalAmount;
    private Bill.BillStatus status;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private String paymentMethod;
    private String notes;
    private String invoiceNumber;
    private LocalDateTime createdAt;
    private List<PaymentResponse> payments;
    private Double paidAmount;


    public Double getSubtotal() { return amount; }
    public Double getDiscountAmount() { return discount; }
    public Double getTaxAmount() { return tax; }
    public Double getFinalAmount() { return totalAmount; }
}

/*
 * This file contains the BillResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
