package com.aerobook.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * The type Aero book exception.
 */
@Getter
public class AeroBookException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    /**
     * Instantiates a new Aero book exception.
     *
     * @param message   the message
     * @param status    the status
     * @param errorCode the error code
     */
    public AeroBookException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
