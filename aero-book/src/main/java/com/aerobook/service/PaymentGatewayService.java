package com.aerobook.service;


import com.aerobook.domain.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulates a payment gateway.
 * In production this would call Razorpay, Stripe, PayU etc.
 */
@Slf4j
@Service
public class PaymentGatewayService {

    private static final String SUCCESS_CODE = "00";
    private static final String FAILURE_CODE = "51";

    public record GatewayResponse(
            boolean success,
            String transactionId,
            String responseCode,
            String message
    ) {}

    // ----------------------------------------------------------------
    // Simulate payment processing
    // In production — call actual gateway API here
    // ----------------------------------------------------------------
    public GatewayResponse processPayment(PaymentMethod method,
                                          BigDecimal amount,
                                          String currency) {
        log.info("Gateway — processing {} payment of {} {}",
                method, amount, currency);

        // Simulate 95% success rate
        boolean success = Math.random() > 0.05;

        if (success) {
            String txnId = "TXN-" + UUID.randomUUID()
                    .toString().substring(0, 12).toUpperCase();
            log.info("Gateway — payment SUCCESS, txnId: {}", txnId);
            return new GatewayResponse(true, txnId, SUCCESS_CODE,
                    "Payment processed successfully");
        } else {
            log.warn("Gateway — payment FAILED");
            return new GatewayResponse(false, null, FAILURE_CODE,
                    "Insufficient funds");
        }
    }

    // ----------------------------------------------------------------
    // Simulate refund processing
    // ----------------------------------------------------------------
    public GatewayResponse processRefund(String originalTxnId,
                                         BigDecimal amount) {
        log.info("Gateway — processing refund of {} for txn: {}",
                amount, originalTxnId);

        String refundId = "RFD-" + UUID.randomUUID()
                .toString().substring(0, 12).toUpperCase();
        log.info("Gateway — refund SUCCESS, refundId: {}", refundId);

        return new GatewayResponse(true, refundId, SUCCESS_CODE,
                "Refund processed successfully");
    }
}