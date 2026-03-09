package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.AircraftStatus;

/**
 * The type Aircraft summary response.
 */
// Used inside AirlineResponse — avoids circular nesting
public record AircraftSummaryResponse(
        Long id,
        String registrationNumber,
        String model,
        AircraftStatus status
) {}
