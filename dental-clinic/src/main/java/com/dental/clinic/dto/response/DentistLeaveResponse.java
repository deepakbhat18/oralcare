package com.dental.clinic.dto.response;

import com.dental.clinic.model.DentistLeave.LeaveStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentistLeaveResponse {
    private Long id;
    private Long dentistId;
    private String dentistName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
}

/*
 * This file contains the DentistLeaveResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
