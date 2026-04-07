package com.aerobook.util;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class PaymentReferenceGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generatePaymentRef(String pnr) {
        return String.format("PAY-%s-%s",
                LocalDate.now().format(DATE_FORMAT),
                pnr.replace("AERO-", ""));
    }

    public String generateRefundRef(String pnr) {
        return String.format("REF-%s-%s-%s",
                LocalDate.now().format(DATE_FORMAT),
                pnr.replace("AERO-", ""),
                UUID.randomUUID().toString().substring(0, 4).toUpperCase());
    }
}