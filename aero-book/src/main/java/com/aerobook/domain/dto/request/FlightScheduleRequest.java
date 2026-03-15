package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.ScheduleDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * The type Flight schedule request.
 */
public record FlightScheduleRequest(

        @NotBlank(message = "Flight number prefix is required")
        String flightNumberPrefix,

        @NotNull(message = "Airline ID is required")
        Long airlineId,

        @NotNull(message = "Aircraft ID is required")
        Long aircraftId,

        @NotNull(message = "Route ID is required")
        Long routeId,

        @NotNull(message = "Departure time is required")
        LocalTime departureTime,

        @NotNull(message = "Arrival time is required")
        LocalTime arrivalTime,

        @NotNull(message = "Duration is required")
        Integer durationMinutes,

        @NotEmpty(message = "At least one operating day is required")
        Set<ScheduleDay> operatingDays,

        @NotNull(message = "Valid from date is required")
        LocalDate validFrom,

        LocalDate validUntil,
        String terminal,
        String gate
) {}