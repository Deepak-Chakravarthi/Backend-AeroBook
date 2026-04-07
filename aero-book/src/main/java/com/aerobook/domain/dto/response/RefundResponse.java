package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.RefundReason;
import com.aerobook.domain.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long         id,
        String       refundReference,
        Long         paymentId,
        String       paymentReference,
        Long         bookingId,
        String       pnr,
        RefundStatus status,
        RefundReason reason,
        BigDecimal   amount,
        String       remarks,
        String       gatewayRefundId,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {}
