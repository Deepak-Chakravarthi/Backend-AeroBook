package com.aerobook.repository;


import com.aerobook.domain.enums.AirlineStatus;
import com.aerobook.enitity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Optional<Airline> findByIataCode(String iataCode);

    boolean existsByIataCode(String iataCode);

    List<Airline> findAllByStatus(AirlineStatus status);

    List<Airline> findAllByCountryIgnoreCase(String country);
}