package com.aerobook.domain.enums;

import com.aerobook.exception.AeroBookException;
import org.springframework.http.HttpStatus;


public enum AircraftStatus {
    ACTIVE, UNDER_MAINTENANCE, RETIRED;


    public static AircraftStatus parseStatus(String status) {
        try {
            return AircraftStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AeroBookException(
                    "Invalid aircraft status: " + status + ". Valid values: ACTIVE, UNDER_MAINTENANCE, RETIRED",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_STATUS"
            );
        }
    }
}
