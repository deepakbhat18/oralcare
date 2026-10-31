package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private TreatmentPlan treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescribed_by")
    private User prescribedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @Column(name = "medicine_list", columnDefinition = "TEXT")
    private String medicineList;

    @Column(name = "dosage_instructions", columnDefinition = "TEXT")
    private String dosageInstructions;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "prescription_items", joinColumns = @JoinColumn(name = "prescription_id"))
    @Builder.Default
    private java.util.List<PrescriptionItem> items = new java.util.ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void syncMedicineList() {
        if (items != null && !items.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (PrescriptionItem item : items) {
                sb.append(item.getMedicineName())
                  .append(" (").append(item.getDosage())
                  .append(", ").append(item.getFrequency())
                  .append(", ").append(item.getDuration()).append(")");
                if (item.getInstructions() != null && !item.getInstructions().isBlank()) {
                    sb.append(" - ").append(item.getInstructions());
                }
                sb.append("\n");
            }
            this.medicineList = sb.toString().trim();
        }
    }
}

/*
 * This file contains the Prescription class which manages JPA persistence entities mapping database tables to Java object structures.
 * Concepts used: JPA/ORM annotations, database schema definitions, and Lombok builders.
 */
