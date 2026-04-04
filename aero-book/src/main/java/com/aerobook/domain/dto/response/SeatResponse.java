package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.SeatStatus;
import com.aerobook.domain.enums.SeatType;

import java.time.LocalDateTime;

public record SeatResponse(
        Long id,
        Long flightId,
        String flightNumber,
        String seatNumber,
        Integer rowNumber,
        String seatLetter,
        SeatClass seatClass,
        SeatType seatType,
        SeatStatus status,
        String heldByBookingRef,
        LocalDateTime heldUntil
) {}