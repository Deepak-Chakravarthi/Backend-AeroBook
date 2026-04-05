package com.aerobook.domain.enums;

public enum BookingStatus {
    PENDING,            // booking initiated, no seats held yet
    SEAT_LOCKED,        // seats held, awaiting payment
    PAYMENT_INITIATED,  // payment started
    CONFIRMED,          // payment successful, booking confirmed
    CANCELLED,          // cancelled by user or system
    EXPIRED             // seat hold TTL expired, booking auto-cancelled
}