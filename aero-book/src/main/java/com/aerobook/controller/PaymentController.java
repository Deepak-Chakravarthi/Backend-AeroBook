package com.aerobook.controller;


import com.aerobook.domain.dto.request.get.PaymentGetRequest;
import com.aerobook.domain.dto.request.PaymentRequest;
import com.aerobook.domain.dto.request.RefundRequest;
import com.aerobook.domain.dto.response.PaymentResponse;
import com.aerobook.domain.dto.response.RefundResponse;
import com.aerobook.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    // ── Admin — view all payments ─────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String paymentReference,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            Pageable pageable) {

        PaymentGetRequest request = PaymentGetRequest.builder()
                .id(id).paymentReference(paymentReference)
                .bookingId(bookingId).userId(userId)
                .status(status).paymentMethod(paymentMethod)
                .build();

        return ResponseEntity.ok(paymentService.getPayments(request, pageable));
    }

    // ── Get by id ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // ── Get by booking ────────────────────────────────────────────────
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBooking(bookingId));
    }

    // ── Initiate payment — requires Idempotency-Key header ───────────
    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(idempotencyKey, request));
    }

    // ── Process payment (calls gateway) ──────────────────────────────
    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.processPayment(idempotencyKey, id));
    }

    // ── Initiate refund ───────────────────────────────────────────────
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<RefundResponse> initiateRefund(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @Valid @RequestBody RefundRequest request) {

        return ResponseEntity.ok(
                paymentService.initiateRefund(idempotencyKey, request));
    }
}