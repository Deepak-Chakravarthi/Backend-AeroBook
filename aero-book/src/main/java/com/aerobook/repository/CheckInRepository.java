package com.aerobook.repository;


import com.aerobook.enitity.CheckIn;
import com.aerobook.domain.enums.CheckInStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long>,
        JpaSpecificationExecutor<CheckIn> {

    Optional<CheckIn> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    List<CheckIn> findAllByFlightId(Long flightId);

    List<CheckIn> findAllByBookingId(Long bookingId);

    @Query("SELECT c FROM CheckIn c " +
            "JOIN FETCH c.ticket t " +
            "JOIN FETCH c.passenger p " +
            "JOIN FETCH c.flight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "LEFT JOIN FETCH c.boardingPass " +
            "WHERE c.id = :id")
    Optional<CheckIn> findByIdWithDetails(Long id);

    @Query("SELECT c FROM CheckIn c " +
            "WHERE c.flight.id = :flightId " +
            "AND c.status = :status")
    List<CheckIn> findAllByFlightIdAndStatus(Long flightId,
                                             CheckInStatus status);
}