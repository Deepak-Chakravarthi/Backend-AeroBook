package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.LoyaltyTier;

import java.time.LocalDateTime;

public record LoyaltyAccountResponse(
        Long        id,
        Long        userId,
        String      username,
        String      membershipNumber,
        LoyaltyTier tier,
        Long        totalMiles,
        Long        availableMiles,
        Long        tierQualifyingMiles,
        Integer     flightsCompleted,
        Long        milesToNextTier,
        String      nextTier,
        LocalDateTime tierUpgradedAt,
        LocalDateTime lastActivityAt,
        LocalDateTime createdAt
) {}