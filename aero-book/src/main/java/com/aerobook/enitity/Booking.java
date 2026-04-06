package com.aerobook.enitity;


import com.aerobook.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pnr", nullable = false, unique = true)
    private String pnr;                     // e.g. "AERO-AB12CD"

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    private BookingType bookingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    // ── Booker (logged-in user) ───────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Outbound flight ───────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_flight_id", nullable = false)
    private Flight outboundFlight;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbound_seat_class", nullable = false)
    private SeatClass outboundSeatClass;

    @Column(name = "outbound_seat_number")
    private String outboundSeatNumber;

    @Column(name = "outbound_seat_hold_ref")
    private String outboundSeatHoldRef;

    // ── Return flight (nullable for ONE_WAY) ─────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_seat_class")
    private SeatClass returnSeatClass;

    @Column(name = "return_seat_number")
    private String returnSeatNumber;

    @Column(name = "return_seat_hold_ref")
    private String returnSeatHoldRef;

    // ── Passenger details ─────────────────────────────────────────────
    @Column(name = "passenger_first_name", nullable = false)
    private String passengerFirstName;

    @Column(name = "passenger_last_name", nullable = false)
    private String passengerLastName;

    @Column(name = "passenger_email", nullable = false)
    private String passengerEmail;

    @Column(name = "passenger_phone", nullable = false)
    private String passengerPhone;

    @Column(name = "passenger_dob")
    private java.time.LocalDate passengerDob;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "nationality")
    private String nationality;

    // ── Fare details ──────────────────────────────────────────────────
    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    // ── Cancellation ──────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason")
    private CancellationReason cancellationReason;

    @Column(name = "cancellation_remarks")
    private String cancellationRemarks;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ── Seat hold expiry ──────────────────────────────────────────────
    @Column(name = "seat_hold_expires_at")
    private LocalDateTime seatHoldExpiresAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // State machine — transition methods
    // ----------------------------------------------------------------

    public void lockSeats(String outboundHoldRef, String returnHoldRef,
                          LocalDateTime holdExpiry) {
        validateTransition(BookingStatus.PENDING, BookingStatus.SEAT_LOCKED);
        this.outboundSeatHoldRef = outboundHoldRef;
        this.returnSeatHoldRef   = returnHoldRef;
        this.seatHoldExpiresAt   = holdExpiry;
        this.status              = BookingStatus.SEAT_LOCKED;
    }

    public void initiatePayment() {
        validateTransition(BookingStatus.SEAT_LOCKED, BookingStatus.PAYMENT_INITIATED);
        this.status = BookingStatus.PAYMENT_INITIATED;
    }

    public void confirm(String outboundSeatNumber, String returnSeatNumber) {
        validateTransition(BookingStatus.PAYMENT_INITIATED, BookingStatus.CONFIRMED);
        this.outboundSeatNumber  = outboundSeatNumber;
        this.returnSeatNumber    = returnSeatNumber;
        this.outboundSeatHoldRef = null;
        this.returnSeatHoldRef   = null;
        this.seatHoldExpiresAt   = null;
        this.status              = BookingStatus.CONFIRMED;
    }

    public void cancel(CancellationReason reason, String remarks) {
        if (this.status == BookingStatus.CANCELLED
                || this.status == BookingStatus.EXPIRED) {
            throw new com.aerobook.exception.AeroBookException(
                    "Booking is already " + this.status,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "BOOKING_ALREADY_TERMINAL"
            );
        }
        this.status                = BookingStatus.CANCELLED;
        this.cancellationReason    = reason;
        this.cancellationRemarks   = remarks;
        this.cancelledAt           = LocalDateTime.now();
    }

    public void expire() {
        if (this.status != BookingStatus.SEAT_LOCKED
                && this.status != BookingStatus.PENDING) {
            return;
        }
        this.status             = BookingStatus.EXPIRED;
        this.cancellationReason = CancellationReason.SEAT_HOLD_EXPIRED;
        this.cancelledAt        = LocalDateTime.now();
    }

    public boolean isSeatHoldExpired() {
        return this.seatHoldExpiresAt != null
                && LocalDateTime.now().isAfter(this.seatHoldExpiresAt);
    }

    public boolean isActive() {
        return this.status != BookingStatus.CANCELLED
                && this.status != BookingStatus.EXPIRED;
    }

    // ----------------------------------------------------------------
    // Private — enforce valid state transitions
    // ----------------------------------------------------------------
    private void validateTransition(BookingStatus from, BookingStatus to) {
        if (this.status != from) {
            throw new com.aerobook.exception.AeroBookException(
                    "Invalid booking state transition: " + this.status
                            + " → " + to + ". Expected current status: " + from,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "INVALID_STATE_TRANSITION"
            );
        }
    }
}