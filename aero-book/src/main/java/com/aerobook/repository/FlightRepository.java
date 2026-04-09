package com.aerobook.repository;

import com.aerobook.entity.Flight;
import com.aerobook.domain.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The interface Flight repository.
 */
public interface FlightRepository extends JpaRepository<Flight, Long>,
        JpaSpecificationExecutor<Flight> {

    /**
     * Exists by flight number and departure date boolean.
     *
     * @param flightNumber  the flight number
     * @param departureDate the departure date
     * @return the boolean
     */
    boolean existsByFlightNumberAndDepartureDate(String flightNumber, LocalDate departureDate);

    /**
     * Find by id with details optional.
     *
     * @param id the id
     * @return the optional
     */
    @Query("SELECT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "LEFT JOIN FETCH f.fares " +
            "WHERE f.id = :id")
    Optional<Flight> findByIdWithDetails(Long id);

    /**
     * Find all by status with details list.
     *
     * @param status the status
     * @return the list
     */
    @Query("SELECT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "LEFT JOIN FETCH f.fares " +
            "WHERE f.status = :status")
    List<Flight> findAllByStatusWithDetails(FlightStatus status);
}
