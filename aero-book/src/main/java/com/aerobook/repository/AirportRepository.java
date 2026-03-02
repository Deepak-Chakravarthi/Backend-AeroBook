package com.aerobook.repository;


import com.aerobook.enitity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByIataCode(String iataCode);

    boolean existsByIataCode(String iataCode);

    List<Airport> findAllByCountry(String country);

    List<Airport> findAllByCityContainingIgnoreCase(String city);
}