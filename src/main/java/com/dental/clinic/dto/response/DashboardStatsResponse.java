package com.dental.clinic.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsResponse {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointmentsToday;
    private long totalAppointmentsCompleted;
    private long totalAppointmentsCancelled;
    private long totalAppointmentsUpcoming;
    private long totalAppointmentsNoShow;
    private long totalDocumentsUploaded;
    private long unreadNotifications;
    private long pendingBills;
}

/*
 * This file contains the DashboardStatsResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
