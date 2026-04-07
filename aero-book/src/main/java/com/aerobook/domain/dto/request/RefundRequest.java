package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.RefundReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RefundRequest(

        @NotNull(message = "Payment ID is required")
        Long paymentId,

        @NotNull(message = "Refund reason is required")
        RefundReason reason,

        @NotNull
        @DecimalMin(value = "0.01", message = "Refund amount must be positive")
        BigDecimal amount,

        String remarks
) {}
