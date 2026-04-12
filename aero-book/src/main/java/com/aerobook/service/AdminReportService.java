package com.aerobook.service;


import com.aerobook.domain.dto.request.OccupancyReportRequest;
import com.aerobook.domain.dto.request.RevenueReportRequest;
import com.aerobook.domain.dto.response.FlightPerformanceResponse;
import com.aerobook.domain.dto.response.FlightPerformanceResponse.AirlinePerformance;
import com.aerobook.domain.dto.response.OccupancyReportResponse;
import com.aerobook.domain.dto.response.OccupancyReportResponse.*;
import com.aerobook.domain.dto.response.RevenueReportResponse;
import com.aerobook.domain.dto.response.RevenueReportResponse.*;
import com.aerobook.entity.Flight;
import com.aerobook.domain.enums.ReportPeriod;
import com.aerobook.repository.AdminReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final AdminReportRepository reportRepository;

    // ----------------------------------------------------------------
    // Revenue report
    // ----------------------------------------------------------------
    public RevenueReportResponse generateRevenueReport(
            RevenueReportRequest request) {
        request.validate();

        LocalDate[] range = resolveDateRange(
                request.period(), request.from(), request.to());
        LocalDate from = range[0];
        LocalDate to   = range[1];

        BigDecimal totalRevenue = reportRepository.sumTotalRevenue(from, to);
        BigDecimal totalBase    = reportRepository.sumTotalBaseFare(from, to);
        BigDecimal totalTax     = reportRepository.sumTotalTax(from, to);
        int        bookings     = reportRepository.countConfirmedBookings(from, to);
        BigDecimal maxBooking   = reportRepository.maxBookingValue(from, to);

        BigDecimal avgTicket = bookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(bookings),
                2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<RevenueByRoute> byRoute = reportRepository
                .revenueByRoute(from, to)
                .stream()
                .map(row -> new RevenueByRoute(
                        (String) row[0],
                        (String) row[1],
                        ((Long) row[2]).intValue(),
                        (BigDecimal) row[3]))
                .toList();

        List<RevenueByAirline> byAirline = reportRepository
                .revenueByAirline(from, to)
                .stream()
                .map(row -> new RevenueByAirline(
                        (String) row[0],
                        (String) row[1],
                        ((Long) row[2]).intValue(),
                        (BigDecimal) row[3]))
                .toList();

        List<RevenueByClass> byClass = reportRepository
                .revenueByClass(from, to)
                .stream()
                .map(row -> new RevenueByClass(
                        (String) row[0],
                        ((Long) row[1]).intValue(),
                        (BigDecimal) row[2]))
                .toList();

        List<RevenueTrend> trend = reportRepository
                .revenueTrend(from, to)
                .stream()
                .map(row -> new RevenueTrend(
                        (LocalDate) row[0],
                        ((Long) row[1]).intValue(),
                        (BigDecimal) row[2]))
                .toList();

        log.info("Revenue report generated — period: {} to {}, revenue: {}",
                from, to, totalRevenue);

        return new RevenueReportResponse(
                from, to,
                totalRevenue, totalBase, totalTax,
                bookings, bookings,
                avgTicket, maxBooking,
                byRoute, byAirline, byClass, trend
        );
    }

    // ----------------------------------------------------------------
    // Occupancy report
    // ----------------------------------------------------------------
    public OccupancyReportResponse generateOccupancyReport(
            OccupancyReportRequest request) {

        LocalDate[] range = resolveDateRange(
                request.period(), request.from(), request.to());
        LocalDate from = range[0];
        LocalDate to   = range[1];

        List<Object[]> flightRows = reportRepository.flightOccupancy(from, to);

        List<FlightOccupancy> flightOccupancies = flightRows.stream()
                .map(row -> {
                    Flight flight    = (Flight) row[0];
                    long totalSeats  = row[1] != null ? (Long) row[1] : 0L;
                    long bookedSeats = row[2] != null ? (Long) row[2] : 0L;
                    double rate = totalSeats > 0
                            ? (double) bookedSeats / totalSeats * 100 : 0;

                    return new FlightOccupancy(
                            flight.getFlightNumber(),
                            flight.getDepartureDate(),
                            flight.getRoute().getOrigin().getIataCode(),
                            flight.getRoute().getDestination().getIataCode(),
                            (int) totalSeats,
                            (int) bookedSeats,
                            (int) (totalSeats - bookedSeats),
                            Math.round(rate * 100.0) / 100.0
                    );
                })
                .toList();

        // Route occupancy — aggregate from flight data
        List<RouteOccupancy> routeOccupancies = flightOccupancies.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        f -> f.originCode() + "-" + f.destinationCode()))
                .entrySet().stream()
                .map(entry -> {
                    String[] codes = entry.getKey().split("-");
                    List<FlightOccupancy> flights = entry.getValue();
                    double avgRate = flights.stream()
                            .mapToDouble(FlightOccupancy::occupancyRate)
                            .average().orElse(0.0);
                    return new RouteOccupancy(
                            codes[0], codes[1],
                            flights.size(),
                            Math.round(avgRate * 100.0) / 100.0
                    );
                })
                .toList();

        // Class occupancy
        List<ClassOccupancy> classOccupancies = reportRepository
                .occupancyByClass(from, to)
                .stream()
                .map(row -> {
                    long total  = row[1] != null ? (Long) row[1] : 0L;
                    long booked = row[2] != null ? (Long) row[2] : 0L;
                    double rate = total > 0
                            ? (double) booked / total * 100 : 0;
                    return new ClassOccupancy(
                            (String) row[0],
                            (int) total,
                            (int) booked,
                            Math.round(rate * 100.0) / 100.0
                    );
                })
                .toList();

        int totalSeats  = flightOccupancies.stream()
                .mapToInt(FlightOccupancy::totalSeats).sum();
        int totalBooked = flightOccupancies.stream()
                .mapToInt(FlightOccupancy::bookedSeats).sum();
        double avgOccupancy = totalSeats > 0
                ? (double) totalBooked / totalSeats * 100 : 0;

        log.info("Occupancy report generated — period: {} to {}, avg: {}%",
                from, to, Math.round(avgOccupancy * 100.0) / 100.0);

        return new OccupancyReportResponse(
                from, to,
                Math.round(avgOccupancy * 100.0) / 100.0,
                flightOccupancies.size(),
                totalSeats, totalBooked,
                flightOccupancies,
                routeOccupancies,
                classOccupancies
        );
    }

    // ----------------------------------------------------------------
    // Flight performance report
    // ----------------------------------------------------------------
    public FlightPerformanceResponse generatePerformanceReport(
            LocalDate from, LocalDate to) {

        int total      = reportRepository.countTotalFlights(from, to);
        int onTime     = reportRepository.countOnTimeFlights(from, to);
        int delayed    = reportRepository.countDelayedFlights(from, to);
        int cancelled  = reportRepository.countCancelledFlights(from, to);
        double avgDelay = reportRepository.avgDelayMinutes(from, to);

        double onTimePct = total > 0
                ? (double) onTime / total * 100 : 0;
        double cancelRate = total > 0
                ? (double) cancelled / total * 100 : 0;

        List<AirlinePerformance> airlinePerformance = reportRepository
                .airlinePerformance(from, to)
                .stream()
                .map(row -> {
                    long totalF   = (Long) row[2];
                    long onTimeF  = (Long) row[3];
                    double onPct  = totalF > 0
                            ? (double) onTimeF / totalF * 100 : 0;
                    return new AirlinePerformance(
                            (String) row[0],
                            (String) row[1],
                            (int) totalF,
                            (int) onTimeF,
                            ((Long) row[4]).intValue(),
                            ((Long) row[5]).intValue(),
                            Math.round(onPct * 100.0) / 100.0,
                            ((Double) row[6])
                    );
                })
                .toList();

        log.info("Performance report — period: {} to {}, on-time: {}%",
                from, to, Math.round(onTimePct * 100.0) / 100.0);

        return new FlightPerformanceResponse(
                from, to,
                total, onTime, delayed, cancelled,
                Math.round(onTimePct * 100.0) / 100.0,
                Math.round(cancelRate * 100.0) / 100.0,
                Math.round(avgDelay * 100.0) / 100.0,
                airlinePerformance
        );
    }

    // ----------------------------------------------------------------
    // Private — resolve date range from period
    // ----------------------------------------------------------------
    private LocalDate[] resolveDateRange(ReportPeriod period,
                                         LocalDate from, LocalDate to) {
        return switch (period) {
            case DAILY   -> new LocalDate[]{LocalDate.now(), LocalDate.now()};
            case WEEKLY  -> new LocalDate[]{
                    LocalDate.now().minusWeeks(1), LocalDate.now()};
            case MONTHLY -> new LocalDate[]{
                    LocalDate.now().withDayOfMonth(1), LocalDate.now()};
            case CUSTOM  -> new LocalDate[]{from, to};
        };
    }
}