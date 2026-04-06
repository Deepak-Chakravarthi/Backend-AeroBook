package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.BookingStatus;
import com.aerobook.domain.enums.BookingType;
import com.aerobook.domain.enums.CancellationReason;
import com.aerobook.domain.enums.SeatClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long          id,
        String        pnr,
        BookingType   bookingType,
        BookingStatus status,

        // booker
        Long   userId,
        String username,

        // outbound
        Long          outboundFlightId,
        String        outboundFlightNumber,
        String        outboundOriginCode,
        String        outboundDestinationCode,
        LocalDateTime outboundDepartureTime,
        LocalDateTime outboundArrivalTime,
        SeatClass     outboundSeatClass,
        String        outboundSeatNumber,
        String        outboundSeatHoldRef,

        // return (nullable)
        Long          returnFlightId,
        String        returnFlightNumber,
        String        returnOriginCode,
        String        returnDestinationCode,
        LocalDateTime returnDepartureTime,
        LocalDateTime returnArrivalTime,
        SeatClass     returnSeatClass,
        String        returnSeatNumber,

        // passenger
        String    passengerFirstName,
        String    passengerLastName,
        String    passengerEmail,
        String    passengerPhone,
        LocalDate passengerDob,
        String    passportNumber,
        String    nationality,

        // fare
        BigDecimal baseFare,
        BigDecimal tax,
        BigDecimal totalFare,

        // cancellation
        CancellationReason cancellationReason,
        String             cancellationRemarks,
        LocalDateTime      cancelledAt,

        // TTL
        LocalDateTime seatHoldExpiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
