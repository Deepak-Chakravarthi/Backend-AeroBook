package com.aerobook.repository;


import com.aerobook.enitity.Seat;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long>,
        JpaSpecificationExecutor<Seat> {

    List<Seat> findAllByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);

    List<Seat> findAllByFlightIdAndSeatClassAndStatus(
            Long flightId, SeatClass seatClass, SeatStatus status);

    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    // Release all expired holds — called by scheduler
    @Modifying
    @Query("UPDATE Seat s SET s.status = 'AVAILABLE', " +
            "s.heldByBookingRef = null, s.heldUntil = null " +
            "WHERE s.status = 'HELD' AND s.heldUntil < :now")
    int releaseExpiredHolds(LocalDateTime now);

    @Query("SELECT s FROM Seat s WHERE s.heldByBookingRef = :bookingRef")
    List<Seat> findAllByBookingRef(String bookingRef);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flight.id = :flightId " +
            "AND s.seatClass = :seatClass AND s.status = 'AVAILABLE'")
    int countAvailableByFlightAndClass(Long flightId, SeatClass seatClass);
}
