package com.dental.clinic.service;

import com.dental.clinic.dto.response.DocumentResponse;
import com.dental.clinic.dto.response.PagedResponse;
import com.dental.clinic.exception.FileUploadException;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.Document;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.DocumentRepository;
import com.dental.clinic.repository.PatientRepository;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.security.aspect.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/tiff"
    );
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final PatientRepository  patientRepository;
    private final UserRepository     userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> getForPatient(Long patientId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return PagedResponse.of(
                documentRepository.findByPatientId(patientId, pageable).map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> getForPatientByType(
            Long patientId, Document.DocumentType type, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return PagedResponse.of(
                documentRepository.findByPatientIdAndFileType(patientId, type, pageable).map(this::toResponse)
        );
    }

    @Audit(action = "UPLOAD_DOCUMENT", entityType = "Document")
    @Transactional
    public DocumentResponse upload(MultipartFile file, Long patientId,
                                   Document.DocumentType fileType, Long uploadedById) {
        validateFile(file);

        Patient patient  = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        User    uploader = userRepository.findById(uploadedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", uploadedById));

        String original    = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String storedName  = UUID.randomUUID() + "_" + original;

        Path basePath   = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(storedName).normalize();


        if (!targetPath.startsWith(basePath)) {
            throw new FileUploadException("Invalid file path detected.");
        }

        try {
            Files.createDirectories(basePath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file: " + e.getMessage(), e);
        }

        Document doc = Document.builder()
                .patient(patient)
                .uploadedBy(uploader)
                .originalName(original)
                .storedName(storedName)
                .fileType(fileType)
                .contentType(file.getContentType())
                .filePath(targetPath.toString())
                .fileSize(file.getSize())
                .build();

        return toResponse(documentRepository.save(doc));
    }

    @Audit(action = "DELETE_DOCUMENT", entityType = "Document")
    @Transactional
    public void delete(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException e) {
            log.warn("Could not delete physical file for document {}: {}", documentId, e.getMessage());
        }
        documentRepository.delete(doc);
    }

    public Path resolveFilePath(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return Paths.get(doc.getFilePath());
    }



    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new FileUploadException("File must not be empty.");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new FileUploadException("File exceeds maximum allowed size of 10 MB.");
        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME_TYPES.contains(mime))
            throw new FileUploadException("Unsupported file type: " + mime +
                    ". Allowed: PDF, JPEG, PNG, TIFF.");
        String name = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (name.contains("..")) throw new FileUploadException("Filename contains illegal path sequence.");

        validateMagicBytes(file);
    }

    private void validateMagicBytes(MultipartFile file) {
        try (java.io.InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new FileUploadException("Invalid file header size");
            }


            if (header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
                return;
            }

            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return;
            }

            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return;
            }

            if ((header[0] == 0x49 && header[1] == 0x49 && header[2] == 0x2A && header[3] == 0x00) ||
                (header[0] == 0x4D && header[1] == 0x4D && header[2] == 0x00 && header[3] == 0x2A)) {
                return;
            }

            throw new FileUploadException("Malicious file warning: Magic bytes signature mismatch. The file content does not match its claimed type.");
        } catch (IOException e) {
            throw new FileUploadException("Unable to read file signature for security scan", e);
        }
    }

    private DocumentResponse toResponse(Document d) {
        return DocumentResponse.builder()
                .id(d.getId())
                .patientId(d.getPatient().getId())
                .patientName(d.getPatient().getName())
                .originalName(d.getOriginalName())
                .fileType(d.getFileType())
                .contentType(d.getContentType())
                .fileSize(d.getFileSize())
                .downloadUrl("/api/documents/" + d.getId() + "/download")
                .uploadedAt(d.getUploadedAt())
                .uploadedByName(d.getUploadedBy().getName())
                .build();
    }
}

/*
 * This file contains the DocumentService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
