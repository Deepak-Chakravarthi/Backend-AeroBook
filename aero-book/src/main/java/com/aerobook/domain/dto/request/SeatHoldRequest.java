package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.SeatClass;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeatHoldRequest(

        @NotNull(message = "Flight ID is required")
        Long flightId,

        @NotNull(message = "Seat class is required")
        SeatClass seatClass,

        @NotNull @Min(1)
        Integer seatCount,

        String preferredSeatNumber      // optional — specific seat request
) {}
