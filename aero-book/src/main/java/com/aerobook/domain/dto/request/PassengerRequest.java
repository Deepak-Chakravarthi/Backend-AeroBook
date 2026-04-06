package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.Gender;
import com.aerobook.domain.enums.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PassengerRequest(

        @NotNull(message = "Booking ID is required")
        Long bookingId,

        Long userId,                        // optional — link to system User

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        Gender gender,

        LocalDate dateOfBirth,

        String passportNumber,
        LocalDate passportExpiry,
        String nationality,

        String email,
        String phone,

        @NotNull(message = "Passenger type is required")
        PassengerType passengerType
) {}
