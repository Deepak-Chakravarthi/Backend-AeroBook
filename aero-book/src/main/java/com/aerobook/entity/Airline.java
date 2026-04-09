package com.aerobook.entity;

import com.aerobook.domain.enums.AirlineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Airline.
 */
@Entity
@Table(name = "airlines", uniqueConstraints = {
        @UniqueConstraint(columnNames = "iata_code")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "iata_code", nullable = false, length = 3)
    private String iataCode;                // e.g. "AI", "6E", "SG"

    @Column(name = "icao_code", length = 4)
    private String icaoCode;                // e.g. "AIC", "IGO"

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirlineStatus status;

    @OneToMany(mappedBy = "airline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aircraft> aircraft = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}