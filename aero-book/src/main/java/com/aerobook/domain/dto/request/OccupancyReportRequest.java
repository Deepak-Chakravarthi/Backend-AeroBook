package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.ReportPeriod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OccupancyReportRequest(

        @NotNull
        ReportPeriod period,

        LocalDate from,
        LocalDate to,
        Long airlineId,
        Long routeId
) {}