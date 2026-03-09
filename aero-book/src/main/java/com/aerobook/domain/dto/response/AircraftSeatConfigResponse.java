package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.SeatClass;

/**
 * The type Aircraft seat config response.
 */
public record AircraftSeatConfigResponse(
        Long id,
        SeatClass seatClass,
        Integer seatCount,
        Integer rows,
        Integer seatsPerRow
) {}