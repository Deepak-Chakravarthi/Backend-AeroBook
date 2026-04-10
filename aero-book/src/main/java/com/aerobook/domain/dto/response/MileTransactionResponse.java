package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.MileTransactionType;

import java.time.LocalDateTime;

public record MileTransactionResponse(
        Long                id,
        Long                loyaltyAccountId,
        String              membershipNumber,
        MileTransactionType type,
        Long                miles,
        String              description,
        String              referenceId,
        Long                balanceAfter,
        LocalDateTime       createdAt
) {}