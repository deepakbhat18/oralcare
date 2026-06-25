package com.dental.clinic.service;

import com.dental.clinic.dto.request.PatientRequest;
import com.dental.clinic.dto.response.PatientResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static io.micrometer.core.ipc.http.HttpSender.Request.build;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public PatientResponse getPatientById(Long id) {
        return mapToResponse(findById(id));
    }

    public List<PatientResponse> getPatientsByClinic(Long clinicId) {
        return patientRepository.findByClinicId(clinicId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PatientResponse> searchPatients(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (request.getEmail() != null && patientRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Patient with email already exists: " + request.getEmail());
        }
        Patient patient = buildPatient(request);
        patient = patientRepository.save(patient);
        log.info("Patient created: {} (id={})", patient.getName(), patient.getId());
        return mapToResponse(patient);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = findById(id);
        patient.setName(request.getName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setContact(request.getContact());
        patient.setEmail(request.getEmail());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setAddress(request.getAddress());
        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinic", request.getClinicId()));
            patient.setClinic(clinic);
        }
        return mapToResponse(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient", id);
        }
        patientRepository.deleteById(id);
        log.info("Patient deleted: id={}", id);
    }

    private Patient buildPatient(PatientRequest request) {
        Patient.PatientBuilder builder = Patient.builder()
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .contact(request.getContact())
                .email(request.getEmail())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .medicalHistory(request.getMedicalHistory())
                .allergies(request.getAllergies())
                .emergencyContact(request.getEmergencyContact())
                .address(request.getAddress());

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinic", request.getClinicId()));
            builder.clinic(clinic);
        }
        return builder.build();
    }

    private Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    public PatientResponse mapToResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .age(p.getAge())
                .contact(p.getContact())
                .email(p.getEmail())
                .gender(p.getGender())
                .bloodGroup(p.getBloodGroup())
                .medicalHistory(p.getMedicalHistory())
                .allergies(p.getAllergies())
                .emergencyContact(p.getEmergencyContact())
                .address(p.getAddress())
                .clinicId(p.getClinic() != null ? p.getClinic().getId() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
    @Transactional
    public void createPatientFromUser(User user) {

        if (user.getEmail() != null && patientRepository.existsByEmail(user.getEmail())) {
            return;
        }
        Patient patient = Patient.builder()
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getPhoneNumber())
                .dateOfBirth(java.time.LocalDate.now().minusYears(18))
                .user(user)
                .build();

        patientRepository.save(patient);

        log.info("Patient auto-created for user: {} (id={})", user.getEmail(), patient.getId());
    }
    @Transactional
    public PatientResponse getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId).orElse(null);

        if (patient == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            patient = Patient.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .contact(user.getPhoneNumber())
                    .dateOfBirth(java.time.LocalDate.now().minusYears(18))
                    .user(user)
                    .build();

            patient = patientRepository.save(patient);

            log.info("Patient auto-created for existing user: {}", userId);
        }

        return mapToResponse(patient);
    }
}

/*
 * This file contains the PatientService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
