package com.aerobook.repository;


import com.aerobook.enitity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * The interface Airline repository.
 */
public interface AirlineRepository extends JpaRepository<Airline, Long>,
        JpaSpecificationExecutor<Airline> {

    /**
     * Exists by iata code boolean.
     *
     * @param iataCode the iata code
     * @return the boolean
     */
    boolean existsByIataCode(String iataCode);
}