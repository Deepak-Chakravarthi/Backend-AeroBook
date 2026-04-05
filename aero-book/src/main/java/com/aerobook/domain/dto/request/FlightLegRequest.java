package com.aerobook.domain.dto.request;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Getter
@Builder
@Jacksonized
public class FlightLegRequest {

    @NotBlank(message = "Origin code is required")
    private final String originCode;

    @NotBlank(message = "Destination code is required")
    private final String destinationCode;

    @NotNull(message = "Departure date is required")
    private final LocalDate departureDate;

    public void validate() {
        if (originCode == null || originCode.isBlank()) {
            throw new IllegalArgumentException(
                    "originCode is required for each leg");
        }
        if (destinationCode == null || destinationCode.isBlank()) {
            throw new IllegalArgumentException(
                    "destinationCode is required for each leg");
        }
        if (departureDate == null) {
            throw new IllegalArgumentException(
                    "departureDate is required for each leg");
        }
        if (originCode.equalsIgnoreCase(destinationCode)) {
            throw new IllegalArgumentException(
                    "Origin and destination cannot be the same for a leg");
        }
    }

    public String toCacheKey() {
        return originCode + ":" + destinationCode + ":" + departureDate;
    }

}