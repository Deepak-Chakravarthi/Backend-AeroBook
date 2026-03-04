package com.aerobook.domain.dto.request;

import lombok.Builder;
import lombok.Getter;

import static com.aerobook.util.StreamUtils.countNonNull;

@Getter
@Builder
public class AirlineGetRequest {

    private final Long id;
    private final String iataCode;
    private final String status;
    private final String country;

    /**
     * Validates that exactly one search parameter is provided.
     * Called before passing to service layer.
     */
    public void validate() {

        long filledCount = countNonNull(id, iataCode, status, country);

        if (filledCount > 1) {
            throw new IllegalArgumentException(
                    "Only one search parameter is allowed at a time. Provided " + filledCount + " parameters."
            );
        }
    }
}
