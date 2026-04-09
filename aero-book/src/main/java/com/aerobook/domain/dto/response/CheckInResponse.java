package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.CheckInStatus;
import com.aerobook.domain.enums.SeatClass;

import java.time.LocalDateTime;

public record CheckInResponse(
        Long          id,
        Long          ticketId,
        String        ticketNumber,
        Long          bookingId,
        String        pnr,
        Long          passengerId,
        String        passengerName,
        Long          flightId,
        String        flightNumber,
        String        originCode,
        String        destinationCode,
        LocalDateTime departureTime,
        String        seatNumber,
        SeatClass     seatClass,
        CheckInStatus status,
        String        boardingGroup,
        LocalDateTime checkedInAt,
        LocalDateTime createdAt
) {}
