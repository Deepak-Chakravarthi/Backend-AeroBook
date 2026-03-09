package com.aerobook.repository;


import com.aerobook.domain.enums.SeatClass;
import com.aerobook.enitity.AircraftSeatConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The interface Aircraft seat config repository.
 */
public interface AircraftSeatConfigRepository extends JpaRepository<AircraftSeatConfig, Long> {
    /**
     * Find all by aircraft id list.
     *
     * @param aircraftId the aircraft id
     * @return the list
     */
    List<AircraftSeatConfig> findAllByAircraftId(Long aircraftId);

    /**
     * Find by aircraft id and seat class optional.
     *
     * @param aircraftId the aircraft id
     * @param seatClass  the seat class
     * @return the optional
     */
    Optional<AircraftSeatConfig> findByAircraftIdAndSeatClass(Long aircraftId, SeatClass seatClass);

    /**
     * Exists by aircraft id and seat class boolean.
     *
     * @param aircraftId the aircraft id
     * @param seatClass  the seat class
     * @return the boolean
     */
    boolean existsByAircraftIdAndSeatClass(Long aircraftId, SeatClass seatClass);
}
