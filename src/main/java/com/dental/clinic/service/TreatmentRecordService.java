package com.dental.clinic.service;

import com.dental.clinic.dto.request.TreatmentRecordRequest;
import com.dental.clinic.dto.response.TreatmentRecordResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import com.dental.clinic.security.aspect.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentRecordService {

    private final TreatmentRecordRepository treatmentRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final UserRepository userRepository;

    public Page<TreatmentRecordResponse> getAll(Pageable pageable) {
        return treatmentRecordRepository.findAll(pageable).map(this::mapToResponse);
    }

    public TreatmentRecordResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    public Page<TreatmentRecordResponse> getByPatient(Long patientId, Pageable pageable) {
        return treatmentRecordRepository.findByPatientId(patientId, pageable).map(this::mapToResponse);
    }

    public Page<TreatmentRecordResponse> getByDentist(Long dentistId, Pageable pageable) {
        return treatmentRecordRepository.findByDentistId(dentistId, pageable).map(this::mapToResponse);
    }

    @Transactional
    @Audit(action = "CREATE_TREATMENT_RECORD", entityType = "TreatmentRecord")
    @CacheEvict(value = "analytics", allEntries = true)
    public TreatmentRecordResponse create(TreatmentRecordRequest request, Long dentistId) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        User dentist = userRepository.findById(dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist", dentistId));

        TreatmentRecord record = TreatmentRecord.builder()
                .patient(patient)
                .dentist(dentist)
                .notes(request.getNotes())
                .proceduresPerformed(request.getProceduresPerformed())
                .cost(request.getCost() != null ? request.getCost() : 0.0)
                .dentistRemarks(request.getDentistRemarks())
                .build();

        if (request.getAppointmentId() != null) {
            appointmentRepository.findById(request.getAppointmentId())
                    .ifPresent(record::setAppointment);
        }

        if (request.getTreatmentPlanId() != null) {
            treatmentPlanRepository.findById(request.getTreatmentPlanId())
                    .ifPresent(record::setTreatmentPlan);
        }

        TreatmentRecord saved = treatmentRecordRepository.save(record);
        log.info("Treatment record created for patient: {}, recordId: {}", patient.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    @Audit(action = "UPDATE_TREATMENT_RECORD", entityType = "TreatmentRecord")
    @CacheEvict(value = "analytics", allEntries = true)
    public TreatmentRecordResponse update(Long id, TreatmentRecordRequest request) {
        TreatmentRecord record = findById(id);
        record.setNotes(request.getNotes());
        record.setProceduresPerformed(request.getProceduresPerformed());
        record.setCost(request.getCost() != null ? request.getCost() : 0.0);
        record.setDentistRemarks(request.getDentistRemarks());

        if (request.getAppointmentId() != null) {
            appointmentRepository.findById(request.getAppointmentId())
                    .ifPresent(record::setAppointment);
        } else {
            record.setAppointment(null);
        }

        if (request.getTreatmentPlanId() != null) {
            treatmentPlanRepository.findById(request.getTreatmentPlanId())
                    .ifPresent(record::setTreatmentPlan);
        } else {
            record.setTreatmentPlan(null);
        }

        TreatmentRecord saved = treatmentRecordRepository.save(record);
        log.info("Treatment record updated, recordId: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    @Audit(action = "DELETE_TREATMENT_RECORD", entityType = "TreatmentRecord")
    @CacheEvict(value = "analytics", allEntries = true)
    public void delete(Long id) {
        TreatmentRecord record = findById(id);
        treatmentRecordRepository.delete(record);
        log.info("Treatment record deleted, recordId: {}", id);
    }

    private TreatmentRecord findById(Long id) {
        return treatmentRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TreatmentRecord", id));
    }

    private TreatmentRecordResponse mapToResponse(TreatmentRecord r) {
        return TreatmentRecordResponse.builder()
                .id(r.getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getName())
                .appointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null)
                .treatmentPlanId(r.getTreatmentPlan() != null ? r.getTreatmentPlan().getId() : null)
                .treatmentPlanName(r.getTreatmentPlan() != null ? r.getTreatmentPlan().getPlanName() : null)
                .dentistId(r.getDentist().getId())
                .dentistName(r.getDentist().getName())
                .notes(r.getNotes())
                .proceduresPerformed(r.getProceduresPerformed())
                .cost(r.getCost())
                .dentistRemarks(r.getDentistRemarks())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

/*
 * This file contains the TreatmentRecordService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
