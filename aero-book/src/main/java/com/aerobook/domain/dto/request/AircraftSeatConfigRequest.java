package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.SeatClass;
import jakarta.validation.constraints.NotNull;

public record AircraftSeatConfigRequest(
        @NotNull
        Integer rows,
        @NotNull
        SeatClass seatClass,
        @NotNull
        Integer seatsPerRow,
        @NotNull
        Integer seatCount

) {
}
