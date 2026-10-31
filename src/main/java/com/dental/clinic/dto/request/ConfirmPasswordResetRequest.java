package com.dental.clinic.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ConfirmPasswordResetRequest {
    private String token;
    private String otp;
    private String email;
    @NotBlank @Size(min=8)
    private String newPassword;
}

/*
 * This file contains the ConfirmPasswordResetRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
