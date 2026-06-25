package com.dental.clinic.service;

import com.dental.clinic.dto.response.ReportResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    public List<ReportResponse> getByPatient(Long patientId) {
        enforcePatientAccess(patientId);
        return reportRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ReportResponse> getMyReports() {
        User currentUser = getCurrentUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return reportRepository.findByPatientIdOrderByUploadedAtDesc(patient.getId()).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReportResponse getById(Long id) {
        Report report = findById(id);
        enforcePatientAccess(report.getPatient().getId());
        return mapToResponse(report);
    }

    @Transactional
    public ReportResponse uploadReport(Long patientId, Long doctorId, MultipartFile file,
                                       String reportType, String description) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        User doctor = resolveDoctor(doctorId);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir, "reports", String.valueOf(patientId));
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/api/reports/download/" + patientId + "/" + storedFileName;

            Report report = Report.builder()
                    .patient(patient).doctor(doctor)
                    .fileUrl(fileUrl).fileName(originalFilename)
                    .fileType(file.getContentType()).fileSize(file.getSize())
                    .reportType(reportType).description(description)
                    .build();

            report = reportRepository.save(report);
            log.info("File report uploaded for patient {}: {}", patientId, originalFilename);
            return mapToResponse(report);

        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Transactional
    public ReportResponse submitTextReport(Long patientId, Long doctorId,
                                           String reportText, String reportType, String description) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        if (reportText == null || reportText.isBlank()) {
            throw new BadRequestException("Report text cannot be empty");
        }

        User doctor = resolveDoctor(doctorId);

        Report report = Report.builder()
                .patient(patient).doctor(doctor)
                .reportText(reportText).reportType(reportType).description(description)
                .build();

        report = reportRepository.save(report);
        log.info("Text report created for patient {}", patientId);
        return mapToResponse(report);
    }

    public Resource downloadReport(Long patientId, String fileName) {
        enforcePatientAccess(patientId);
        try {
            Path filePath = Paths.get(uploadDir, "reports", String.valueOf(patientId), fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            throw new ResourceNotFoundException("File not found: " + fileName);
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file path");
        }
    }

    @Transactional
    public void deleteReport(Long id) {
        Report report = findById(id);
        if (report.getFileUrl() != null) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(
                        report.getFileUrl().replace("/api/reports/download/", "reports/"));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Could not delete physical file: {}", e.getMessage());
            }
        }
        reportRepository.deleteById(id);
    }

    private Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new BadRequestException("Not authenticated");
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User resolveDoctor(Long doctorId) {
        if (doctorId != null) {
            return userRepository.findById(doctorId).orElse(null);
        }
        try { return getCurrentUser(); } catch (Exception e) { return null; }
    }

    private void enforcePatientAccess(Long patientId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == User.Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            if (!patient.getId().equals(patientId)) {
                throw new BadRequestException("Access denied: you can only view your own reports");
            }
        }
    }

    public ReportResponse mapToResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getName())
                .doctorId(r.getDoctor() != null ? r.getDoctor().getId() : null)
                .doctorName(r.getDoctor() != null ? r.getDoctor().getName() : null)
                .fileUrl(r.getFileUrl())
                .fileName(r.getFileName())
                .fileType(r.getFileType())
                .fileSize(r.getFileSize())
                .reportText(r.getReportText())
                .reportType(r.getReportType())
                .description(r.getDescription())
                .uploadedAt(r.getUploadedAt())
                .build();
    }

    public byte[] exportPatientSummary(Long patientId, String format) {
        enforcePatientAccess(patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        List<Report> reports = reportRepository.findByPatientIdOrderByUploadedAtDesc(patientId);

        return switch (format) {
            case "CSV"  -> buildCsv(patient, reports).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            case "PDF"  -> buildSimplePdf(patient, reports);
            default     -> buildJson(patient, reports).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        };
    }

    private String buildJson(Patient patient, List<Report> reports) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            java.util.Map<String, Object> summary = java.util.Map.of(
                    "patient", java.util.Map.of("id", patient.getId(), "name", patient.getName(),
                            "email", patient.getEmail() != null ? patient.getEmail() : ""),
                    "reports", reports.stream().map(this::mapToResponse).collect(Collectors.toList())
            );
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
        } catch (Exception e) { throw new BadRequestException("JSON export failed: " + e.getMessage()); }
    }

    private String buildCsv(Patient patient, List<Report> reports) {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient ID,Patient Name,Report ID,Type,Description,Doctor,Date\n");
        for (Report r : reports) {
            sb.append(patient.getId()).append(",")
                    .append(patient.getName()).append(",")
                    .append(r.getId()).append(",")
                    .append(r.getReportType() != null ? r.getReportType() : "").append(",")
                    .append(r.getDescription() != null ? r.getDescription().replace(",", ";") : "").append(",")
                    .append(r.getDoctor() != null ? r.getDoctor().getName() : "").append(",")
                    .append(r.getUploadedAt() != null ? r.getUploadedAt().toLocalDate() : "").append("\n");
        }
        return sb.toString();
    }

    private byte[] buildSimplePdf(Patient patient, List<Report> reports) {

        String text = "Patient Report Summary\n" +
                "Patient: " + patient.getName() + " (ID: " + patient.getId() + ")\n" +
                "Email: " + (patient.getEmail() != null ? patient.getEmail() : "N/A") + "\n\n" +
                "Reports (" + reports.size() + "):\n" +
                reports.stream().map(r ->
                        "  - [" + r.getId() + "] " + (r.getReportType() != null ? r.getReportType() : "Report") +
                                " | " + (r.getDescription() != null ? r.getDescription() : "") +
                                " | " + (r.getUploadedAt() != null ? r.getUploadedAt().toLocalDate() : "")
                ).collect(Collectors.joining("\n"));
        return text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}

/*
 * This file contains the ReportService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
