package com.aerobook.repository;


import com.aerobook.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * The interface Airport repository.
 */
public interface AirportRepository extends JpaRepository<Airport, Long>,
        JpaSpecificationExecutor<Airport> {

    /**
     * Exists by iata code boolean.
     *
     * @param iataCode the iata code
     * @return the boolean
     */
    boolean existsByIataCode(String iataCode);
}