package com.aerobook.util;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TicketNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * Generates: TKT-20260316-AB12CD-1
     * Format: TKT-{date}-{pnr}-{sequence}
     */
    public String generate(String pnr) {
        return String.format("TKT-%s-%s-%d",
                LocalDate.now().format(DATE_FORMAT),
                pnr.replace("AERO-", ""),
                sequence.getAndIncrement()
        );
    }
}
