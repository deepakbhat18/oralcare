package com.dental.clinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*
 * This file contains the FileUploadException class which manages intercepting runtime errors globally and mapping them to standardized HTTP responses.
 * Concepts used: Spring @ControllerAdvice exception handlers and REST error handling.
 */
