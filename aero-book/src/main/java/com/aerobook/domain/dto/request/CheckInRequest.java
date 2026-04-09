package com.aerobook.domain.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckInRequest(

        @NotNull(message = "Ticket ID is required")
        Long ticketId,

        @NotBlank(message = "Seat number is required")
        String seatNumber           // seat selected during check-in
) {}
