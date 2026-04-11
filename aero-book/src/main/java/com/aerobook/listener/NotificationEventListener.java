package com.aerobook.listener;


import com.aerobook.entity.*;
import com.aerobook.domain.enums.NotificationType;
import com.aerobook.event.*;
import com.aerobook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    // ----------------------------------------------------------------
    // Booking confirmed → send confirmation email
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        log.info("NotificationEventListener — booking confirmed: {}",
                booking.getPnr());

        notificationService.processNotification(
                NotificationEvent.builder()
                        .source(this)
                        .userId(booking.getUser().getId())
                        .recipientEmail(booking.getPassengerEmail())
                        .type(NotificationType.BOOKING_CONFIRMED)
                        .referenceId(booking.getPnr())
                        .templateVariables(Map.of(
                                "passengerName",   booking.getPassengerFirstName()
                                        + " " + booking.getPassengerLastName(),
                                "pnr",             booking.getPnr(),
                                "flightNumber",    booking.getOutboundFlight()
                                        .getFlightNumber(),
                                "originCode",      booking.getOutboundFlight()
                                        .getRoute().getOrigin().getIataCode(),
                                "originCity",      booking.getOutboundFlight()
                                        .getRoute().getOrigin().getCity(),
                                "destinationCode", booking.getOutboundFlight()
                                        .getRoute().getDestination().getIataCode(),
                                "destinationCity", booking.getOutboundFlight()
                                        .getRoute().getDestination().getCity(),
                                "departureTime",   booking.getOutboundFlight()
                                        .getDepartureTime().toString(),
                                "seatClass",       booking.getOutboundSeatClass()
                                        .name(),
                                "totalFare",       booking.getTotalFare()
                                        .toString()
                        ))
                        .build()
        );
    }

    // ----------------------------------------------------------------
    // Payment success → send payment receipt
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        Payment payment = event.getPayment();
        log.info("NotificationEventListener — payment success: {}",
                payment.getPaymentReference());

        notificationService.processNotification(
                NotificationEvent.builder()
                        .source(this)
                        .userId(payment.getUser().getId())
                        .recipientEmail(payment.getBooking().getPassengerEmail())
                        .type(NotificationType.PAYMENT_SUCCESS)
                        .referenceId(payment.getPaymentReference())
                        .templateVariables(Map.of(
                                "pnr",              payment.getBooking().getPnr(),
                                "paymentReference", payment.getPaymentReference(),
                                "amount",           payment.getAmount().toString(),
                                "paymentMethod",    payment.getPaymentMethod().name(),
                                "transactionId",    payment.getGatewayTransactionId()
                                        != null ? payment.getGatewayTransactionId() : "N/A"
                        ))
                        .build()
        );
    }

    // ----------------------------------------------------------------
    // Flight delayed → notify all affected passengers
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onFlightDelayed(FlightStatusChangedEvent event) {
        if (event.getNewStatus() !=
                com.aerobook.domain.enums.FlightStatus.DELAYED) return;

        Flight flight = event.getFlight();
        log.info("NotificationEventListener — flight delayed: {}",
                flight.getFlightNumber());

        // Notify all confirmed passengers on this flight
        event.getAffectedBookings().forEach(booking ->
                notificationService.processNotification(
                        NotificationEvent.builder()
                                .source(this)
                                .userId(booking.getUser().getId())
                                .recipientEmail(booking.getPassengerEmail())
                                .type(NotificationType.FLIGHT_DELAYED)
                                .referenceId(flight.getFlightNumber())
                                .templateVariables(Map.of(
                                        "pnr",              booking.getPnr(),
                                        "flightNumber",     flight.getFlightNumber(),
                                        "delayMinutes",     flight.getDelayMinutes()
                                                .toString(),
                                        "originalDeparture",flight.getDepartureTime()
                                                .toString(),
                                        "newDeparture",     flight.getDepartureTime()
                                                .plusMinutes(flight.getDelayMinutes())
                                                .toString(),
                                        "reason",           event.getReason() != null
                                                ? event.getReason()
                                                : "Operational reasons"
                                ))
                                .build()
                )
        );
    }

    // ----------------------------------------------------------------
    // Tier upgraded → congratulations email
    // ----------------------------------------------------------------
    @Async
    @EventListener
    public void onTierUpgraded(TierUpgradedEvent event) {
        LoyaltyAccount account = event.getLoyaltyAccount();
        log.info("NotificationEventListener — tier upgraded: {} → {}",
                event.getPreviousTier(), event.getNewTier());

        notificationService.processNotification(
                NotificationEvent.builder()
                        .source(this)
                        .userId(account.getUser().getId())
                        .recipientEmail(account.getUser().getEmail())
                        .type(NotificationType.TIER_UPGRADED)
                        .referenceId(account.getMembershipNumber())
                        .templateVariables(Map.of(
                                "passengerName",  account.getUser().getFirstName()
                                        + " " + account.getUser().getLastName(),
                                "previousTier",   event.getPreviousTier().name(),
                                "newTier",        event.getNewTier().name(),
                                "totalMiles",     account.getTotalMiles().toString(),
                                "bonusMiles",     "0"
                        ))
                        .build()
        );
    }
}