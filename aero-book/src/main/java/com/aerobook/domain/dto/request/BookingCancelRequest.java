package com.aerobook.domain.dto.request;



import com.aerobook.domain.enums.CancellationReason;
import jakarta.validation.constraints.NotNull;

public record BookingCancelRequest(

        @NotNull(message = "Cancellation reason is required")
        CancellationReason reason,

        String remarks
) {}