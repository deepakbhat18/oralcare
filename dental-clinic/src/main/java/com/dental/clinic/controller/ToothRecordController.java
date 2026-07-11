package com.dental.clinic.controller;

import com.dental.clinic.dto.request.ToothRecordRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.ToothRecordResponse;
import com.dental.clinic.service.ToothRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/odontogram")
@RequiredArgsConstructor
@Tag(name = "Odontogram", description = "Manage interactive dental charting")
@SecurityRequirement(name = "bearerAuth")
public class ToothRecordController {

    private final ToothRecordService toothRecordService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get all tooth records for a patient")
    public ResponseEntity<ApiResponse<List<ToothRecordResponse>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(toothRecordService.getTeethByPatientId(patientId)));
    }

    @PutMapping("/patient/{patientId}/tooth/{toothNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Update or create a tooth record")
    public ResponseEntity<ApiResponse<ToothRecordResponse>> updateTooth(
            @PathVariable Long patientId,
            @PathVariable Integer toothNumber,
            @Valid @RequestBody ToothRecordRequest request,
            @RequestParam Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success("Tooth record updated", 
                toothRecordService.updateTooth(patientId, toothNumber, request, doctorId)));
    }
}
