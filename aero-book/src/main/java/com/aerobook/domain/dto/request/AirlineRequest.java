package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.AirlineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AirlineRequest(

        @NotBlank(message = "Airline name is required")
        String name,

        @NotBlank(message = "IATA code is required")
        @Size(min = 2, max = 3, message = "IATA code must be 2-3 characters")
        String iataCode,

        @Size(max = 4, message = "ICAO code must be max 4 characters")
        String icaoCode,

        String logoUrl,
        String country,

        @NotNull(message = "Status is required")
        AirlineStatus status
) {}
