package com.aerobook.repository;


import com.aerobook.enitity.Ticket;
import com.aerobook.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    boolean existsByTicketNumber(String ticketNumber);

    List<Ticket> findAllByBookingId(Long bookingId);

    List<Ticket> findAllByPassengerId(Long passengerId);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.passenger p " +
            "JOIN FETCH t.booking b " +
            "JOIN FETCH t.flight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(Long id);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.passenger " +
            "JOIN FETCH t.flight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE t.booking.id = :bookingId")
    List<Ticket> findAllByBookingIdWithDetails(Long bookingId);

    List<Ticket> findAllByBookingIdAndStatus(Long bookingId, TicketStatus status);
}
