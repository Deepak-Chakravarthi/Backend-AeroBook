package com.aerobook.domain.enums;


public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTERED    // moved to DLQ after max retries
}