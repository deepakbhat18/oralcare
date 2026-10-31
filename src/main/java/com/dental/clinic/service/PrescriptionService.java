package com.dental.clinic.service;

import com.dental.clinic.dto.request.PrescriptionRequest;
import com.dental.clinic.dto.request.PrescriptionItemDto;
import com.dental.clinic.dto.response.PrescriptionResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import com.dental.clinic.security.aspect.Audit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository  prescriptionRepository;
    private final PatientRepository       patientRepository;
    private final UserRepository          userRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;

    public List<PrescriptionResponse> getAll() {
        return prescriptionRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public PrescriptionResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    public List<PrescriptionResponse> getByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PrescriptionResponse> getByDoctor(Long doctorId) {
        return prescriptionRepository.findByPrescribedById(doctorId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    @Audit(action = "CREATE_PRESCRIPTION", entityType = "Prescription")
    public PrescriptionResponse create(PrescriptionRequest req, Long doctorId) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", req.getPatientId()));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        List<PrescriptionItem> items = List.of();
        if (req.getItems() != null) {
            items = req.getItems().stream()
                    .map(i -> PrescriptionItem.builder()
                            .medicineName(i.getMedicineName())
                            .dosage(i.getDosage())
                            .frequency(i.getFrequency())
                            .duration(i.getDuration())
                            .instructions(i.getInstructions())
                            .build())
                    .collect(Collectors.toList());
        }

        Prescription.PrescriptionBuilder builder = Prescription.builder()
                .patient(patient)
                .prescribedBy(doctor)
                .medicineList(req.getMedicineList())
                .dosageInstructions(req.getDosageInstructions())
                .durationDays(req.getDurationDays())
                .notes(req.getNotes())
                .items(items)
                .active(true);

        if (req.getTreatmentId() != null) {
            treatmentPlanRepository.findById(req.getTreatmentId())
                    .ifPresent(builder::treatment);
        }

        return mapToResponse(prescriptionRepository.save(builder.build()));
    }

    @Transactional
    @Audit(action = "UPDATE_PRESCRIPTION", entityType = "Prescription")
    public PrescriptionResponse update(Long id, PrescriptionRequest req) {
        Prescription p = findById(id);

        if (req.getItems() != null) {
            List<PrescriptionItem> items = req.getItems().stream()
                    .map(i -> PrescriptionItem.builder()
                            .medicineName(i.getMedicineName())
                            .dosage(i.getDosage())
                            .frequency(i.getFrequency())
                            .duration(i.getDuration())
                            .instructions(i.getInstructions())
                            .build())
                    .collect(Collectors.toList());
            p.setItems(items);
        }

        p.setMedicineList(req.getMedicineList());
        p.setDosageInstructions(req.getDosageInstructions());
        p.setDurationDays(req.getDurationDays());
        p.setNotes(req.getNotes());

        return mapToResponse(prescriptionRepository.save(p));
    }

    @Transactional
    @Audit(action = "DELETE_PRESCRIPTION", entityType = "Prescription")
    public void delete(Long id) {
        if (!prescriptionRepository.existsById(id))
            throw new ResourceNotFoundException("Prescription", id);
        prescriptionRepository.deleteById(id);
    }

    private Prescription findById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
    }

    public PrescriptionResponse mapToResponse(Prescription p) {
        List<PrescriptionItemDto> items = List.of();
        if (p.getItems() != null) {
            items = p.getItems().stream()
                    .map(i -> PrescriptionItemDto.builder()
                            .medicineName(i.getMedicineName())
                            .dosage(i.getDosage())
                            .frequency(i.getFrequency())
                            .duration(i.getDuration())
                            .instructions(i.getInstructions())
                            .build())
                    .collect(Collectors.toList());
        }

        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getName())
                .treatmentId(p.getTreatment() != null ? p.getTreatment().getId() : null)
                .prescribedById(p.getPrescribedBy() != null ? p.getPrescribedBy().getId() : null)
                .prescribedByName(p.getPrescribedBy() != null ? p.getPrescribedBy().getName() : null)
                .medicineList(p.getMedicineList())
                .dosageInstructions(p.getDosageInstructions())
                .durationDays(p.getDurationDays())
                .notes(p.getNotes())
                .active(p.isActive())
                .items(items)
                .createdAt(p.getCreatedAt())
                .build();
    }
}

/*
 * This file contains the PrescriptionService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
