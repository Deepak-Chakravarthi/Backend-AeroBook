package com.aerobook.repository;


import com.aerobook.enitity.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * The interface Flight schedule repository.
 */
public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long>,
        JpaSpecificationExecutor<FlightSchedule> {

    /**
     * Find all by active true list.
     *
     * @return the list
     */
    List<FlightSchedule> findAllByActiveTrue();

    /**
     * Find all active with details list.
     *
     * @return the list
     */
    @Query("SELECT s FROM FlightSchedule s " +
            "JOIN FETCH s.airline " +
            "JOIN FETCH s.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE s.active = true")
    List<FlightSchedule> findAllActiveWithDetails();
}
