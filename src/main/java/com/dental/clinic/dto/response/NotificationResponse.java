package com.dental.clinic.dto.response;

import com.dental.clinic.model.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long                        id;
    private String                      title;
    private String                      message;
    private Notification.NotificationType type;
    private boolean                     read;
    private Long                        referenceId;
    private String                      referenceType;
    private LocalDateTime               createdAt;
}

/*
 * This file contains the NotificationResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
