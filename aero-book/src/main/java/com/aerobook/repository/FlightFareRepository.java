package com.aerobook.repository;

import com.aerobook.enitity.FlightFare;
import com.aerobook.domain.enums.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightFareRepository extends JpaRepository<FlightFare, Long> {
    List<FlightFare> findAllByFlightId(Long flightId);
    Optional<FlightFare> findByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);
    boolean existsByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);
}
