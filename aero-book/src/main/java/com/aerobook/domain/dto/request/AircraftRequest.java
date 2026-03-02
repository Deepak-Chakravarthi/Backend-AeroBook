package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.AircraftStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AircraftRequest(

        @NotBlank(message = "Registration number is required")
        String registrationNumber,

        @NotBlank(message = "Model is required")
        String model,

        @NotBlank(message = "Manufacturer is required")
        String manufacturer,

        @NotNull @Min(1)
        Integer totalSeats,

        @NotNull
        AircraftStatus status,

        @NotNull(message = "Airline ID is required")
        Long airlineId
) {
}