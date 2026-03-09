package com.aerobook.repository;

import com.aerobook.enitity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * The interface Route repository.
 */
public interface RouteRepository extends JpaRepository<Route, Long>,
        JpaSpecificationExecutor<Route> {

    /**
     * Exists by origin id and destination id boolean.
     *
     * @param originId      the origin id
     * @param destinationId the destination id
     * @return the boolean
     */
    boolean existsByOriginIdAndDestinationId(Long originId, Long destinationId);
}