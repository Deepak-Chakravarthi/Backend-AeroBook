package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.SeatClass;

import java.math.BigDecimal;

public record FareSearchResult(
        SeatClass    seatClass,
        BigDecimal   baseFare,
        BigDecimal   tax,
        BigDecimal   totalFare,
        BigDecimal   totalFareForAllPassengers,  // totalFare × passengerCount
        Integer      availableSeats,
        boolean      sufficientForPassengers     // availableSeats >= passengerCount
) {}
