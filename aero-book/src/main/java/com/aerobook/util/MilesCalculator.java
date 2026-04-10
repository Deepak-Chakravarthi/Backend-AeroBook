package com.aerobook.util;


import com.aerobook.domain.enums.LoyaltyTier;
import com.aerobook.domain.enums.SeatClass;
import org.springframework.stereotype.Component;

@Component
public class MilesCalculator {

    // Base miles per km flown
    private static final double BASE_MILES_PER_KM = 1.0;

    // Seat class multipliers
    private static final double ECONOMY_MULTIPLIER  = 1.0;
    private static final double BUSINESS_MULTIPLIER = 2.0;
    private static final double FIRST_MULTIPLIER    = 3.0;

    // Tier bonus multipliers
    private static final double BLUE_BONUS      = 0.0;
    private static final double SILVER_BONUS    = 0.25;
    private static final double GOLD_BONUS      = 0.50;
    private static final double PLATINUM_BONUS  = 1.00;

    // Tier upgrade bonus miles
    private static final long SILVER_UPGRADE_BONUS   = 1_000L;
    private static final long GOLD_UPGRADE_BONUS     = 5_000L;
    private static final long PLATINUM_UPGRADE_BONUS = 10_000L;

    // ----------------------------------------------------------------
    // Calculate miles earned for a flight
    // ----------------------------------------------------------------
    public long calculateEarnedMiles(int distanceKm,
                                     SeatClass seatClass,
                                     LoyaltyTier tier) {
        double baseMiles  = distanceKm * BASE_MILES_PER_KM;
        double classMult  = resolveClassMultiplier(seatClass);
        double tierBonus  = resolveTierBonus(tier);

        long earned = Math.round(baseMiles * classMult * (1 + tierBonus));

        // Minimum 500 miles per flight
        return Math.max(earned, 500L);
    }

    // ----------------------------------------------------------------
    // Tier upgrade bonus
    // ----------------------------------------------------------------
    public long calculateTierUpgradeBonus(LoyaltyTier newTier) {
        return switch (newTier) {
            case SILVER   -> SILVER_UPGRADE_BONUS;
            case GOLD     -> GOLD_UPGRADE_BONUS;
            case PLATINUM -> PLATINUM_UPGRADE_BONUS;
            default       -> 0L;
        };
    }

    // ----------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------
    private double resolveClassMultiplier(SeatClass seatClass) {
        return switch (seatClass) {
            case ECONOMY  -> ECONOMY_MULTIPLIER;
            case BUSINESS -> BUSINESS_MULTIPLIER;
            case FIRST    -> FIRST_MULTIPLIER;
        };
    }

    private double resolveTierBonus(LoyaltyTier tier) {
        return switch (tier) {
            case BLUE     -> BLUE_BONUS;
            case SILVER   -> SILVER_BONUS;
            case GOLD     -> GOLD_BONUS;
            case PLATINUM -> PLATINUM_BONUS;
        };
    }
}