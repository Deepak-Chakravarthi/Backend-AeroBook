package com.aerobook.domain.enums;

public enum SeatStatus {
    AVAILABLE,
    HELD,           // temporarily held during booking initiation
    BOOKED,         // confirmed after payment
    BLOCKED,        // blocked by airline (crew, broken seat etc.)
    CHECKED_IN      // passenger checked in
}
