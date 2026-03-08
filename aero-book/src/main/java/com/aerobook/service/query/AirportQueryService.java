package com.aerobook.service.query;

import com.aerobook.enitity.Airport;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.repository.AirportRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AirportQueryService {

    private final AirportRepository airportRepository;

    @Cacheable(value = "airport", key = "#id")
    public Airport findAirportById(Long id) {

        return airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", "id", id.toString()));
    }
}
