package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.RoleType;
import jakarta.validation.constraints.NotNull;

public record RoleAssignRequest(

        @NotNull(message = "Role is required")
        RoleType role
) {}
