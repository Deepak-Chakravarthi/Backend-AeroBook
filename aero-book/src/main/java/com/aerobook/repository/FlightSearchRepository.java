package com.aerobook.repository;


import com.aerobook.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface FlightSearchRepository extends JpaRepository<Flight, Long> {

    // ── One-way: exact date ──────────────────────────────────────────
    @Query("SELECT DISTINCT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin o " +
            "JOIN FETCH r.destination d " +
            "LEFT JOIN FETCH f.fares fa " +
            "WHERE o.iataCode = :originCode " +
            "AND d.iataCode   = :destinationCode " +
            "AND f.departureDate = :departureDate " +
            "AND f.status NOT IN ('CANCELLED', 'DIVERTED') " +
            "ORDER BY f.departureTime ASC")
    List<Flight> searchOneWay(String originCode,
                              String destinationCode,
                              LocalDate departureDate);

    // ── One-way: date range ──────────────────────────────────────────
    @Query("SELECT DISTINCT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin o " +
            "JOIN FETCH r.destination d " +
            "LEFT JOIN FETCH f.fares fa " +
            "WHERE o.iataCode = :originCode " +
            "AND d.iataCode   = :destinationCode " +
            "AND f.departureDate BETWEEN :fromDate AND :toDate " +
            "AND f.status NOT IN ('CANCELLED', 'DIVERTED') " +
            "ORDER BY f.departureTime ASC")
    List<Flight> searchOneWayDateRange(String originCode,
                                       String destinationCode,
                                       LocalDate fromDate,
                                       LocalDate toDate);

    // ── Return: outbound ─────────────────────────────────────────────
    @Query("SELECT DISTINCT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin o " +
            "JOIN FETCH r.destination d " +
            "LEFT JOIN FETCH f.fares fa " +
            "WHERE o.iataCode = :originCode " +
            "AND d.iataCode   = :destinationCode " +
            "AND f.departureDate = :departureDate " +
            "AND f.status NOT IN ('CANCELLED', 'DIVERTED') " +
            "ORDER BY f.departureTime ASC")
    List<Flight> searchReturn(String originCode,
                              String destinationCode,
                              LocalDate departureDate);

    // ── Multi-city: single leg ───────────────────────────────────────
    @Query("SELECT DISTINCT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin o " +
            "JOIN FETCH r.destination d " +
            "LEFT JOIN FETCH f.fares fa " +
            "WHERE o.iataCode = :originCode " +
            "AND d.iataCode   = :destinationCode " +
            "AND f.departureDate = :departureDate " +
            "AND f.status NOT IN ('CANCELLED', 'DIVERTED') " +
            "ORDER BY f.departureTime ASC")
    List<Flight> searchLeg(String originCode,
                           String destinationCode,
                           LocalDate departureDate);
}
