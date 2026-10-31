package com.dental.clinic.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Double amount;
    private String paymentMethod;
    private String transactionReference;
    private boolean refund;
    private String notes;
    private LocalDateTime paidAt;
}

/*
 * This file contains the PaymentResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
