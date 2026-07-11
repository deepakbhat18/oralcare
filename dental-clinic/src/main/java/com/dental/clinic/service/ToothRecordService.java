package com.dental.clinic.service;

import com.dental.clinic.dto.request.ToothRecordRequest;
import com.dental.clinic.dto.response.ToothRecordResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.ToothRecord;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.PatientRepository;
import com.dental.clinic.repository.ToothRecordRepository;
import com.dental.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToothRecordService {

    private final ToothRecordRepository toothRepo;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public List<ToothRecordResponse> getTeethByPatientId(Long patientId) {
        if (!patientRepo.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return toothRepo.findByPatientId(patientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ToothRecordResponse updateTooth(Long patientId, Integer toothNumber, ToothRecordRequest request, Long updatedById) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        User doctor = userRepo.findById(updatedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", updatedById));

        ToothRecord record = toothRepo.findByPatientIdAndToothNumber(patientId, toothNumber)
                .orElse(null);

        ToothRecord.ToothCondition condition;
        try {
            condition = ToothRecord.ToothCondition.valueOf(request.getCondition().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid tooth condition: " + request.getCondition());
        }

        if (record == null) {
            record = ToothRecord.builder()
                    .patient(patient)
                    .toothNumber(toothNumber)
                    .condition(condition)
                    .notes(request.getNotes())
                    .updatedBy(doctor)
                    .build();
        } else {
            record.setCondition(condition);
            record.setNotes(request.getNotes());
            record.setUpdatedBy(doctor);
        }

        return toResponse(toothRepo.save(record));
    }

    private ToothRecordResponse toResponse(ToothRecord m) {
        return ToothRecordResponse.builder()
                .id(m.getId())
                .patientId(m.getPatient().getId())
                .toothNumber(m.getToothNumber())
                .condition(m.getCondition().name())
                .notes(m.getNotes())
                .updatedAt(m.getUpdatedAt())
                .updatedById(m.getUpdatedBy() != null ? m.getUpdatedBy().getId() : null)
                .updatedByName(m.getUpdatedBy() != null ? m.getUpdatedBy().getName() : null)
                .build();
    }
}
