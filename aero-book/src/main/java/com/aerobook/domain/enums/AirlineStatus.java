package com.aerobook.domain.enums;

import com.aerobook.exception.AeroBookException;
import org.springframework.http.HttpStatus;

public enum AirlineStatus {
    ACTIVE, INACTIVE, SUSPENDED;


    public static AirlineStatus parseStatus(String status) {
        try {
            return AirlineStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AeroBookException(
                    "Invalid airline status: " + status + ". Valid values: ACTIVE, INACTIVE, SUSPENDED",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_STATUS"
            );
        }
    }
}
