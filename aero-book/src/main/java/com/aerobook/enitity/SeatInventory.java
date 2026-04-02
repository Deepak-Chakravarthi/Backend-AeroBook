package com.aerobook.enitity;


import com.aerobook.domain.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Class-level inventory per flight.
 * Tracks total, available, held, and booked seat counts.
 * @Version enables optimistic locking for concurrent booking.
 */
@Entity
@Table(name = "seat_inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"flight_id", "seat_class"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "held_seats", nullable = false)
    private Integer heldSeats;

    @Column(name = "booked_seats", nullable = false)
    private Integer bookedSeats;

    @Column(name = "blocked_seats", nullable = false)
    private Integer blockedSeats;

    // ----------------------------------------------------------------
    // Optimistic locking — prevents lost updates under concurrency
    // Two users booking last seat simultaneously:
    //   Thread 1 reads version=5, Thread 2 reads version=5
    //   Thread 1 updates → version becomes 6
    //   Thread 2 tries update with version=5 → OptimisticLockException
    // ----------------------------------------------------------------
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Business methods — inventory operations
    // ----------------------------------------------------------------

    public void holdSeats(int count) {
        validateSufficientAvailable(count);
        this.availableSeats -= count;
        this.heldSeats += count;
    }

    public void releaseHeldSeats(int count) {
        validateSufficientHeld(count);
        this.heldSeats -= count;
        this.availableSeats += count;
    }

    public void confirmBooking(int count) {
        validateSufficientHeld(count);
        this.heldSeats -= count;
        this.bookedSeats += count;
    }

    public void cancelBooking(int count) {
        if (this.bookedSeats < count) {
            throw new IllegalStateException("Cannot cancel more seats than booked");
        }
        this.bookedSeats -= count;
        this.availableSeats += count;
    }

    public boolean hasSufficientAvailability(int count) {
        return this.availableSeats >= count;
    }

    private void validateSufficientAvailable(int count) {
        if (this.availableSeats < count) {
            throw new com.aerobook.exception.AeroBookException(
                    "Insufficient available seats. Requested: " + count
                            + ", Available: " + this.availableSeats,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "INSUFFICIENT_SEATS"
            );
        }
    }

    private void validateSufficientHeld(int count) {
        if (this.heldSeats < count) {
            throw new IllegalStateException(
                    "Cannot release more held seats than currently held");
        }
    }
}