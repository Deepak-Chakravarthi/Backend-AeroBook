package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.ScheduleDay;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record FlightScheduleResponse(
        Long id,
        String flightNumberPrefix,
        Long airlineId,
        String airlineName,
        Long routeId,
        String originCode,
        String destinationCode,
        LocalTime departureTime,
        LocalTime arrivalTime,
        Integer durationMinutes,
        Set<ScheduleDay> operatingDays,
        LocalDate validFrom,
        LocalDate validUntil,
        Boolean active,
        String terminal,
        String gate
) {}