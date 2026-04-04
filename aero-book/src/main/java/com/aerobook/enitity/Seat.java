package com.aerobook.enitity;


import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.SeatStatus;
import com.aerobook.domain.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Individual physical seat on a specific flight.
 * e.g. Flight AI-101 on 2026-03-16, Seat 12A (Economy, Window)
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"flight_id", "seat_number"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;          // e.g. "12A", "3C"

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;          // e.g. 12

    @Column(name = "seat_letter", nullable = false)
    private String seatLetter;          // e.g. "A"

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;          // WINDOW, MIDDLE, AISLE

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;

    @Column(name = "held_by_booking_ref")
    private String heldByBookingRef;    // booking reference holding this seat

    @Column(name = "held_until")
    private LocalDateTime heldUntil;    // TTL — when hold expires

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Business methods
    // ----------------------------------------------------------------

    public void hold(String bookingRef, LocalDateTime until) {
        validateAvailable();
        this.status          = SeatStatus.HELD;
        this.heldByBookingRef = bookingRef;
        this.heldUntil       = until;
    }

    public void confirmBooking() {
        if (this.status != SeatStatus.HELD) {
            throw new com.aerobook.exception.AeroBookException(
                    "Seat " + seatNumber + " is not in HELD status",
                    org.springframework.http.HttpStatus.CONFLICT,
                    "SEAT_NOT_HELD"
            );
        }
        this.status           = SeatStatus.BOOKED;
        this.heldByBookingRef = null;
        this.heldUntil        = null;
    }

    public void release() {
        this.status           = SeatStatus.AVAILABLE;
        this.heldByBookingRef = null;
        this.heldUntil        = null;
    }

    public void block() {
        this.status = SeatStatus.BLOCKED;
    }

    public boolean isHoldExpired() {
        return this.status == SeatStatus.HELD
                && this.heldUntil != null
                && LocalDateTime.now().isAfter(this.heldUntil);
    }

    private void validateAvailable() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new com.aerobook.exception.AeroBookException(
                    "Seat " + seatNumber + " is not available. Current status: " + status,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "SEAT_NOT_AVAILABLE"
            );
        }
    }
}