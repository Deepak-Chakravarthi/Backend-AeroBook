package com.aerobook.service;


import com.aerobook.domain.dto.request.*;
import com.aerobook.domain.dto.request.get.PaymentGetRequest;
import com.aerobook.domain.dto.response.PaymentResponse;
import com.aerobook.domain.dto.response.RefundResponse;
import com.aerobook.domain.enums.*;
import com.aerobook.entity.Booking;
import com.aerobook.entity.Payment;
import com.aerobook.entity.Refund;
import com.aerobook.entity.User;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.PaymentMapper;
import com.aerobook.repository.PaymentRepository;
import com.aerobook.repository.RefundRepository;
import com.aerobook.security.UserPrincipal;
import com.aerobook.util.PaymentReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository          paymentRepository;
    private final RefundRepository           refundRepository;
    private final PaymentMapper              paymentMapper;
    private final PaymentReferenceGenerator  referenceGenerator;
    private final IdempotencyService         idempotencyService;
    private final PaymentGatewayService      gatewayService;
    private final BookingService             bookingService;
    private final UserService                userService;
    private final TicketService              ticketService;

    // ----------------------------------------------------------------
    // Get payments — filterable
    // ----------------------------------------------------------------
    public List<PaymentResponse> getPayments(PaymentGetRequest request,
                                             Pageable pageable) {
        return paymentRepository.findAll(request.toSpecification(), pageable)
                .map(paymentMapper::toResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Get payment by id
    // ----------------------------------------------------------------
    public PaymentResponse getPaymentById(Long id) {
        return paymentMapper.toResponse(findPaymentById(id));
    }

    // ----------------------------------------------------------------
    // Get payments by booking
    // ----------------------------------------------------------------
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findAllByBookingId(bookingId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Initiate payment — with idempotency
    // ----------------------------------------------------------------
    @Transactional
    public PaymentResponse initiatePayment(String idempotencyKey,
                                           PaymentRequest request) {
        // Step 1 — check idempotency
        var existing = idempotencyService.findExistingRecord(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate payment request — returning cached response, key: {}",
                    idempotencyKey);
            PaymentResponse cached = idempotencyService.parseResponse(
                    existing.get(), PaymentResponse.class);
            if (cached != null) return cached;
        }

        // Step 2 — validate booking
        Booking booking = bookingService.findBookingById(request.bookingId());
        validateBookingForPayment(booking);

        User user = userService.findUserById(resolveCurrentUserId());

        // Step 3 — check no existing successful payment
        paymentRepository.findByBookingIdAndStatus(
                        request.bookingId(), PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw new AeroBookException(
                            "Payment already completed for booking: "
                                    + booking.getPnr(),
                            HttpStatus.CONFLICT,
                            "PAYMENT_ALREADY_EXISTS"
                    );
                });

        // Step 4 — create payment record
        String paymentRef = referenceGenerator.generatePaymentRef(booking.getPnr());

        Payment payment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .paymentReference(paymentRef)
                .booking(booking)
                .user(user)
                .status(PaymentStatus.INITIATED)
                .paymentMethod(request.paymentMethod())
                .amount(booking.getTotalFare())
                .currency("INR")
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        PaymentResponse response = paymentMapper.toResponse(saved);

        // Step 5 — store idempotency record
        idempotencyService.store(idempotencyKey, response,
                HttpStatus.CREATED.value(), "/payments");

        log.info("Payment initiated — ref: {}, booking: {}",
                paymentRef, booking.getPnr());

        return response;
    }

    // ----------------------------------------------------------------
    // Process payment — calls gateway
    // ----------------------------------------------------------------
    @Transactional
    public PaymentResponse processPayment(String idempotencyKey, Long paymentId) {
        // Idempotency check
        var existing = idempotencyService.findExistingRecord(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate process request — key: {}", idempotencyKey);
            PaymentResponse cached = idempotencyService.parseResponse(
                    existing.get(), PaymentResponse.class);
            if (cached != null) return cached;
        }

        Payment payment = findPaymentById(paymentId);
        payment.markProcessing();
        paymentRepository.save(payment);

        // Call gateway
        PaymentGatewayService.GatewayResponse gatewayResponse =
                gatewayService.processPayment(
                        payment.getPaymentMethod(),
                        payment.getAmount(),
                        payment.getCurrency()
                );

        if (gatewayResponse.success()) {
            // Payment success flow
            payment.markSuccess(
                    gatewayResponse.transactionId(),
                    gatewayResponse.responseCode()
            );
            paymentRepository.save(payment);

            // Confirm booking
            bookingService.confirmBooking(payment.getBooking().getId());

            // Issue tickets
            ticketService.issueTickets(payment.getBooking().getId());

            log.info("Payment SUCCESS — ref: {}, txn: {}",
                    payment.getPaymentReference(),
                    gatewayResponse.transactionId());
        } else {
            // Payment failed flow
            payment.markFailed(
                    gatewayResponse.message(),
                    gatewayResponse.responseCode()
            );
            paymentRepository.save(payment);

            // Cancel booking on payment failure
            bookingService.cancelBooking(
                    payment.getBooking().getId(),
                    new BookingCancelRequest(
                            CancellationReason.PAYMENT_FAILED,
                            "Payment failed: " + gatewayResponse.message()
                    )
            );

            log.warn("Payment FAILED — ref: {}, reason: {}",
                    payment.getPaymentReference(), gatewayResponse.message());
        }

        PaymentResponse response = paymentMapper.toResponse(
                paymentRepository.findByIdWithDetails(paymentId).orElseThrow());

        idempotencyService.store(idempotencyKey, response,
                HttpStatus.OK.value(), "/payments/" + paymentId + "/process");

        return response;
    }

    // ----------------------------------------------------------------
    // Initiate refund
    // ----------------------------------------------------------------
    @Transactional
    public RefundResponse initiateRefund(String idempotencyKey,
                                         RefundRequest request) {
        // Idempotency check
        var existing = idempotencyService.findExistingRecord(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate refund request — key: {}", idempotencyKey);
            RefundResponse cached = idempotencyService.parseResponse(
                    existing.get(), RefundResponse.class);
            if (cached != null) return cached;
        }

        Payment payment = findPaymentById(request.paymentId());
        validateRefundAmount(payment, request.amount());

        payment.initiateRefund();
        paymentRepository.save(payment);

        String refundRef = referenceGenerator.generateRefundRef(
                payment.getBooking().getPnr());

        Refund refund = Refund.builder()
                .refundReference(refundRef)
                .payment(payment)
                .booking(payment.getBooking())
                .status(RefundStatus.INITIATED)
                .reason(request.reason())
                .amount(request.amount())
                .remarks(request.remarks())
                .createdAt(LocalDateTime.now())
                .build();

        Refund saved = refundRepository.save(refund);

        // Process refund via gateway
        processRefundViaGateway(saved, payment);

        RefundResponse response = paymentMapper.toRefundResponse(
                refundRepository.findByIdWithDetails(saved.getId()).orElseThrow());

        idempotencyService.store(idempotencyKey, response,
                HttpStatus.OK.value(), "/payments/refund");

        log.info("Refund initiated — ref: {}, amount: {}",
                refundRef, request.amount());

        return response;
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------
    public Payment findPaymentById(Long id) {
        return paymentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void validateBookingForPayment(Booking booking) {
        if (booking.getStatus() != BookingStatus.PAYMENT_INITIATED) {
            throw new AeroBookException(
                    "Booking must be in PAYMENT_INITIATED status. " +
                            "Current: " + booking.getStatus(),
                    HttpStatus.CONFLICT,
                    "INVALID_BOOKING_STATUS"
            );
        }
    }

    private void validateRefundAmount(Payment payment, BigDecimal refundAmount) {
        BigDecimal alreadyRefunded = refundRepository
                .sumRefundedAmountByPayment(payment.getId());

        BigDecimal remainingRefundable = payment.getAmount()
                .subtract(alreadyRefunded);

        if (refundAmount.compareTo(remainingRefundable) > 0) {
            throw new AeroBookException(
                    "Refund amount " + refundAmount
                            + " exceeds refundable amount " + remainingRefundable,
                    HttpStatus.BAD_REQUEST,
                    "REFUND_AMOUNT_EXCEEDS_LIMIT"
            );
        }
    }

    private void processRefundViaGateway(Refund refund, Payment payment) {
        refund.markProcessing();
        refundRepository.save(refund);

        PaymentGatewayService.GatewayResponse gatewayResponse =
                gatewayService.processRefund(
                        payment.getGatewayTransactionId(),
                        refund.getAmount()
                );

        if (gatewayResponse.success()) {
            refund.markSuccess(gatewayResponse.transactionId());

            BigDecimal totalRefunded = refundRepository
                    .sumRefundedAmountByPayment(payment.getId());

            if (totalRefunded.compareTo(payment.getAmount()) >= 0) {
                payment.markRefunded();
            } else {
                payment.markPartiallyRefunded();
            }

            // Cancel tickets if full refund
            if (payment.getStatus() == PaymentStatus.REFUNDED) {
                ticketService.cancelTickets(payment.getBooking().getId());
            }
        } else {
            refund.markFailed();
            payment.initiateRefund(); // revert to previous state
        }

        refundRepository.save(refund);
        paymentRepository.save(payment);
    }

    private Long resolveCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }
}