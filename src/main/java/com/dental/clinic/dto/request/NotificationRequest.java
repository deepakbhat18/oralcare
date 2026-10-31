package com.dental.clinic.dto.request;
import com.dental.clinic.model.Notification;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class NotificationRequest {
    @NotNull private Long userId;
    @NotBlank private String message;
    private String title;
    private Notification.NotificationType type;
    private Long referenceId;
    private String referenceType;
}

/*
 * This file contains the NotificationRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
