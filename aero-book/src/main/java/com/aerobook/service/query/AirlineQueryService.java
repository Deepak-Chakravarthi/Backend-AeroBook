package com.aerobook.service.query;

import com.aerobook.enitity.Airline;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.repository.AirlineRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AirlineQueryService {

    private final AirlineRepository airlineRepository;

    @Cacheable(value = "airlineById", key = "#id")
    public Airline findAirlineById(Long id) {
        return airlineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Airline", "id", id.toString()));
    }
}
