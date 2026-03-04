package com.aerobook.domain.enums;

import com.aerobook.exception.AeroBookException;
import org.springframework.http.HttpStatus;

public enum RouteStatus {
    ACTIVE, INACTIVE;

    public static RouteStatus parseStatus(String status) {
        try {
            return RouteStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AeroBookException(
                    "Invalid route status: " + status + ". Valid values: ACTIVE, INACTIVE",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_STATUS"
            );
        }
    }
}