package com.aerobook.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Enforces module boundary rules at runtime.
 * Active only in 'dev' profile — zero overhead in prod.
 *
 * Rule: services may only inject repositories from their own module.
 * Cross-module data access must go through service interfaces.
 *
 * Violations throw IllegalStateException immediately —
 * find them before microservices split, not after.
 */
@Slf4j
@Aspect
@Component
@Profile("dev")
public class ModuleBoundaryAspect {

    // ----------------------------------------------------------------
    // FlightService must not directly access BookingRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.FlightService) && " +
            "target(com.aerobook.repository.BookingRepository)")
    public void enforceFlightServiceBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: FlightService must not directly access " +
                        "BookingRepository. Use BookingService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // BookingService must not directly access FlightRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.BookingService) && " +
            "target(com.aerobook.repository.FlightRepository)")
    public void enforceBookingServiceFlightBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: BookingService must not directly access " +
                        "FlightRepository. Use FlightService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // BookingService must not directly access UserRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.BookingService) && " +
            "target(com.aerobook.repository.UserRepository)")
    public void enforceBookingServiceUserBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: BookingService must not directly access " +
                        "UserRepository. Use UserService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // PaymentService must not directly access BookingRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.PaymentService) && " +
            "target(com.aerobook.repository.BookingRepository)")
    public void enforcePaymentServiceBookingBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: PaymentService must not directly access " +
                        "BookingRepository. Use BookingService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // PaymentService must not directly access TicketRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.PaymentService) && " +
            "target(com.aerobook.repository.TicketRepository)")
    public void enforcePaymentServiceTicketBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: PaymentService must not directly access " +
                        "TicketRepository. Use TicketService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // LoyaltyService must not directly access FlightRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.LoyaltyService) && " +
            "target(com.aerobook.repository.FlightRepository)")
    public void enforceLoyaltyServiceFlightBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: LoyaltyService must not directly access " +
                        "FlightRepository. Use FlightService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // LoyaltyService must not directly access BookingRepository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.LoyaltyService) && " +
            "target(com.aerobook.repository.BookingRepository)")
    public void enforceLoyaltyServiceBookingBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: LoyaltyService must not directly access " +
                        "BookingRepository. Use BookingService instead. " +
                        "Method: " + jp.getSignature().getName());
    }

    // ----------------------------------------------------------------
    // NotificationService must not access any domain repository
    // ----------------------------------------------------------------
    @Before("within(com.aerobook.service.NotificationService) && " +
            "(" +
            "target(com.aerobook.repository.FlightRepository) || " +
            "target(com.aerobook.repository.BookingRepository) || " +
            "target(com.aerobook.repository.PaymentRepository) || " +
            "target(com.aerobook.repository.UserRepository)" +
            ")")
    public void enforceNotificationServiceBoundary(JoinPoint jp) {
        throw new IllegalStateException(
                "BOUNDARY VIOLATION: NotificationService must not access " +
                        "domain repositories directly. Receive data via events only. " +
                        "Method: " + jp.getSignature().getName());
    }
}