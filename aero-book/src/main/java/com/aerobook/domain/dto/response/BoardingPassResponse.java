package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.BoardingPassStatus;
import com.aerobook.domain.enums.SeatClass;

import java.time.LocalDateTime;

public record BoardingPassResponse(
        Long               id,
        String             boardingPassNumber,
        Long               checkInId,
        Long               ticketId,
        Long               passengerId,
        String             passengerName,
        Long               flightId,
        String             flightNumber,
        String             originCode,
        String             destinationCode,
        LocalDateTime      departureTime,
        LocalDateTime      boardingTime,
        String             seatNumber,
        SeatClass          seatClass,
        String             gate,
        String             terminal,
        String             boardingGroup,
        String             barcode,
        BoardingPassStatus status,
        String             pdfDownloadUrl,
        LocalDateTime      createdAt
) {}