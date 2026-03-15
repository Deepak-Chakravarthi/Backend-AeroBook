package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(

        @NotNull(message = "Status is required")
        UserStatus status
) {}
