package com.aerobook.enitity;


import com.aerobook.domain.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;

/**
 * The type Aircraft seat config.
 */
@Entity
@Table(name = "aircraft_seat_config", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"aircraft_id", "seat_class"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AircraftSeatConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Column(name = "rows")
    private Integer rows;

    @Column(name = "seats_per_row")
    private Integer seatsPerRow;
}