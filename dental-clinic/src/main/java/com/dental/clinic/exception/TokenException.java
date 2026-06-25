package com.dental.clinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}

/*
 * This file contains the TokenException class which manages intercepting runtime errors globally and mapping them to standardized HTTP responses.
 * Concepts used: Spring @ControllerAdvice exception handlers and REST error handling.
 */
