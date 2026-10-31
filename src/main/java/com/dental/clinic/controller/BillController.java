package com.dental.clinic.controller;

import com.dental.clinic.dto.request.BillRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.BillResponse;
import com.dental.clinic.model.Bill;
import com.dental.clinic.service.BillService;
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
@RequestMapping("/bills")
@RequiredArgsConstructor
@Tag(name = "Bills", description = "Manage patient billing and invoices")
@SecurityRequirement(name = "bearerAuth")
public class BillController {

    private final BillService billService;
    private final com.dental.clinic.service.PdfInvoiceService pdfInvoiceService;

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Download bill invoice PDF")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            Bill bill = billService.getRawBillById(id);
            byte[] pdfBytes = pdfInvoiceService.generateInvoicePdf(bill);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + bill.getInvoiceNumber() + ".pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DENTIST')")
    @Operation(summary = "Get all bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(billService.getAllBills()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<ApiResponse<BillResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(billService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get bills by patient")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(billService.getByPatient(patientId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Get bills by status (PAID, UNPAID, OVERDUE, etc.)")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByStatus(@PathVariable Bill.BillStatus status) {
        return ResponseEntity.ok(ApiResponse.success(billService.getByStatus(status)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Create a new bill")
    public ResponseEntity<ApiResponse<BillResponse>> create(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bill created", billService.createBill(request)));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Mark bill as paid")
    public ResponseEntity<ApiResponse<BillResponse>> markAsPaid(@PathVariable Long id, @RequestParam(required = false) String paymentMethod) {
        return ResponseEntity.ok(ApiResponse.success("Bill marked as paid", billService.markAsPaid(id, paymentMethod)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Update bill status")
    public ResponseEntity<ApiResponse<BillResponse>> updateStatus(@PathVariable Long id, @RequestParam Bill.BillStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", billService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Record a payment or refund against a bill")
    public ResponseEntity<ApiResponse<BillResponse>> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody com.dental.clinic.dto.request.PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", billService.recordPayment(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete bill")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok(ApiResponse.success("Bill deleted", null));
    }
}

/*
 * This file contains the BillController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
