package com.dental.clinic.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothRecordRequest {
    @NotNull(message = "Tooth condition is required")
    private String condition;

    private String notes;
}
