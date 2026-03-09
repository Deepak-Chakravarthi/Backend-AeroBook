package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.AircraftStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Aircraft request.
 */
@AllArgsConstructor
@Getter
@Setter
public class AircraftRequest {

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotNull
    @Min(1)
    private Integer totalSeats;

    @NotNull
    private AircraftStatus status;

    @NotNull(message = "Airline ID is required")
    private Long airlineId;

}