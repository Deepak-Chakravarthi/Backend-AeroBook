package com.aerobook.repository;

import com.aerobook.enitity.Flight;
import com.aerobook.domain.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long>,
        JpaSpecificationExecutor<Flight> {

    boolean existsByFlightNumberAndDepartureDate(String flightNumber, LocalDate departureDate);

    @Query("SELECT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "LEFT JOIN FETCH f.fares " +
            "WHERE f.id = :id")
    Optional<Flight> findByIdWithDetails(Long id);

    @Query("SELECT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "LEFT JOIN FETCH f.fares " +
            "WHERE f.status = :status")
    List<Flight> findAllByStatusWithDetails(FlightStatus status);
}
