package com.dental.clinic.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private Long dentistId;
    private AppointmentResponse activeAppointment;
    private List<AppointmentResponse> waitingQueue;
    private int totalWaiting;
    private int totalEstimatedWaitTimeMinutes;
}

/*
 * This file contains the QueueStatusResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
