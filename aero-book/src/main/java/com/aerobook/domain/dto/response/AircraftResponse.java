package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.AircraftStatus;

import java.util.List;

/**
 * The type Aircraft response.
 */
public record AircraftResponse(
        Long id,
        String registrationNumber,
        String model,
        String manufacturer,
        Integer totalSeats,
        AircraftStatus status,
        Long airlineId,
        String airlineName,
        List<AircraftSeatConfigResponse> seatConfigs
) {
}