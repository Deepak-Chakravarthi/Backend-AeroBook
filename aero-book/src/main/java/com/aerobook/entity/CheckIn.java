package com.aerobook.entity;


import com.aerobook.domain.enums.CheckInStatus;
import com.aerobook.domain.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CheckInStatus status;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "boarding_group")
    private String boardingGroup;           // e.g. "A", "B", "C"

    @OneToOne(mappedBy = "checkIn",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private BoardingPass boardingPass;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Business methods
    // ----------------------------------------------------------------

    public void complete(String seatNumber, String boardingGroup) {
        this.seatNumber    = seatNumber;
        this.boardingGroup = boardingGroup;
        this.status        = CheckInStatus.CHECKED_IN;
        this.checkedInAt   = LocalDateTime.now();
    }

    public void markBoardingPassIssued() {
        this.status = CheckInStatus.BOARDING_PASS_ISSUED;
    }

    public void markBoarded() {
        this.status = CheckInStatus.BOARDED;
    }

    public void markNoShow() {
        this.status = CheckInStatus.NO_SHOW;
    }
}
