package com.dental.clinic.controller;

import com.dental.clinic.dto.request.DentistAvailabilityRequest;
import com.dental.clinic.dto.request.DentistLeaveRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.DentistAvailabilityResponse;
import com.dental.clinic.dto.response.DentistLeaveResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.service.DentistScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dentists")
@RequiredArgsConstructor
@Tag(name = "Dentist Schedules", description = "Manage dentist availability, schedules, and leaves")
@SecurityRequirement(name = "bearerAuth")
public class DentistScheduleController {

    private final DentistScheduleService dentistScheduleService;
    private final UserRepository userRepository;

    @PostMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Set availability for the authenticated dentist")
    public ResponseEntity<ApiResponse<DentistAvailabilityResponse>> setAvailability(
            @Valid @RequestBody DentistAvailabilityRequest request, Authentication auth) {
        Long dentistId = resolveUserId(auth);
        request.setDentistId(dentistId);
        return ResponseEntity.ok(ApiResponse.success("Availability updated successfully",
                dentistScheduleService.saveAvailability(request)));
    }

    @GetMapping("/availability/{dentistId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get list of availabilities for a dentist")
    public ResponseEntity<ApiResponse<List<DentistAvailabilityResponse>>> getAvailability(
            @PathVariable Long dentistId) {
        return ResponseEntity.ok(ApiResponse.success(dentistScheduleService.getAvailabilities(dentistId)));
    }

    @DeleteMapping("/availability/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Delete an availability schedule slot")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable Long id) {
        dentistScheduleService.deleteAvailability(id);
        return ResponseEntity.ok(ApiResponse.success("Availability removed successfully", null));
    }

    @PostMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Request a leave (dentist)")
    public ResponseEntity<ApiResponse<DentistLeaveResponse>> requestLeave(
            @Valid @RequestBody DentistLeaveRequest request, Authentication auth) {
        Long dentistId = resolveUserId(auth);
        request.setDentistId(dentistId);
        return ResponseEntity.ok(ApiResponse.success("Leave requested successfully",
                dentistScheduleService.requestLeave(request)));
    }

    @GetMapping("/leaves/{dentistId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get list of leaves for a dentist")
    public ResponseEntity<ApiResponse<List<DentistLeaveResponse>>> getLeaves(
            @PathVariable Long dentistId) {
        return ResponseEntity.ok(ApiResponse.success(dentistScheduleService.getLeaves(dentistId)));
    }

    @PostMapping("/leaves/{leaveId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Approve or Reject a dentist leave request")
    public ResponseEntity<ApiResponse<DentistLeaveResponse>> updateLeaveStatus(
            @PathVariable Long leaveId, @RequestParam String status) {
        DentistLeaveResponse response;
        if ("APPROVED".equalsIgnoreCase(status)) {
            response = dentistScheduleService.approveLeave(leaveId);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            response = dentistScheduleService.rejectLeave(leaveId);
        } else {
            throw new com.dental.clinic.exception.BadRequestException("Invalid status: must be APPROVED or REJECTED");
        }
        return ResponseEntity.ok(ApiResponse.success("Leave status updated successfully", response));
    }

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
        return user.getId();
    }
}

/*
 * This file contains the DentistScheduleController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
