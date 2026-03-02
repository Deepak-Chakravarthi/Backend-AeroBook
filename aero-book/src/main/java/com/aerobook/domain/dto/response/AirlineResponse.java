package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.AirlineStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AirlineResponse(
        Long id,
        String name,
        String iataCode,
        String icaoCode,
        String logoUrl,
        String country,
        AirlineStatus status,
        List<AircraftSummaryResponse> aircraft,
        LocalDateTime createdAt
) {
}