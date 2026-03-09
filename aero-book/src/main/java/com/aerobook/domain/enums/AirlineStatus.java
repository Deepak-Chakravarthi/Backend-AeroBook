package com.aerobook.domain.enums;

import com.aerobook.exception.AeroBookException;
import org.springframework.http.HttpStatus;

/**
 * The enum Airline status.
 */
public enum AirlineStatus {
    /**
     * Active airline status.
     */
    ACTIVE,
    /**
     * Inactive airline status.
     */
    INACTIVE,
    /**
     * Suspended airline status.
     */
    SUSPENDED;

}
