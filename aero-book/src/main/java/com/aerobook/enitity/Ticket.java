package com.aerobook.enitity;

import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One ticket per passenger per flight leg.
 * ONE_WAY booking → 1 ticket
 * RETURN booking  → 2 tickets (outbound + return)
 */
@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ticket_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;            // e.g. "TKT-20260316-AB12CD-1"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Column(name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Column(name = "fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Column(name = "is_return_leg", nullable = false)
    private Boolean isReturnLeg;            // true = return flight ticket

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Business methods
    // ----------------------------------------------------------------

    public void cancel() {
        if (this.status == TicketStatus.CANCELLED
                || this.status == TicketStatus.REFUNDED) {
            throw new com.aerobook.exception.AeroBookException(
                    "Ticket " + ticketNumber + " is already " + this.status,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "TICKET_ALREADY_TERMINAL"
            );
        }
        this.status      = TicketStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void markCheckedIn() {
        this.status = TicketStatus.CHECKED_IN;
    }

    public void markBoarded() {
        this.status = TicketStatus.BOARDED;
    }

    public void markNoShow() {
        this.status = TicketStatus.NO_SHOW;
    }
}
