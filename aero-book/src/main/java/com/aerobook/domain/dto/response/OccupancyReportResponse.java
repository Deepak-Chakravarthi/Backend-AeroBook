package com.aerobook.domain.dto.response;

import java.time.LocalDate;
import java.util.List;

public record OccupancyReportResponse(
        LocalDate from,
        LocalDate to,
        double    averageOccupancyRate,
        int       totalFlights,
        int       totalSeatsAvailable,
        int       totalSeatsBooked,
        List<FlightOccupancy>  flightOccupancies,
        List<RouteOccupancy>   routeOccupancies,
        List<ClassOccupancy>   classOccupancies
) {
    public record FlightOccupancy(
            String flightNumber,
            LocalDate departureDate,
            String originCode,
            String destinationCode,
            int    totalSeats,
            int    bookedSeats,
            int    availableSeats,
            double occupancyRate
    ) {}

    public record RouteOccupancy(
            String originCode,
            String destinationCode,
            int    totalFlights,
            double averageOccupancyRate
    ) {}

    public record ClassOccupancy(
            String seatClass,
            int    totalSeats,
            int    bookedSeats,
            double occupancyRate
    ) {}
}