package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.RoleType;
import com.aerobook.domain.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserStatus status,
        Set<RoleType> roles,
        LocalDateTime createdAt
) {}
