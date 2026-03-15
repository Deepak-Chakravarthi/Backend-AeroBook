package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.FlightStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The type Flight request.
 */
public record FlightRequest(

        @NotBlank(message = "Flight number is required")
        String flightNumber,

        @NotNull(message = "Airline ID is required")
        Long airlineId,

        @NotNull(message = "Aircraft ID is required")
        Long aircraftId,

        @NotNull(message = "Route ID is required")
        Long routeId,

        @NotNull(message = "Departure date is required")
        LocalDate departureDate,

        @NotNull(message = "Departure time is required")
        LocalDateTime departureTime,

        @NotNull(message = "Arrival time is required")
        LocalDateTime arrivalTime,

        @NotNull(message = "Duration is required")
        Integer durationMinutes,

        FlightStatus status,

        String gate,
        String terminal
) {}
