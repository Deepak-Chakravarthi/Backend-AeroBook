package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.FlightStatus;
import jakarta.validation.constraints.NotNull;

/**
 * The type Flight status update request.
 */
public record FlightStatusUpdateRequest(

        @NotNull(message = "Status is required")
        FlightStatus status,

        Integer delayMinutes,   // populated when status = DELAYED
        String reason
) {}
