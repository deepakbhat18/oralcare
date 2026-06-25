package com.dental.clinic.dto.request;
import com.dental.clinic.model.Appointment;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class AppointmentRequest {
    @NotNull private Long patientId;
    @NotNull private Long dentistId;
    @NotBlank private String treatmentType;
    @NotNull private LocalDateTime dateTime;
    private Appointment.AppointmentStatus status;
    private String notes;
    private Integer durationMinutes;
    private String roomNumber;
    private Long clinicId;
}

/*
 * This file contains the AppointmentRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
