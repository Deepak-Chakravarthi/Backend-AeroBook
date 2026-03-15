package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.SeatClass;

import java.math.BigDecimal;

/**
 * The type Flight fare response.
 */
public record FlightFareResponse(
        Long id,
        SeatClass seatClass,
        BigDecimal baseFare,
        BigDecimal tax,
        BigDecimal totalFare,
        Integer availableSeats
) {}