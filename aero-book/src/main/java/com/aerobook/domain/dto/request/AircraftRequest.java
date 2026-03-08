package com.aerobook.domain.dto.request;

import com.aerobook.domain.enums.AircraftStatus;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.service.query.AircraftQueryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AircraftRequest {
    private final AircraftQueryService aircraftQueryService;

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


    public void validateRegistrationUpdate(String existing, String newRegistration) {

        if (!existing.equals(newRegistration)
                && aircraftQueryService.existsByRegistrationNumber(newRegistration)) {

            throw new DuplicateResourceException("Aircraft", "registration number", newRegistration);
        }
    }
}