package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long         id,
        String       ticketNumber,
        Long         bookingId,
        String       pnr,
        Long         passengerId,
        String       passengerName,
        Long         flightId,
        String       flightNumber,
        String       originCode,
        String       destinationCode,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        SeatClass    seatClass,
        String       seatNumber,
        TicketStatus status,
        BigDecimal   fare,
        BigDecimal   tax,
        BigDecimal   totalFare,
        Boolean      isReturnLeg,
        LocalDateTime createdAt
) {}
