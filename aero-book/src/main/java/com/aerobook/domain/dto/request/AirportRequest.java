package com.aerobook.domain.dto.request;

import com.aerobook.exception.DuplicateResourceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AirportRequest {

    @NotBlank(message = "IATA code is required")
    @Size(min = 2, max = 3, message = "IATA code must be 2-3 characters")
    private String iataCode;


        public void validateIataCodeUpdate(String existing, String newIataCode) {

                if (!existing.equalsIgnoreCase(newIataCode)
                        && airlineQueryService.existsByIataCode(newIataCode)) {

                        throw new DuplicateResourceException("Aircraft", "IATA Code", newIataCode);
                }
        }
}

