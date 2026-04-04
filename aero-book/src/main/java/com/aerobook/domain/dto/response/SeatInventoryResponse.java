package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.SeatClass;

public record SeatInventoryResponse(
        Long id,
        Long flightId,
        String flightNumber,
        SeatClass seatClass,
        Integer totalSeats,
        Integer availableSeats,
        Integer heldSeats,
        Integer bookedSeats,
        Integer blockedSeats,
        Long version
) {}
