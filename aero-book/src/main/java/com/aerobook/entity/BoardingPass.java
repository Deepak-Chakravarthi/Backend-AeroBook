package com.aerobook.entity;


import com.aerobook.domain.enums.BoardingPassStatus;
import com.aerobook.domain.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "boarding_passes")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BoardingPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "boarding_pass_number", nullable = false, unique = true)
    private String boardingPassNumber;      // e.g. "BP-20260316-AB12CD-1A"

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckIn checkIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    @Column(name = "origin_code", nullable = false)
    private String originCode;

    @Column(name = "destination_code", nullable = false)
    private String destinationCode;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Column(name = "gate")
    private String gate;

    @Column(name = "terminal")
    private String terminal;

    @Column(name = "boarding_group")
    private String boardingGroup;

    @Column(name = "boarding_time", nullable = false)
    private LocalDateTime boardingTime;     // departure - 45 min

    @Column(name = "barcode", nullable = false)
    private String barcode;                 // unique barcode string

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BoardingPassStatus status;

    @Column(name = "pdf_path")
    private String pdfPath;                 // stored PDF file path

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------------
    // Business methods
    // ----------------------------------------------------------------

    public void markUsed() {
        this.status = BoardingPassStatus.USED;
    }

    public void cancel() {
        this.status = BoardingPassStatus.CANCELLED;
    }

    public void expire() {
        this.status = BoardingPassStatus.EXPIRED;
    }
}
