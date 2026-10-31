package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.DocumentResponse;
import com.dental.clinic.dto.response.PagedResponse;
import com.dental.clinic.model.Document;
import com.dental.clinic.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Secure patient document management")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "List all documents for a patient (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> list(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                documentService.getForPatient(patientId, page, size)));
    }

    @GetMapping("/patient/{patientId}/type/{fileType}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "List patient documents filtered by type (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> listByType(
            @PathVariable Long patientId,
            @PathVariable Document.DocumentType fileType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                documentService.getForPatientByType(patientId, fileType, page, size)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Upload a patient document (PDF, JPEG, PNG, TIFF — max 10 MB)")
    public ResponseEntity<ApiResponse<DocumentResponse>> upload(
            @RequestParam("file")       MultipartFile file,
            @RequestParam("patientId")  Long patientId,
            @RequestParam("fileType")   Document.DocumentType fileType,
            @RequestParam("uploadedBy") Long uploadedById) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document uploaded successfully",
                documentService.upload(file, patientId, fileType, uploadedById)));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Download a document by ID")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Path filePath = documentService.resolveFilePath(id);
        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Soft-delete a document")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}

/*
 * This file contains the DocumentController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
