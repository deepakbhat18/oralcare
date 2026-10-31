package com.dental.clinic.service;

import com.dental.clinic.dto.response.TimelineEventResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DocumentRepository documentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentRecordRepository treatmentRecordRepository;

    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getPatientTimeline(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }

        List<TimelineEventResponse> events = new ArrayList<>();


        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        for (Appointment a : appointments) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("dentistName", a.getDentist().getName());
            extra.put("status", a.getStatus().toString());
            extra.put("roomNumber", a.getRoomNumber());
            extra.put("durationMinutes", a.getDurationMinutes());

            events.add(TimelineEventResponse.builder()
                    .eventId("APPOINTMENT_" + a.getId())
                    .eventType("APPOINTMENT")
                    .eventDate(a.getDateTime())
                    .title("Appointment for " + a.getTreatmentType())
                    .description("Status: " + a.getStatus() + (a.getNotes() != null ? ". Notes: " + a.getNotes() : ""))
                    .extraInfo(extra)
                    .build());
        }


        List<Document> documents = documentRepository.findByPatientId(patientId, Pageable.unpaged()).getContent();
        for (Document d : documents) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("fileType", d.getFileType().toString());
            extra.put("filePath", d.getFilePath());
            extra.put("originalName", d.getOriginalName());

            events.add(TimelineEventResponse.builder()
                    .eventId("DOCUMENT_" + d.getId())
                    .eventType("DOCUMENT")
                    .eventDate(d.getUploadedAt())
                    .title("Uploaded Document: " + d.getOriginalName())
                    .description("Type: " + d.getFileType() + ", Uploaded by: " + d.getUploadedBy().getName())
                    .extraInfo(extra)
                    .build());
        }


        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        for (Prescription p : prescriptions) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("prescribedBy", p.getPrescribedBy() != null ? p.getPrescribedBy().getName() : "N/A");
            extra.put("isActive", p.isActive());

            List<Map<String, String>> items = new ArrayList<>();
            if (p.getItems() != null) {
                for (PrescriptionItem item : p.getItems()) {
                    Map<String, String> itemMap = new HashMap<>();
                    itemMap.put("medicine", item.getMedicineName());
                    itemMap.put("dosage", item.getDosage());
                    itemMap.put("frequency", item.getFrequency());
                    itemMap.put("duration", item.getDuration());
                    itemMap.put("instructions", item.getInstructions());
                    items.add(itemMap);
                }
            }
            extra.put("items", items);

            events.add(TimelineEventResponse.builder()
                    .eventId("PRESCRIPTION_" + p.getId())
                    .eventType("PRESCRIPTION")
                    .eventDate(p.getCreatedAt())
                    .title("Prescription Issued")
                    .description(p.getMedicineList() != null ? p.getMedicineList() : "No medicines specified")
                    .extraInfo(extra)
                    .build());
        }


        List<TreatmentPlan> plans = treatmentPlanRepository.findByPatientId(patientId);
        for (TreatmentPlan tp : plans) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("dentistName", tp.getDentist().getName());
            extra.put("status", tp.getStatus().toString());
            extra.put("estimatedCost", tp.getEstimatedCost());
            extra.put("completedSessions", tp.getCompletedSessions());
            extra.put("totalSessions", tp.getTotalSessions());

            events.add(TimelineEventResponse.builder()
                    .eventId("TREATMENT_PLAN_" + tp.getId())
                    .eventType("TREATMENT_PLAN")
                    .eventDate(tp.getCreatedAt())
                    .title("Treatment Plan: " + tp.getPlanName())
                    .description("Status: " + tp.getStatus() + ". Sessions: " + tp.getCompletedSessions() + "/" + tp.getTotalSessions() + ". Est. Cost: $" + tp.getEstimatedCost())
                    .extraInfo(extra)
                    .build());
        }


        List<TreatmentRecord> records = treatmentRecordRepository.findByPatientId(patientId);
        for (TreatmentRecord tr : records) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("dentistName", tr.getDentist().getName());
            extra.put("cost", tr.getCost());
            extra.put("remarks", tr.getDentistRemarks());
            extra.put("procedures", tr.getProceduresPerformed());

            events.add(TimelineEventResponse.builder()
                    .eventId("TREATMENT_RECORD_" + tr.getId())
                    .eventType("TREATMENT_RECORD")
                    .eventDate(tr.getCreatedAt())
                    .title("Treatment Procedure Performed")
                    .description("Notes: " + tr.getNotes() + ". Procedures: " + tr.getProceduresPerformed())
                    .extraInfo(extra)
                    .build());
        }


        events.sort((e1, e2) -> {
            if (e1.getEventDate() == null && e2.getEventDate() == null) return 0;
            if (e1.getEventDate() == null) return 1;
            if (e2.getEventDate() == null) return -1;
            return e2.getEventDate().compareTo(e1.getEventDate());
        });

        return events;
    }
}

/*
 * This file contains the TimelineService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
