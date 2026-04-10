package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_apt_patient",  columnList = "patient_id"),
        @Index(name = "idx_apt_dentist",  columnList = "dentist_id"),
        @Index(name = "idx_apt_datetime", columnList = "date_time"),
        @Index(name = "idx_apt_booking",  columnList = "booking_time")
})
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false)
    private User dentist;

    @Column(name = "treatment_type", nullable = false)
    private String treatmentType;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "predicted_queue_time")
    private Double predictedQueueTime;

    @Column(name = "predicted_treatment_duration")
    private Double predictedTreatmentDuration;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AppointmentStatus {
        SCHEDULED, CONFIRMED, WAITING, IN_PROGRESS, COMPLETED,
        CANCELLED, NO_SHOW, RESCHEDULED
    }
}