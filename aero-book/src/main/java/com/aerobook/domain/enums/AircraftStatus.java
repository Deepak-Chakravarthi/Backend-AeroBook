package com.aerobook.domain.enums;

import com.aerobook.exception.AeroBookException;
import org.springframework.http.HttpStatus;


/**
 * The enum Aircraft status.
 */
public enum AircraftStatus {
    /**
     * Active aircraft status.
     */
    ACTIVE,
    /**
     * Under maintenance aircraft status.
     */
    UNDER_MAINTENANCE,
    /**
     * Retired aircraft status.
     */
    RETIRED;

}
