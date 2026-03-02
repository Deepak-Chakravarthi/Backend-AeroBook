package com.aerobook.repository;


import com.aerobook.domain.enums.RouteStatus;
import com.aerobook.enitity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByOriginIdAndDestinationId(Long originId, Long destinationId);

    boolean existsByOriginIdAndDestinationId(Long originId, Long destinationId);

    List<Route> findAllByStatus(RouteStatus status);

    @Query("SELECT r FROM Route r JOIN FETCH r.origin JOIN FETCH r.destination WHERE r.status = 'ACTIVE'")
    List<Route> findAllActiveRoutesWithAirports();

    @Query("SELECT r FROM Route r JOIN FETCH r.origin JOIN FETCH r.destination " +
            "WHERE r.origin.iataCode = :originCode AND r.destination.iataCode = :destinationCode")
    Optional<Route> findByOriginCodeAndDestinationCode(String originCode, String destinationCode);
}