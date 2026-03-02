package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.RouteStatus;

public record RouteResponse(
        Long id,
        AirportResponse origin,
        AirportResponse destination,
        Integer distanceKm,
        Integer estimatedDurationMinutes,
        RouteStatus status
) {
}