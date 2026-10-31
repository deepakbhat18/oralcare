package com.dental.clinic.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}

/*
 * This file contains the BadRequestException class which manages intercepting runtime errors globally and mapping them to standardized HTTP responses.
 * Concepts used: Spring @ControllerAdvice exception handlers and REST error handling.
 */
