package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.FlightStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkFlightStatusRequest(

        @NotEmpty(message = "Flight IDs are required")
        List<Long> flightIds,

        @NotNull(message = "Status is required")
        FlightStatus status,

        Integer delayMinutes,
        String reason
) {}