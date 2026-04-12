package com.aerobook.domain.dto.response;


import java.time.LocalDate;
import java.util.List;

public record FlightPerformanceResponse(
        LocalDate from,
        LocalDate to,
        int       totalFlights,
        int       onTimeFlights,
        int       delayedFlights,
        int       cancelledFlights,
        double    onTimePercentage,
        double    cancellationRate,
        double    averageDelayMinutes,
        List<AirlinePerformance> airlinePerformance
) {
    public record AirlinePerformance(
            String airlineName,
            String iataCode,
            int    totalFlights,
            int    onTime,
            int    delayed,
            int    cancelled,
            double onTimePercentage,
            double averageDelayMinutes
    ) {}
}