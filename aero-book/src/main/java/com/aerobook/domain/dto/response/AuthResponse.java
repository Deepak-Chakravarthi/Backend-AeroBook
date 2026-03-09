package com.aerobook.domain.dto.response;

import com.aerobook.domain.enums.RoleType;
import java.util.Set;

public record AuthResponse(
        Long userId,
        String username,
        String email,
        Set<RoleType> roles,
        String tokenType,
        String accessToken,
        long expiresIn
) {
    public static AuthResponse of(Long userId, String username,
                                  String email, Set<RoleType> roles,
                                  String token, long expiresIn) {
        return new AuthResponse(userId, username, email, roles, "Bearer", token, expiresIn);
    }
}