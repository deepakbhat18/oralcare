package com.dental.clinic.dto.request;
import com.dental.clinic.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank @Size(min=2, max=100)
    private String name;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min=8, max=64)
    private String password;
    private User.Role role;
    private String phoneNumber;
    private Long clinicId;
}
