package com.dental.clinic.controller;

import com.dental.clinic.dto.request.PatientRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.PatientResponse;
import com.dental.clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient registration and profile management")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "Get all patients", description = "Returns all patients. Restricted to ADMIN, DENTIST, RECEPTIONIST.")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients() {
        return ResponseEntity.ok(ApiResponse.success(patientService.getAllPatients()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @Parameter(description = "Patient ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id)));
    }

    @GetMapping("/clinic/{clinicId}")
    @Operation(summary = "Get patients by clinic")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getByClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientsByClinic(clinicId)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients by name")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> search(
            @Parameter(description = "Name search term") @RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.success(patientService.searchPatients(name)));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new patient",
            description = "Provide patient details including dateOfBirth (age is auto-calculated)"
    )
    public ResponseEntity<ApiResponse<PatientResponse>> register(@Valid @RequestBody PatientRequest request) {
        PatientResponse patient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient registered successfully", patient));
    }

    @PostMapping
    @Operation(summary = "Create patient (staff)", description = "Create a patient record. For staff use.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> create(@Valid @RequestBody PatientRequest request) {
        PatientResponse patient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient created successfully", patient));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DENTIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Patient updated", patientService.updatePatient(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted", null));
    }
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientByUserId(userId)));
    }
}

/*
 * This file contains the PatientController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
