package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.RouteStatus;
import jakarta.validation.constraints.NotNull;

public record RouteRequest(

        @NotNull(message = "Origin airport ID is required")
        Long originAirportId,

        @NotNull(message = "Destination airport ID is required")
        Long destinationAirportId,

        Integer distanceKm,
        Integer estimatedDurationMinutes,

        @NotNull
        RouteStatus status
) {}