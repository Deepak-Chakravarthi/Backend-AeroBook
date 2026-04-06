package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.Gender;
import com.aerobook.domain.enums.PassengerType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PassengerResponse(
        Long          id,
        Long          bookingId,
        String        pnr,
        Long          userId,
        String        firstName,
        String        lastName,
        Gender        gender,
        LocalDate     dateOfBirth,
        String        passportNumber,
        LocalDate     passportExpiry,
        String        nationality,
        String        email,
        String        phone,
        PassengerType passengerType,
        List<TicketResponse> tickets,
        LocalDateTime createdAt
) {}
