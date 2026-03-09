package com.aerobook.exception;

import org.springframework.http.HttpStatus;

/**
 * The type Duplicate resource exception.
 */
public class DuplicateResourceException extends AeroBookException {
    /**
     * Instantiates a new Duplicate resource exception.
     *
     * @param resource the resource
     * @param field    the field
     * @param value    the value
     */
    public DuplicateResourceException(String resource, String field, String value) {
        super(resource + " already exists with " + field + ": " + value, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
}