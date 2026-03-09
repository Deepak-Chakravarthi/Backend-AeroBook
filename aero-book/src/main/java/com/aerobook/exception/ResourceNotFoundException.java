package com.aerobook.exception;

import org.springframework.http.HttpStatus;

/**
 * The type Resource not found exception.
 */
public class ResourceNotFoundException extends AeroBookException {
    /**
     * Instantiates a new Resource not found exception.
     *
     * @param resource the resource
     * @param id       the id
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    /**
     * Instantiates a new Resource not found exception.
     *
     * @param resource the resource
     * @param field    the field
     * @param value    the value
     */
    public ResourceNotFoundException(String resource, String field, String value) {
        super(resource + " not found with " + field + ": " + value, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}