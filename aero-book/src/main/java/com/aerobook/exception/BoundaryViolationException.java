package com.aerobook.exception;


/**
 * Thrown when a module boundary rule is violated.
 * Only active in dev profile.
 */
public class BoundaryViolationException extends RuntimeException {

    public BoundaryViolationException(String message) {
        super("BOUNDARY VIOLATION: " + message);
    }
}
