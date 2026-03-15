package com.aerobook.domain.enums;

public enum RoleType {
    PASSENGER,
    AGENT,
    AIRLINE_ADMIN,
    SUPER_ADMIN;

    /**
     * Method to resolveRoleType
     *
     * @param role role
     * @return RoleType
     */
    public static RoleType resolveRoleType(String role) {
        try {
            return RoleType.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + role + ". Valid values: PASSENGER, AGENT, AIRLINE_ADMIN, SUPER_ADMIN");
        }
    }
}
