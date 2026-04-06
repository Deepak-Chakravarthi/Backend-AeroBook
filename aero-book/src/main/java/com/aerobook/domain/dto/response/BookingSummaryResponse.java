package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.BookingStatus;
import com.aerobook.domain.enums.BookingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingSummaryResponse(
        Long          id,
        String        pnr,
        BookingType   bookingType,
        BookingStatus status,
        String        outboundFlightNumber,
        String        outboundOriginCode,
        String        outboundDestinationCode,
        LocalDateTime outboundDepartureTime,
        String        passengerFirstName,
        String        passengerLastName,
        BigDecimal    totalFare,
        LocalDateTime createdAt
) {}
