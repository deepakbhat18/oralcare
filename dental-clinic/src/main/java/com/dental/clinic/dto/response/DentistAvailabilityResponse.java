package com.dental.clinic.dto.response;

import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentistAvailabilityResponse {
    private Long id;
    private Long dentistId;
    private String dentistName;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}

/*
 * This file contains the DentistAvailabilityResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
