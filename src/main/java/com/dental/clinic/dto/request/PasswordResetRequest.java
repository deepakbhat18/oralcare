package com.dental.clinic.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class PasswordResetRequest {
    @NotBlank @Email
    private String email;
    private String phone;
    private String method;
}

/*
 * This file contains the PasswordResetRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
