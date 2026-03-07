package com.aerobook.repository;

import com.aerobook.enitity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RouteRepository extends JpaRepository<Route, Long>,
        JpaSpecificationExecutor<Route> {

    boolean existsByOriginIdAndDestinationId(Long originId, Long destinationId);
}