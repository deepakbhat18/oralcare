package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.TimelineEventResponse;
import com.dental.clinic.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
@Tag(name = "Timeline", description = "Aggregate chronological patient history events")
@SecurityRequirement(name = "bearerAuth")
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get aggregate chronological health events timeline for a patient")
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getPatientTimeline(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(timelineService.getPatientTimeline(patientId)));
    }
}

/*
 * This file contains the TimelineController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
