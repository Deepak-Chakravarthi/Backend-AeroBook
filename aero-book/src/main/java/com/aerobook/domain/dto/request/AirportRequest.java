package com.aerobook.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Airport request.
 */
@AllArgsConstructor
@Getter
@Setter
public class AirportRequest {

    @NotBlank(message = "IATA code is required")
    @Size(min = 2, max = 3, message = "IATA code must be 2-3 characters")
    private String iataCode;

}

