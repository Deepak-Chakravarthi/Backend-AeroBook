package com.aerobook.domain.enums;


public enum MileTransactionType {
    EARNED,         // miles earned on flight completion
    REDEEMED,       // miles used for booking discount
    EXPIRED,        // miles expired after inactivity
    ADJUSTED,       // manual adjustment by admin
    BONUS           // bonus miles on tier upgrade or promotions
}