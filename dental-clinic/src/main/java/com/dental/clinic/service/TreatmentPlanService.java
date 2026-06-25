package com.dental.clinic.service;

import com.dental.clinic.dto.request.TreatmentPlanRequest;
import com.dental.clinic.dto.response.TreatmentPlanResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import com.dental.clinic.security.aspect.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final PatientRepository       patientRepository;
    private final UserRepository          userRepository;

    public List<TreatmentPlanResponse> getAll() {
        return treatmentPlanRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public TreatmentPlanResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    public List<TreatmentPlanResponse> getByPatient(Long patientId) {
        return treatmentPlanRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    @Audit(action = "CREATE_TREATMENT_PLAN", entityType = "TreatmentPlan")
    public TreatmentPlanResponse create(TreatmentPlanRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", req.getPatientId()));
        User dentist = userRepository.findById(req.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist", req.getDentistId()));

        TreatmentPlan plan = TreatmentPlan.builder()
                .patient(patient).dentist(dentist)
                .planName(req.getPlanName())
                .description(req.getDescription())

                .sessions(
                        req.getSessions() != null
                                ? req.getSessions().stream()
                                  .map(s -> new TreatmentPlan.SessionRecord(s, false, null, null))
                                  .collect(Collectors.toList())
                                : List.of()
                )
                .estimatedCost(req.getEstimatedCost())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .totalSessions(req.getTotalSessions())
                .status(TreatmentPlan.TreatmentStatus.PENDING)
                .completedSessions(0)
                .build();

        TreatmentPlan saved = treatmentPlanRepository.save(plan);
        log.info("Treatment plan created: id={}, patient={}, sessions={}",
                saved.getId(), patient.getName(), saved.getTotalSessions());
        return mapToResponse(saved);
    }

    @Transactional
    @Audit(action = "UPDATE_TREATMENT_PLAN", entityType = "TreatmentPlan")
    public TreatmentPlanResponse update(Long id, TreatmentPlanRequest req) {
        TreatmentPlan plan = findById(id);
        plan.setPlanName(req.getPlanName());
        plan.setDescription(req.getDescription());

        if (req.getSessions() != null) {
            List<TreatmentPlan.SessionRecord> sessions =
                    req.getSessions().stream()
                            .map(s -> new TreatmentPlan.SessionRecord(s, false, null, null))
                            .collect(Collectors.toList());

            plan.setSessions(sessions);
        }
        plan.setEstimatedCost(req.getEstimatedCost());
        plan.setStartDate(req.getStartDate());
        plan.setEndDate(req.getEndDate());
        plan.setTotalSessions(req.getTotalSessions());
        return mapToResponse(treatmentPlanRepository.save(plan));
    }

    @Transactional
    @Audit(action = "UPDATE_TREATMENT_PLAN_STATUS", entityType = "TreatmentPlan")
    public TreatmentPlanResponse updateStatus(Long id, TreatmentPlan.TreatmentStatus status) {
        TreatmentPlan plan = findById(id);
        plan.setStatus(status);
        return mapToResponse(treatmentPlanRepository.save(plan));
    }






    @Transactional
    @Audit(action = "COMPLETE_TREATMENT_SESSION", entityType = "TreatmentPlan")
    public TreatmentPlanResponse incrementCompletedSessions(Long id) {
        TreatmentPlan plan = findById(id);
        int completed = plan.getCompletedSessions() != null ? plan.getCompletedSessions() : 0;
        int total     = plan.getTotalSessions()     != null ? plan.getTotalSessions()     : 0;

        if (completed >= total) {
            throw new BadRequestException(
                    "All " + total + " sessions are already marked as completed for plan id=" + id);
        }

        plan.setCompletedSessions(completed + 1);

        if (plan.getCompletedSessions().equals(total)) {
            plan.setStatus(TreatmentPlan.TreatmentStatus.COMPLETED);
            log.info("Treatment plan id={} marked COMPLETED — all {} sessions done", id, total);
        } else if (plan.getStatus() == TreatmentPlan.TreatmentStatus.PENDING) {
            plan.setStatus(TreatmentPlan.TreatmentStatus.IN_PROGRESS);
        }

        return mapToResponse(treatmentPlanRepository.save(plan));
    }

    @Transactional
    public void delete(Long id) {
        if (!treatmentPlanRepository.existsById(id))
            throw new ResourceNotFoundException("TreatmentPlan", id);
        treatmentPlanRepository.deleteById(id);
    }

    private TreatmentPlan findById(Long id) {
        return treatmentPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TreatmentPlan", id));
    }

    public TreatmentPlanResponse mapToResponse(TreatmentPlan p) {
        return TreatmentPlanResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getName())
                .dentistId(p.getDentist().getId())
                .dentistName(p.getDentist().getName())
                .planName(p.getPlanName())
                .description(p.getDescription())

                .sessions(
                        p.getSessions().stream()
                                .map(TreatmentPlan.SessionRecord::getTitle)
                                .collect(Collectors.toList())
                )
                .status(p.getStatus())
                .estimatedCost(p.getEstimatedCost())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .totalSessions(p.getTotalSessions())
                .completedSessions(p.getCompletedSessions())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

/*
 * This file contains the TreatmentPlanService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
