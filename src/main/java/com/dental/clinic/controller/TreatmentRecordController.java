package com.dental.clinic.controller;

import com.dental.clinic.dto.request.TreatmentRecordRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.TreatmentRecordResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.service.TreatmentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/treatment-records")
@RequiredArgsConstructor
@Tag(name = "Treatment Records", description = "Manage dental treatment history records")
@SecurityRequirement(name = "bearerAuth")
public class TreatmentRecordController {

    private final TreatmentRecordService treatmentRecordService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get all treatment records (paginated)")
    public ResponseEntity<ApiResponse<Page<TreatmentRecordResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(treatmentRecordService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get treatment record by ID")
    public ResponseEntity<ApiResponse<TreatmentRecordResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(treatmentRecordService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get treatment records for a patient (paginated)")
    public ResponseEntity<ApiResponse<Page<TreatmentRecordResponse>>> getByPatient(
            @PathVariable Long patientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(treatmentRecordService.getByPatient(patientId, pageable)));
    }

    @GetMapping("/dentist/{dentistId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get treatment records performed by a dentist (paginated)")
    public ResponseEntity<ApiResponse<Page<TreatmentRecordResponse>>> getByDentist(
            @PathVariable Long dentistId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(treatmentRecordService.getByDentist(dentistId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Create treatment record — dentistId resolved from token")
    public ResponseEntity<ApiResponse<TreatmentRecordResponse>> create(
            @Valid @RequestBody TreatmentRecordRequest request, Authentication auth) {
        Long dentistId = resolveUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Treatment record created successfully",
                        treatmentRecordService.create(request, dentistId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Update treatment record")
    public ResponseEntity<ApiResponse<TreatmentRecordResponse>> update(
            @PathVariable Long id, @Valid @RequestBody TreatmentRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Treatment record updated successfully",
                treatmentRecordService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Delete treatment record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        treatmentRecordService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Treatment record deleted successfully", null));
    }

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
        return user.getId();
    }
}

/*
 * This file contains the TreatmentRecordController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
