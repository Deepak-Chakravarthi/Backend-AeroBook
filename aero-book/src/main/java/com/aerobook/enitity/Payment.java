package com.aerobook.enitity;


import com.aerobook.domain.enums.PaymentMethod;
import com.aerobook.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Idempotency key — prevents duplicate payments ─────────────────
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "payment_reference", unique = true)
    private String paymentReference;        // e.g. "PAY-20260316-AB12CD"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;                // "INR"

    // Gateway response fields
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @Column(name = "gateway_response_code")
    private String gatewayResponseCode;

    @Column(name = "gateway_response_message")
    private String gatewayResponseMessage;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "payment",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Refund> refunds = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Business methods
    // ----------------------------------------------------------------

    public void markProcessing() {
        validateTransition(PaymentStatus.INITIATED, PaymentStatus.PROCESSING);
        this.status = PaymentStatus.PROCESSING;
    }

    public void markSuccess(String gatewayTransactionId,
                            String responseCode) {
        validateTransition(PaymentStatus.PROCESSING, PaymentStatus.SUCCESS);
        this.status                = PaymentStatus.SUCCESS;
        this.gatewayTransactionId  = gatewayTransactionId;
        this.gatewayResponseCode   = responseCode;
        this.paidAt                = LocalDateTime.now();
    }

    public void markFailed(String reason, String responseCode) {
        if (this.status != PaymentStatus.INITIATED
                && this.status != PaymentStatus.PROCESSING) {
            throw new com.aerobook.exception.AeroBookException(
                    "Cannot mark payment as failed from status: " + this.status,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "INVALID_STATE_TRANSITION"
            );
        }
        this.status                  = PaymentStatus.FAILED;
        this.failureReason           = reason;
        this.gatewayResponseCode     = responseCode;
        this.gatewayResponseMessage  = reason;
    }

    public void initiateRefund() {
        if (this.status != PaymentStatus.SUCCESS
                && this.status != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new com.aerobook.exception.AeroBookException(
                    "Refund can only be initiated for SUCCESS payments. " +
                            "Current status: " + this.status,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "REFUND_NOT_ALLOWED"
            );
        }
        this.status = PaymentStatus.REFUND_INITIATED;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void markPartiallyRefunded() {
        this.status = PaymentStatus.PARTIALLY_REFUNDED;
    }

    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCESS
                || this.status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    private void validateTransition(PaymentStatus from, PaymentStatus to) {
        if (this.status != from) {
            throw new com.aerobook.exception.AeroBookException(
                    "Invalid payment state transition: "
                            + this.status + " → " + to
                            + ". Expected: " + from,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "INVALID_STATE_TRANSITION"
            );
        }
    }
}