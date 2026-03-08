package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.AirlineStatus;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.service.AirlineQueryService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AirlineRequest {
    private final AirlineQueryService airlineQueryService;

    @NotBlank(message = "Airline name is required")
    private String name;

    @NotBlank(message = "IATA code is required")
    @Size(min = 2, max = 3, message = "IATA code must be 2-3 characters")
    private String iataCode;

    @Size(max = 4, message = "ICAO code must be max 4 characters")
    private String icaoCode;

    private String logoUrl;
    private String country;

    @NotNull(message = "Status is required")
    private AirlineStatus status;


    public void validateIataCodeUpdate(String existing, String newIataCode) {

        if (!existing.equalsIgnoreCase(newIataCode)
                && airlineQueryService.existsByIataCode(newIataCode)) {

            throw new DuplicateResourceException("Aircraft", "IATA Code", newIataCode);
        }
    }
}
