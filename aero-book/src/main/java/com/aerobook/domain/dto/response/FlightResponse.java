package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.FlightStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The type Flight response.
 */
public record FlightResponse(
        Long id,
        String flightNumber,
        Long airlineId,
        String airlineName,
        String airlineIataCode,
        Long aircraftId,
        String aircraftModel,
        String registrationNumber,
        Long routeId,
        String originCode,
        String originCity,
        String destinationCode,
        String destinationCity,
        LocalDate departureDate,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Integer durationMinutes,
        Integer delayMinutes,
        FlightStatus status,
        String gate,
        String terminal,
        List<FlightFareResponse> fares
) {}
