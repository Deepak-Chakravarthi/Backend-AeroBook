package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.FlightStatus;
import com.aerobook.domain.enums.SeatClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FlightSearchResultItem(
        Long   flightId,
        String flightNumber,
        String airlineName,
        String airlineIataCode,
        String originCode,
        String originCity,
        String destinationCode,
        String destinationCity,
        LocalDate     departureDate,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Integer durationMinutes,
        Integer delayMinutes,
        FlightStatus status,
        String gate,
        String terminal,
        List<FareSearchResult> availableFares,
        Integer totalAvailableSeats
) {}
