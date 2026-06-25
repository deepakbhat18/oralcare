package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.exception.ConflictException;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.Clinic;
import com.dental.clinic.repository.ClinicRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clinics", description = "Multi-tenant clinic management (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class ClinicController {

    private final ClinicRepository clinicRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all clinics")
    public ResponseEntity<ApiResponse<List<Clinic>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(clinicRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get clinic by ID")
    public ResponseEntity<ApiResponse<Clinic>> getById(@PathVariable Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", id));
        return ResponseEntity.ok(ApiResponse.success(clinic));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new clinic", description = "Automatically generates a unique tenantId for multi-tenant support.")
    public ResponseEntity<ApiResponse<Clinic>> create(@RequestBody Clinic clinic) {
        if (clinic.getTenantId() == null) {
            clinic.setTenantId("tenant-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (clinicRepository.existsByTenantId(clinic.getTenantId())) {
            throw new ConflictException("Tenant ID already exists: " + clinic.getTenantId());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clinic created", clinicRepository.save(clinic)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update clinic details")
    public ResponseEntity<ApiResponse<Clinic>> update(@PathVariable Long id, @RequestBody Clinic updated) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", id));
        clinic.setName(updated.getName());
        clinic.setAddress(updated.getAddress());
        clinic.setContact(updated.getContact());
        clinic.setEmail(updated.getEmail());
        clinic.setWebsite(updated.getWebsite());
        return ResponseEntity.ok(ApiResponse.success("Clinic updated", clinicRepository.save(clinic)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete clinic")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!clinicRepository.existsById(id)) throw new ResourceNotFoundException("Clinic", id);
        clinicRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Clinic deleted", null));
    }
}

/*
 * This file contains the ClinicController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
