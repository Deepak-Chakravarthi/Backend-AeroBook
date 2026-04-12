package com.aerobook.repository;


import com.aerobook.entity.Booking;
import com.aerobook.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AdminReportRepository extends JpaRepository<Booking, Long> {

    // ── Revenue queries ───────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(b.totalFare), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to")
    BigDecimal sumTotalRevenue(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(b.baseFare), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to")
    BigDecimal sumTotalBaseFare(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(b.tax), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to")
    BigDecimal sumTotalTax(LocalDate from, LocalDate to);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to")
    int countConfirmedBookings(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(MAX(b.totalFare), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to")
    BigDecimal maxBookingValue(LocalDate from, LocalDate to);

    // Revenue by route
    @Query("SELECT " +
            "r.origin.iataCode, r.destination.iataCode, " +
            "COUNT(b), SUM(b.totalFare) " +
            "FROM Booking b " +
            "JOIN b.outboundFlight f " +
            "JOIN f.route r " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to " +
            "GROUP BY r.origin.iataCode, r.destination.iataCode " +
            "ORDER BY SUM(b.totalFare) DESC")
    List<Object[]> revenueByRoute(LocalDate from, LocalDate to);

    // Revenue by airline
    @Query("SELECT " +
            "a.name, a.iataCode, COUNT(b), SUM(b.totalFare) " +
            "FROM Booking b " +
            "JOIN b.outboundFlight f " +
            "JOIN f.airline a " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to " +
            "GROUP BY a.name, a.iataCode " +
            "ORDER BY SUM(b.totalFare) DESC")
    List<Object[]> revenueByAirline(LocalDate from, LocalDate to);

    // Revenue by seat class
    @Query("SELECT " +
            "CAST(b.outboundSeatClass AS string), COUNT(b), SUM(b.totalFare) " +
            "FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to " +
            "GROUP BY b.outboundSeatClass")
    List<Object[]> revenueByClass(LocalDate from, LocalDate to);

    // Daily revenue trend
    @Query("SELECT " +
            "CAST(b.createdAt AS LocalDate), COUNT(b), SUM(b.totalFare) " +
            "FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND CAST(b.createdAt AS LocalDate) BETWEEN :from AND :to " +
            "GROUP BY CAST(b.createdAt AS LocalDate) " +
            "ORDER BY CAST(b.createdAt AS LocalDate) ASC")
    List<Object[]> revenueTrend(LocalDate from, LocalDate to);

    // ── Occupancy queries ─────────────────────────────────────────────

    @Query("SELECT f, " +
            "SUM(CASE WHEN si.seatClass = 'ECONOMY'  THEN si.totalSeats ELSE 0 END) + " +
            "SUM(CASE WHEN si.seatClass = 'BUSINESS' THEN si.totalSeats ELSE 0 END) + " +
            "SUM(CASE WHEN si.seatClass = 'FIRST'    THEN si.totalSeats ELSE 0 END), " +
            "SUM(CASE WHEN si.seatClass = 'ECONOMY'  THEN si.bookedSeats ELSE 0 END) + " +
            "SUM(CASE WHEN si.seatClass = 'BUSINESS' THEN si.bookedSeats ELSE 0 END) + " +
            "SUM(CASE WHEN si.seatClass = 'FIRST'    THEN si.bookedSeats ELSE 0 END) " +
            "FROM Flight f " +
            "JOIN SeatInventory si ON si.flight.id = f.id " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE f.departureDate BETWEEN :from AND :to " +
            "AND f.status != 'CANCELLED' " +
            "GROUP BY f")
    List<Object[]> flightOccupancy(LocalDate from, LocalDate to);

    // Occupancy by class
    @Query("SELECT " +
            "CAST(si.seatClass AS string), " +
            "SUM(si.totalSeats), SUM(si.bookedSeats) " +
            "FROM SeatInventory si " +
            "JOIN si.flight f " +
            "WHERE f.departureDate BETWEEN :from AND :to " +
            "AND f.status != 'CANCELLED' " +
            "GROUP BY si.seatClass")
    List<Object[]> occupancyByClass(LocalDate from, LocalDate to);

    // ── Flight performance queries ─────────────────────────────────────

    @Query("SELECT COUNT(f) FROM Flight f " +
            "WHERE f.departureDate BETWEEN :from AND :to")
    int countTotalFlights(LocalDate from, LocalDate to);

    @Query("SELECT COUNT(f) FROM Flight f " +
            "WHERE f.status = 'LANDED' " +
            "AND (f.delayMinutes IS NULL OR f.delayMinutes = 0) " +
            "AND f.departureDate BETWEEN :from AND :to")
    int countOnTimeFlights(LocalDate from, LocalDate to);

    @Query("SELECT COUNT(f) FROM Flight f " +
            "WHERE f.status = 'DELAYED' " +
            "AND f.departureDate BETWEEN :from AND :to")
    int countDelayedFlights(LocalDate from, LocalDate to);

    @Query("SELECT COUNT(f) FROM Flight f " +
            "WHERE f.status = 'CANCELLED' " +
            "AND f.departureDate BETWEEN :from AND :to")
    int countCancelledFlights(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(AVG(f.delayMinutes), 0) FROM Flight f " +
            "WHERE f.status = 'DELAYED' " +
            "AND f.departureDate BETWEEN :from AND :to")
    double avgDelayMinutes(LocalDate from, LocalDate to);

    // Airline performance
    @Query("SELECT " +
            "a.name, a.iataCode, " +
            "COUNT(f), " +
            "SUM(CASE WHEN f.status = 'LANDED' AND " +
            "(f.delayMinutes IS NULL OR f.delayMinutes = 0) THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN f.status = 'DELAYED'   THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN f.status = 'CANCELLED' THEN 1 ELSE 0 END), " +
            "COALESCE(AVG(CASE WHEN f.status = 'DELAYED' " +
            "THEN f.delayMinutes ELSE NULL END), 0) " +
            "FROM Flight f " +
            "JOIN f.airline a " +
            "WHERE f.departureDate BETWEEN :from AND :to " +
            "GROUP BY a.name, a.iataCode " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> airlinePerformance(LocalDate from, LocalDate to);
}