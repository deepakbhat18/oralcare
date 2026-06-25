package com.dental.clinic.service;

import com.dental.clinic.dto.request.DoctorNoteRequest;
import com.dental.clinic.dto.response.DoctorNoteResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorNoteService {

    private final DoctorNoteRepository    doctorNoteRepository;
    private final PatientRepository       patientRepository;
    private final UserRepository          userRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;

    public List<DoctorNoteResponse> getByPatient(Long patientId) {
        return doctorNoteRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public DoctorNoteResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public DoctorNoteResponse create(DoctorNoteRequest req, Long doctorId) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", req.getPatientId()));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        DoctorNote.DoctorNoteBuilder builder = DoctorNote.builder()
                .patient(patient)
                .doctor(doctor)
                .noteText(req.getNoteText())
                .category(req.getCategory());

        if (req.getTreatmentId() != null) {
            treatmentPlanRepository.findById(req.getTreatmentId()).ifPresent(builder::treatment);
        }

        return mapToResponse(doctorNoteRepository.save(builder.build()));
    }

    @Transactional
    public DoctorNoteResponse update(Long id, DoctorNoteRequest req) {
        DoctorNote note = findById(id);
        note.setNoteText(req.getNoteText());
        note.setCategory(req.getCategory());
        return mapToResponse(doctorNoteRepository.save(note));
    }

    @Transactional
    public void delete(Long id) {
        if (!doctorNoteRepository.existsById(id)) throw new ResourceNotFoundException("DoctorNote", id);
        doctorNoteRepository.deleteById(id);
    }

    private DoctorNote findById(Long id) {
        return doctorNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorNote", id));
    }

    public DoctorNoteResponse mapToResponse(DoctorNote n) {
        return DoctorNoteResponse.builder()
                .id(n.getId())
                .patientId(n.getPatient().getId())
                .patientName(n.getPatient().getName())
                .treatmentId(n.getTreatment() != null ? n.getTreatment().getId() : null)
                .doctorId(n.getDoctor().getId())
                .doctorName(n.getDoctor().getName())
                .noteText(n.getNoteText())
                .category(n.getCategory())
                .flagged(n.isFlagged())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

/*
 * This file contains the DoctorNoteService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
