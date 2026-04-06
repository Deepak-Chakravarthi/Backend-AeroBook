package com.aerobook.repository;

import com.aerobook.enitity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long>,
        JpaSpecificationExecutor<Passenger> {

    List<Passenger> findAllByBookingId(Long bookingId);

    @Query("SELECT p FROM Passenger p " +
            "JOIN FETCH p.booking b " +
            "LEFT JOIN FETCH p.tickets t " +
            "LEFT JOIN FETCH t.flight " +
            "WHERE p.id = :id")
    Optional<Passenger> findByIdWithTickets(Long id);

    @Query("SELECT p FROM Passenger p " +
            "JOIN FETCH p.booking b " +
            "LEFT JOIN FETCH p.tickets " +
            "WHERE b.id = :bookingId")
    List<Passenger> findAllByBookingIdWithTickets(Long bookingId);
}
