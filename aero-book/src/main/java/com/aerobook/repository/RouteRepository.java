package com.aerobook.repository;


import com.aerobook.domain.enums.RouteStatus;
import com.aerobook.enitity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    boolean existsByOriginIdAndDestinationId(Long originId, Long destinationId);

    List<Route> findAllByStatus(RouteStatus status);

    @Query("SELECT r FROM Route r JOIN FETCH r.origin JOIN FETCH r.destination " +
            "WHERE r.origin.iataCode = :iataCode")
    List<Route> findAllByOriginIataCode(String iataCode);

    @Query("SELECT r FROM Route r JOIN FETCH r.origin JOIN FETCH r.destination " +
            "WHERE r.destination.iataCode = :iataCode")
    List<Route> findAllByDestinationIataCode(String iataCode);
}