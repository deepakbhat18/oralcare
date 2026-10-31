package com.dental.clinic.controller;

import com.dental.clinic.dto.request.TreatmentPlanRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.TreatmentPlanResponse;
import com.dental.clinic.model.TreatmentPlan;
import com.dental.clinic.service.TreatmentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/treatment-plans")
@RequiredArgsConstructor
@Tag(name = "Treatment Plans", description = "Manage dental treatment plans and session tracking")
@SecurityRequirement(name = "bearerAuth")
public class TreatmentPlanController {

    private final TreatmentPlanService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get all treatment plans")
    public ResponseEntity<ApiResponse<List<TreatmentPlanResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get treatment plan by ID")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get treatment plans for a patient")
    public ResponseEntity<ApiResponse<List<TreatmentPlanResponse>>> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByPatient(patientId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Create a treatment plan")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> create(
            @Valid @RequestBody TreatmentPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Treatment plan created", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Update treatment plan")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> update(
            @PathVariable Long id, @Valid @RequestBody TreatmentPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Treatment plan updated", service.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Update treatment plan status")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam TreatmentPlan.TreatmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", service.updateStatus(id, status)));
    }

    @PatchMapping("/{id}/complete-session")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Mark next session as completed",
            description = "Increments completedSessions by 1. Auto-sets status to COMPLETED when all done.")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Session marked complete", service.incrementCompletedSessions(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete treatment plan")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Treatment plan deleted", null));
    }
}

/*
 * This file contains the TreatmentPlanController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
