package com.aerobook.domain.enums;


public enum TicketStatus {
    ISSUED,         // ticket generated after booking confirmed
    CHECKED_IN,     // passenger checked in
    BOARDED,        // passenger boarded
    NO_SHOW,        // passenger did not board
    CANCELLED,      // ticket cancelled
    REFUNDED        // ticket refunded
}
