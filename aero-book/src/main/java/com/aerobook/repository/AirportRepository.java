package com.aerobook.repository;


import com.aerobook.enitity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AirportRepository extends JpaRepository<Airport, Long>,
        JpaSpecificationExecutor<Airport> {

    boolean existsByIataCode(String iataCode);
}