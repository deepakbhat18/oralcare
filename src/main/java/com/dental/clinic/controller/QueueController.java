package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.AppointmentResponse;
import com.dental.clinic.dto.response.QueueStatusResponse;
import com.dental.clinic.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@Tag(name = "Queue Management", description = "Real-time queue check-in, service, and status tracking")
@SecurityRequirement(name = "bearerAuth")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/check-in/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DENTIST')")
    @Operation(summary = "Check in a patient on arrival (moves status to WAITING)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> checkIn(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Patient checked in", queueService.checkIn(appointmentId)));
    }

    @PostMapping("/serve/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DENTIST')")
    @Operation(summary = "Serve patient (moves status to IN_PROGRESS, completes previously active one)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> serve(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Patient is now being served", queueService.serve(appointmentId)));
    }

    @GetMapping("/status/{dentistId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DENTIST','PATIENT')")
    @Operation(summary = "Get the active queue and estimated wait time status dashboard for a dentist")
    public ResponseEntity<ApiResponse<QueueStatusResponse>> getQueueStatus(@PathVariable Long dentistId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.getQueueStatus(dentistId)));
    }
}

/*
 * This file contains the QueueController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
