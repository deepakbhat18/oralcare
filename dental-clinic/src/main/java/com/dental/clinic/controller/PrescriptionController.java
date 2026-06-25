package com.dental.clinic.controller;

import com.dental.clinic.dto.request.PrescriptionRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.PrescriptionResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;




@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Manage patient prescriptions")
@SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final UserRepository      userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get all prescriptions")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','PATIENT')")
    @Operation(summary = "Get prescription by ID")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','PATIENT')")
    @Operation(summary = "Get prescriptions for a patient")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getByPatient(patientId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get prescriptions written by the authenticated dentist")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getMine(Authentication auth) {
        Long doctorId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getByDoctor(doctorId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Create prescription — doctorId resolved from JWT, never hardcoded")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody PrescriptionRequest request,
            Authentication auth) {
        Long doctorId = resolveUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prescription created",
                        prescriptionService.create(request, doctorId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Update prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Prescription updated",
                prescriptionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Delete prescription")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Prescription deleted", null));
    }





    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found in DB: " + email));
        return user.getId();
    }
}

/*
 * This file contains the PrescriptionController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
