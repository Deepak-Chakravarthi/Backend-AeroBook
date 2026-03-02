package com.aerobook.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AeroBookException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public AeroBookException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
