package com.aerobook.entity;


import com.aerobook.domain.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loyalty_accounts")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "membership_number", nullable = false, unique = true)
    private String membershipNumber;        // e.g. "AERO-FF-AB12CD"

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private LoyaltyTier tier;

    @Column(name = "total_miles", nullable = false)
    private Long totalMiles;                // lifetime miles earned

    @Column(name = "available_miles", nullable = false)
    private Long availableMiles;            // miles available to redeem

    @Column(name = "tier_qualifying_miles", nullable = false)
    private Long tierQualifyingMiles;       // miles counted toward tier status

    @Column(name = "flights_completed", nullable = false)
    private Integer flightsCompleted;

    @Column(name = "tier_upgraded_at")
    private LocalDateTime tierUpgradedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @OneToMany(mappedBy = "loyaltyAccount",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MileTransaction> transactions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Business methods
    // ----------------------------------------------------------------

    public void earnMiles(long miles) {
        this.totalMiles           += miles;
        this.availableMiles       += miles;
        this.tierQualifyingMiles  += miles;
        this.lastActivityAt        = LocalDateTime.now();
    }

    public void redeemMiles(long miles) {
        if (this.availableMiles < miles) {
            throw new com.aerobook.exception.AeroBookException(
                    "Insufficient miles. Available: " + this.availableMiles
                            + ", Requested: " + miles,
                    org.springframework.http.HttpStatus.CONFLICT,
                    "INSUFFICIENT_MILES"
            );
        }
        this.availableMiles  -= miles;
        this.lastActivityAt   = LocalDateTime.now();
    }

    public void adjustMiles(long miles) {
        this.availableMiles      += miles;
        this.totalMiles          += miles;
        this.lastActivityAt       = LocalDateTime.now();
    }

    public void incrementFlightsCompleted() {
        this.flightsCompleted++;
    }

    public LoyaltyTier calculateTier() {
        if (this.tierQualifyingMiles >= 100_000) return LoyaltyTier.PLATINUM;
        if (this.tierQualifyingMiles >= 50_000)  return LoyaltyTier.GOLD;
        if (this.tierQualifyingMiles >= 10_000)  return LoyaltyTier.SILVER;
        return LoyaltyTier.BLUE;
    }

    public boolean isTierUpgradeEligible() {
        return calculateTier() != this.tier;
    }
}