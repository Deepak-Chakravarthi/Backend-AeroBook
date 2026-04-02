package com.aerobook.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatReleaseRequest(

        @NotNull
        Long flightId,

        @NotBlank(message = "Booking reference is required")
        String bookingRef
) {}