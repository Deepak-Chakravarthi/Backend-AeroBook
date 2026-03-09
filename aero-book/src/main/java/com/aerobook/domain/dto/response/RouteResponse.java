package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.RouteStatus;

/**
 * The type Route response.
 */
public record RouteResponse(
        Long id,
        AirportResponse origin,
        AirportResponse destination,
        Integer distanceKm,
        Integer estimatedDurationMinutes,
        RouteStatus status
) {
}