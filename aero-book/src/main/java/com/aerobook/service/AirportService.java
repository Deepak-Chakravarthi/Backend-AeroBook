package com.aerobook.service;


import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.request.get.AirportGetRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.enitity.Airport;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AirportMapper;
import com.aerobook.repository.AirportRepository;
import com.aerobook.service.query.AirportQueryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;
    private final AirportQueryService airportQueryService;

    public List<AirportResponse> getAirports(AirportGetRequest request, Pageable pageable) {
        return airportRepository.findAll(request.toSpecification(), pageable)
                .map(airportMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {"airport"}, key = "#id")
    public AirportResponse createAirport(AirportRequest request) {
        if (airportRepository.existsByIataCode(request.getIataCode().toUpperCase())) {
            throw new DuplicateResourceException("Airport", "IATA code", request.getIataCode());
        }
        Airport airport = airportMapper.toEntity(request);
        airport.setIataCode(request.getIataCode().toUpperCase());
        return airportMapper.toResponse(airportRepository.save(airport));
    }

    @Transactional
    @CacheEvict(value = {"airport"}, key = "#id")
    public AirportResponse updateAirport(Long id, AirportRequest request) {
        Airport airport = airportQueryService.findAirportById(id);
        airportMapper.updateEntity(request, airport);
        return airportMapper.toResponse(airportRepository.save(airport));
    }

    @Transactional
    public void deleteAirport(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Airport", id);
        }
        airportRepository.deleteById(id);
    }
}