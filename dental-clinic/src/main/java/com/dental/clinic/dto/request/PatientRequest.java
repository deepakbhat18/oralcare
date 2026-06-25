package com.dental.clinic.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank
    private String name;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank
    private String contact;

    @Email
    private String email;

    private String gender;
    private String bloodGroup;
    private String medicalHistory;
    private String allergies;
    private String emergencyContact;
    private String address;
    private Long clinicId;
}

/*
 * This file contains the PatientRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
