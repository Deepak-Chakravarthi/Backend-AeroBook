package com.aerobook.domain.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MileAdjustRequest(

        @NotNull(message = "Miles amount is required")
        Long miles,                 // positive to add, negative to deduct

        @NotBlank(message = "Description is required")
        String description,

        String referenceId
) {}