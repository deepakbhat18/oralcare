package com.dental.clinic.exception;
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}

/*
 * This file contains the ConflictException class which manages intercepting runtime errors globally and mapping them to standardized HTTP responses.
 * Concepts used: Spring @ControllerAdvice exception handlers and REST error handling.
 */
