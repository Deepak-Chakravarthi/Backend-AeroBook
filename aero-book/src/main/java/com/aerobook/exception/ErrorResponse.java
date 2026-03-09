package com.aerobook.exception;


import java.time.LocalDateTime;
import java.util.Map;

/**
 * The type Error response.
 */
public record ErrorResponse(
        int status,
        String errorCode,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors   // populated on validation failures
) {}
