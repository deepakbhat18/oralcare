package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tooth_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"patient_id", "tooth_number"})
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToothRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToothCondition condition;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public enum ToothCondition {
        HEALTHY,
        CAVITY,
        CROWN,
        MISSING,
        FILLING,
        ROOT_CANAL
    }
}
