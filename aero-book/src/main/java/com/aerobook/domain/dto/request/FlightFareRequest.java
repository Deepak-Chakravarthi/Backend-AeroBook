package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.SeatClass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FlightFareRequest(

        @NotNull(message = "Seat class is required")
        SeatClass seatClass,

        @NotNull @Positive(message = "Base fare must be positive")
        BigDecimal baseFare,

        @NotNull @Positive(message = "Tax must be positive")
        BigDecimal tax,

        @NotNull
        Integer availableSeats
) {}
