package com.dental.clinic.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventResponse {
    private String eventId;
    private String eventType;
    private LocalDateTime eventDate;
    private String title;
    private String description;
    private Map<String, Object> extraInfo;
}

/*
 * This file contains the TimelineEventResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
