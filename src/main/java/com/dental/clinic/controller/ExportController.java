package com.dental.clinic.controller;

import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Bill;
import com.dental.clinic.repository.AppointmentRepository;
import com.dental.clinic.repository.BillRepository;
import com.dental.clinic.security.TenantContext;
import com.dental.clinic.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
@Tag(name = "Exports", description = "Export data to Excel and CSV format")
@SecurityRequirement(name = "bearerAuth")
public class ExportController {

    private final ExportService exportService;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;

    @GetMapping("/appointments/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DENTIST', 'RECEPTIONIST')")
    @Operation(summary = "Export appointments list to Excel")
    public ResponseEntity<byte[]> exportAppointmentsExcel(@RequestParam(required = false) Long clinicId) throws IOException {
        Long tenantClinicId = TenantContext.getCurrentTenant();
        List<Appointment> appointments;
        if (tenantClinicId != null) {
            appointments = appointmentRepository.findByClinicId(tenantClinicId);
        } else if (clinicId != null) {
            appointments = appointmentRepository.findByClinicId(clinicId);
        } else {
            appointments = appointmentRepository.findAll();
        }

        byte[] excelBytes = exportService.exportAppointmentsToExcel(appointments);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=appointments.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/appointments/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'DENTIST', 'RECEPTIONIST')")
    @Operation(summary = "Export appointments list to CSV")
    public ResponseEntity<String> exportAppointmentsCsv(@RequestParam(required = false) Long clinicId) {
        Long tenantClinicId = TenantContext.getCurrentTenant();
        List<Appointment> appointments;
        if (tenantClinicId != null) {
            appointments = appointmentRepository.findByClinicId(tenantClinicId);
        } else if (clinicId != null) {
            appointments = appointmentRepository.findByClinicId(clinicId);
        } else {
            appointments = appointmentRepository.findAll();
        }

        String csvData = exportService.exportAppointmentsToCsv(appointments);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=appointments.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvData);
    }

    @GetMapping("/revenue/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Export revenue report to Excel")
    public ResponseEntity<byte[]> exportRevenueExcel(@RequestParam(required = false) Long clinicId) throws IOException {
        Long tenantClinicId = TenantContext.getCurrentTenant();
        List<Bill> bills;
        if (tenantClinicId != null) {
            bills = billRepository.findByClinicId(tenantClinicId);
        } else if (clinicId != null) {
            bills = billRepository.findByClinicId(clinicId);
        } else {
            bills = billRepository.findAll();
        }

        byte[] excelBytes = exportService.exportRevenueToExcel(bills);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/revenue/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Export revenue report to CSV")
    public ResponseEntity<String> exportRevenueCsv(@RequestParam(required = false) Long clinicId) {
        Long tenantClinicId = TenantContext.getCurrentTenant();
        List<Bill> bills;
        if (tenantClinicId != null) {
            bills = billRepository.findByClinicId(tenantClinicId);
        } else if (clinicId != null) {
            bills = billRepository.findByClinicId(clinicId);
        } else {
            bills = billRepository.findAll();
        }

        String csvData = exportService.exportRevenueToCsv(bills);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue_report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvData);
    }
}

/*
 * This file contains the ExportController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
