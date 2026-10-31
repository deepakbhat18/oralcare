package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "bills")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private TreatmentPlan treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "discount")
    @Builder.Default
    private Double discount = 0.0;

    @Column(name = "tax")
    @Builder.Default
    private Double tax = 0.0;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BillStatus status = BillStatus.UNPAID;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BillStatus {
        UNPAID, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED, REFUNDED
    }

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (amount != null) {
            double disc = discount != null ? discount : 0.0;
            double taxAmt = tax != null ? tax : 0.0;
            this.totalAmount = (amount - disc) + taxAmt;
        }
    }

    public Double getPaidAmount() {
        if (payments == null) return 0.0;
        return payments.stream()
                .mapToDouble(p -> p.isRefund() ? -p.getAmount() : p.getAmount())
                .sum();
    }
}

/*
 * This file contains the Bill class which manages JPA persistence entities mapping database tables to Java object structures.
 * Concepts used: JPA/ORM annotations, database schema definitions, and Lombok builders.
 */
