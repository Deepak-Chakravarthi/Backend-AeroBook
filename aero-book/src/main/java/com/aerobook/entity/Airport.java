package com.aerobook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * The type Airport.
 */
// Airport is reference data needed by Route
@Entity
@Table(name = "airports", uniqueConstraints = {
        @UniqueConstraint(columnNames = "iata_code")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, length = 3)
    private String iataCode;               // e.g. "DEL", "BOM", "BLR"

    @Column(name = "name", nullable = false)
    private String name;                   // e.g. "Indira Gandhi International Airport"

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "timezone", nullable = false)
    private String timezone;               // e.g. "Asia/Kolkata"

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}