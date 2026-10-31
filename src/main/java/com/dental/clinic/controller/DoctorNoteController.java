package com.dental.clinic.controller;

import com.dental.clinic.dto.request.DoctorNoteRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.DoctorNoteResponse;
import com.dental.clinic.service.DoctorNoteService;
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
@RequestMapping("/doctor-notes")
@RequiredArgsConstructor
@Tag(name = "Doctor Notes", description = "Manage clinical notes")
@SecurityRequirement(name = "bearerAuth")
public class DoctorNoteController {

    private final DoctorNoteService doctorNoteService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get all notes for patient")
    public ResponseEntity<ApiResponse<List<DoctorNoteResponse>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(doctorNoteService.getByPatient(patientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get doctor note by ID")
    public ResponseEntity<ApiResponse<DoctorNoteResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorNoteService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Create a doctor note")
    public ResponseEntity<ApiResponse<DoctorNoteResponse>> create(
            @Valid @RequestBody DoctorNoteRequest request,
            @RequestParam Long doctorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note created", doctorNoteService.create(request, doctorId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Update doctor note")
    public ResponseEntity<ApiResponse<DoctorNoteResponse>> update(
            @PathVariable Long id, @Valid @RequestBody DoctorNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Note updated", doctorNoteService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Delete doctor note")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        doctorNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted", null));
    }
}

/*
 * This file contains the DoctorNoteController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
