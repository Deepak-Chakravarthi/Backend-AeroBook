package com.aerobook.util;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class BoardingPassNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate(String pnr, String seatNumber) {
        return String.format("BP-%s-%s-%s",
                LocalDate.now().format(DATE_FORMAT),
                pnr.replace("AERO-", ""),
                seatNumber.replace(" ", "")
        );
    }

    public String generateBarcode(String boardingPassNumber) {
        return boardingPassNumber + "-"
                + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
    }
}