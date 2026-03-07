package com.aerobook.repository;


import com.aerobook.enitity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AirlineRepository extends JpaRepository<Airline, Long>,
        JpaSpecificationExecutor<Airline> {

    boolean existsByIataCode(String iataCode);
}