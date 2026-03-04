package com.aerobook.domain.dto.request;


import lombok.Builder;
import lombok.Getter;

import static com.aerobook.util.StreamUtils.countNonNull;

@Getter
@Builder
public class RouteGetRequest {

    private final Long id;
    private final String originCode;
    private final String destinationCode;
    private final String status;

     /**
     * Validates that exactly one search parameter is provided.
     * Called before passing to service layer.
     */
    public void validate() {

        long filledCount = countNonNull(id, originCode, status, status);

        if (filledCount > 1) {
            throw new IllegalArgumentException(
                    "Only one search parameter is allowed at a time. Provided " + filledCount + " parameters."
            );
        }
    }
}