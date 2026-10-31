package com.dental.clinic.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull private Long senderId;
    @NotNull private Long receiverId;
    @NotBlank private String content;
    private String messageType = "TEXT";
}

/*
 * This file contains the ChatMessageRequest class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
