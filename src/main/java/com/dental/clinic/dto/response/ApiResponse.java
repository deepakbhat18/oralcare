package com.dental.clinic.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }
    public static <T> ApiResponse<T> success(T data) { return success("Success", data); }
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).error(message).timestamp(LocalDateTime.now()).build();
    }
}

/*
 * This file contains the ApiResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
