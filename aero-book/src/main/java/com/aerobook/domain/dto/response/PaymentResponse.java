package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.PaymentMethod;
import com.aerobook.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentResponse(
        Long          id,
        String        idempotencyKey,
        String        paymentReference,
        Long          bookingId,
        String        pnr,
        Long          userId,
        String        username,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        BigDecimal    amount,
        String        currency,
        String        gatewayTransactionId,
        String        gatewayResponseCode,
        String        failureReason,
        LocalDateTime paidAt,
        List<RefundResponse> refunds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
