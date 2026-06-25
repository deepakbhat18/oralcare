package com.dental.clinic.controller;

import com.dental.clinic.dto.request.AppointmentRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.AppointmentResponse;
import com.dental.clinic.model.Appointment;
import com.dental.clinic.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "FCFS-based appointment booking and queue management")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get all appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAllAppointments()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get appointments by patient — includes queuePosition and estimatedWaitMinutes")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getByPatient(patientId)));
    }

    @GetMapping("/dentist/{dentistId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get appointments by dentist")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDentist(@PathVariable Long dentistId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getByDentist(dentistId)));
    }

    @GetMapping("/dentist/{dentistId}/queue")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get live FCFS queue for a dentist today",
            description = "Returns WAITING/IN_PROGRESS/SCHEDULED appointments sorted by queuePosition. " +
                    "Use this endpoint for the receptionist queue management screen.")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getLiveQueue(@PathVariable Long dentistId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getLiveQueue(dentistId)));
    }
    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get available 30-min slots for a dentist on a given date")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableSlots(
            @RequestParam Long dentistId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAvailableSlots(dentistId, date)));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get upcoming appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getUpcoming() {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getUpcoming()));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get appointments by date range")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getByDateRange(start, end)));
    }

    @PostMapping("/book")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT','DENTIST')")
    @Operation(summary = "Book appointment (FCFS)",
            description = "Books the appointment, assigns a FCFS queue position, and returns estimatedWaitMinutes. " +
                    "Multiple patients CAN book the same slot — they are served FCFS by bookingTime.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", appointmentService.bookAppointment(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DENTIST')")
    @Operation(summary = "Update appointment details")
    public ResponseEntity<ApiResponse<AppointmentResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment updated", appointmentService.updateAppointment(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Update appointment status",
            description = "WAITING → IN_PROGRESS → COMPLETED. Queue positions recalculate on COMPLETED/CANCELLED.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Appointment.AppointmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", appointmentService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Delete appointment — queue recalculates automatically")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment deleted", null));
    }
}

/*
 * This file contains the AppointmentController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
