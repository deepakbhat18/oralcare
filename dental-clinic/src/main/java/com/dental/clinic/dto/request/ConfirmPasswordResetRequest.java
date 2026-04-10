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
