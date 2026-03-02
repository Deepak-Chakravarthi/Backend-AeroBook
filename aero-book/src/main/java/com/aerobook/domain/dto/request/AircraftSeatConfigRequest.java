package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.SeatClass;

public record AircraftSeatConfigRequest(
        Integer rows,
        SeatClass seatClass,
        Integer seatsPerRow,
        Integer seatCount

) {


}
