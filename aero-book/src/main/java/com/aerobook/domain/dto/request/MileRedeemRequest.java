package com.aerobook.domain.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MileRedeemRequest(

        @NotNull
        @Min(value = 1, message = "Miles to redeem must be positive")
        Long miles,

        String description
) {}