package com.aerobook.repository;

import com.aerobook.entity.FlightFare;
import com.aerobook.domain.enums.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The interface Flight fare repository.
 */
public interface FlightFareRepository extends JpaRepository<FlightFare, Long> {
    /**
     * Find all by flight id list.
     *
     * @param flightId the flight id
     * @return the list
     */
    List<FlightFare> findAllByFlightId(Long flightId);

    /**
     * Find by flight id and seat class optional.
     *
     * @param flightId  the flight id
     * @param seatClass the seat class
     * @return the optional
     */
    Optional<FlightFare> findByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);

    /**
     * Exists by flight id and seat class boolean.
     *
     * @param flightId  the flight id
     * @param seatClass the seat class
     * @return the boolean
     */
    boolean existsByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);
}
