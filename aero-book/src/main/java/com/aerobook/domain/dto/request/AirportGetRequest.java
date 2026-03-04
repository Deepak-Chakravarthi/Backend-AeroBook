package com.aerobook.domain.dto.request;

import lombok.Builder;
import lombok.Getter;

import static com.aerobook.util.StreamUtils.countNonNull;

@Getter
@Builder
public class AirportGetRequest {

    private final Long id;
    private final String iataCode;
    private final String city;
    private final String country;

    public void validate() {
        long filledCount = countNonNull(id, iataCode, city, country);

        if (filledCount > 1) {
            throw new IllegalArgumentException(
                    "Only one search parameter is allowed at a time. Provided " + filledCount + " parameters."
            );
        }
    }

}