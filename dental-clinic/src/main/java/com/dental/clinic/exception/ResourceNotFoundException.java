package com.dental.clinic.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }
}

/*
 * This file contains the ResourceNotFoundException class which manages intercepting runtime errors globally and mapping them to standardized HTTP responses.
 * Concepts used: Spring @ControllerAdvice exception handlers and REST error handling.
 */
