package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.SeatClass;

import java.time.LocalDateTime;
import java.util.List;

public record SeatHoldResponse(
        String bookingRef,
        Long flightId,
        String flightNumber,
        SeatClass seatClass,
        Integer seatsHeld,
        List<String> seatNumbers,       // specific seats allocated
        LocalDateTime heldUntil,        // TTL expiry
        Integer remainingAvailable
) {}
