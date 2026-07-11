package com.dental.clinic.dto.response;

import com.dental.clinic.model.Appointment;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long patientUserId;
    private Long dentistId;
    private String dentistName;
    private String treatmentType;
    private LocalDateTime dateTime;
    private Appointment.AppointmentStatus status;
    private String notes;
    private Integer durationMinutes;
    private String roomNumber;
    private String allergies;

    private LocalDateTime bookingTime;
    private Integer queuePosition;
    private Integer estimatedWaitMinutes;

    private LocalDateTime createdAt;
    private String patientEmail;
    private String patientContact;
}

/*
 * This file contains the AppointmentResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
