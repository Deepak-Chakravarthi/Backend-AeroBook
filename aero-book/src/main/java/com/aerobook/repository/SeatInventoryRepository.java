package com.aerobook.repository;


import com.aerobook.enitity.SeatInventory;
import com.aerobook.domain.enums.SeatClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    List<SeatInventory> findAllByFlightId(Long flightId);

    Optional<SeatInventory> findByFlightIdAndSeatClass(Long flightId, SeatClass seatClass);

    // Pessimistic write lock for atomic inventory updates
    // Used alongside optimistic locking as double safety
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM SeatInventory si WHERE si.flight.id = :flightId AND si.seatClass = :seatClass")
    Optional<SeatInventory> findByFlightIdAndSeatClassWithLock(Long flightId, SeatClass seatClass);

    @Query("SELECT si FROM SeatInventory si WHERE si.flight.id = :flightId AND si.availableSeats > 0")
    List<SeatInventory> findAvailableByFlightId(Long flightId);
}
