package com.dental.clinic.dto.response;
import com.dental.clinic.model.User;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private String phoneNumber;
    private boolean active;
    private boolean emailVerified;
    private Long clinicId;
    private LocalDateTime createdAt;
}

/*
 * This file contains the UserResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
