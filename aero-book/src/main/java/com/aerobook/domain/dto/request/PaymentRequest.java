package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotNull(message = "Booking ID is required")
        Long bookingId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        // Simulated gateway fields
        String cardLastFour,
        String upiId,
        String walletProvider
) {}