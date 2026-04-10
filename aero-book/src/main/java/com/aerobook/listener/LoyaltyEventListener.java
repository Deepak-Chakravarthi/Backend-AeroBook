package com.aerobook.listener;


import com.aerobook.entity.Booking;
import com.aerobook.entity.Flight;
import com.aerobook.event.BookingConfirmedEvent;
import com.aerobook.event.FlightCompletedEvent;
import com.aerobook.event.TierUpgradedEvent;
import com.aerobook.service.FlightService;
import com.aerobook.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoyaltyEventListener {

    private final LoyaltyService loyaltyService;
    private final FlightService flightService;

    public LoyaltyEventListener(LoyaltyService loyaltyService, @Lazy FlightService flightService) {
        this.loyaltyService = loyaltyService;
        this.flightService = flightService;
    }

    // ----------------------------------------------------------------
    // Flight completed — award miles to the booking user
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onFlightCompleted(FlightCompletedEvent event) {
        Flight flight = event.getFlight();
        log.info("LoyaltyEventListener — awarding miles for flight: {}",
                flight.getFlightNumber());
        try {
            flightService.awardMilesForCompletedFlight(flight);
        } catch (Exception e) {
            log.error("Failed to award miles for flight: {} — {}",
                    flight.getFlightNumber(), e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Booking confirmed — create loyalty account if not exists
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        Long userId     = booking.getUser().getId();

        log.info("LoyaltyEventListener — booking confirmed: {}, user: {}",
                booking.getPnr(), userId);

        try {
            // Ensure loyalty account exists for user
            loyaltyService.getMyAccount(userId);
            log.info("Loyalty account verified for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to process loyalty for booking: {} — {}",
                    booking.getPnr(), e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Tier upgraded — log and notify
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onTierUpgraded(TierUpgradedEvent event) {
        log.info("TierUpgradedEvent — user: {}, {} → {}",
                event.getLoyaltyAccount().getUser().getId(),
                event.getPreviousTier(),
                event.getNewTier());
        // Notification module picks this up separately
    }
}