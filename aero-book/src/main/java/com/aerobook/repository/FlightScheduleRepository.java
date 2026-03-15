package com.aerobook.repository;


import com.aerobook.enitity.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long>,
        JpaSpecificationExecutor<FlightSchedule> {

    List<FlightSchedule> findAllByActiveTrue();

    @Query("SELECT s FROM FlightSchedule s " +
            "JOIN FETCH s.airline " +
            "JOIN FETCH s.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE s.active = true")
    List<FlightSchedule> findAllActiveWithDetails();
}
