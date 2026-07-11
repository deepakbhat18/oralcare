package com.dental.clinic.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothRecordResponse {
    private Long id;
    private Long patientId;
    private Integer toothNumber;
    private String condition;
    private String notes;
    private LocalDateTime updatedAt;
    private Long updatedById;
    private String updatedByName;
}
