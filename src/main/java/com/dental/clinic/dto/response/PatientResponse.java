package com.dental.clinic.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String name;
    private Integer age;
    private String contact;
    private String email;
    private String gender;
    private String bloodGroup;
    private String medicalHistory;
    private String allergies;
    private String emergencyContact;
    private String address;
    private Long clinicId;
    private LocalDateTime createdAt;
}

/*
 * This file contains the PatientResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
