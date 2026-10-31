package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.ReportResponse;
import com.dental.clinic.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Upload, view, and manage patient medical reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Patient views their own reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getMyReports()));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get all reports for a patient")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getByPatient(patientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<ApiResponse<ReportResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getById(id)));
    }

    @PostMapping(value = "/upload/{patientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Upload a file report for a patient (PDF/image, max 10MB)")
    public ResponseEntity<ApiResponse<ReportResponse>> upload(
            @PathVariable Long patientId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String description) {
        ReportResponse report = reportService.uploadReport(patientId, doctorId, file, reportType, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report uploaded successfully", report));
    }

    @PostMapping("/text/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Submit a text/clinical report for a patient")
    public ResponseEntity<ApiResponse<ReportResponse>> submitTextReport(
            @PathVariable Long patientId,
            @RequestBody Map<String, String> body) {
        Long doctorId = body.get("doctorId") != null ? Long.parseLong(body.get("doctorId")) : null;
        ReportResponse report = reportService.submitTextReport(
                patientId, doctorId, body.get("reportText"), body.get("reportType"), body.get("description"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Text report created successfully", report));
    }

    @GetMapping("/download/{patientId}/{fileName}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Download a report file")
    public ResponseEntity<Resource> download(@PathVariable Long patientId, @PathVariable String fileName) {
        Resource resource = reportService.downloadReport(patientId, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Delete a report")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted", null));
    }
    @GetMapping("/patient/{patientId}/export")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Export patient summary report as PDF, CSV, or JSON")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "JSON") String format) {
        byte[] data = reportService.exportPatientSummary(patientId, format.toUpperCase());
        String contentType = switch (format.toUpperCase()) {
            case "PDF"  -> "application/pdf";
            case "CSV"  -> "text/csv";
            default     -> "application/json";
        };
        String ext = format.toLowerCase();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"patient_" + patientId + "_report." + ext + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}

/*
 * This file contains the ReportController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
