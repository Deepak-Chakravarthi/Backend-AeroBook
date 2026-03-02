package com.aerobook.repository;


import com.aerobook.enitity.AircraftSeatConfig;
import com.aerobook.domain.enums.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AircraftSeatConfigRepository extends JpaRepository<AircraftSeatConfig, Long> {
    List<AircraftSeatConfig> findAllByAircraftId(Long aircraftId);
    Optional<AircraftSeatConfig> findByAircraftIdAndSeatClass(Long aircraftId, SeatClass seatClass);
    boolean existsByAircraftIdAndSeatClass(Long aircraftId, SeatClass seatClass);
}
