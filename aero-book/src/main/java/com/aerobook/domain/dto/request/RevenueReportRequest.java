package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.ReportPeriod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RevenueReportRequest(

        @NotNull(message = "Period is required")
        ReportPeriod period,

        LocalDate from,
        LocalDate to,

        Long airlineId,         // optional — filter by airline
        Long routeId            // optional — filter by route
) {
    public void validate() {
        if (period == com.aerobook.domain.enums.ReportPeriod.CUSTOM) {
            if (from == null || to == null) {
                throw new IllegalArgumentException(
                        "from and to dates are required for CUSTOM period");
            }
            if (from.isAfter(to)) {
                throw new IllegalArgumentException(
                        "from date must be before to date");
            }
        }
    }
}