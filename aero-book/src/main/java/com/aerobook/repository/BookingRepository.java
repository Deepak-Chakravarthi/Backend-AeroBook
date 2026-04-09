package com.aerobook.repository;


import com.aerobook.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByPnr(String pnr);

    boolean existsByPnr(String pnr);

    List<Booking> findAllByUserId(Long userId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.outboundFlight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(Long id);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.outboundFlight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE b.pnr = :pnr")
    Optional<Booking> findByPnrWithDetails(String pnr);

    // Expired bookings — for scheduler
    @Query("SELECT b FROM Booking b " +
            "WHERE b.status IN ('PENDING', 'SEAT_LOCKED') " +
            "AND b.seatHoldExpiresAt < :now")
    List<Booking> findExpiredBookings(LocalDateTime now);

    // User's active bookings
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.outboundFlight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE b.user.id = :userId " +
            "AND b.status NOT IN ('CANCELLED', 'EXPIRED') " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findActiveBookingsByUser(Long userId);
}
