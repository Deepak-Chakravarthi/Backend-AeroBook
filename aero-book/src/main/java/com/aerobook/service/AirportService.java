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
import org.springframework.cache.annotation.CachePut;
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
    @CachePut(value = "airport", key = "#request.id")
    public Airport createAirportEntity(AirportRequest request) {
        if (airportRepository.existsByIataCode(request.getIataCode().toUpperCase())) {
            throw new DuplicateResourceException("Airport", "IATA code", request.getIataCode());
        }
        Airport airport = airportMapper.toEntity(request);
        airport.setIataCode(request.getIataCode().toUpperCase());
        return airportRepository.save(airport);
    }

    public AirportResponse createAirport(AirportRequest airportRequest) {
        Airport airline = createAirportEntity(airportRequest);
        return airportMapper.toResponse(airline);
    }

    @Transactional
    @CachePut(value = "airportById", key = "#id")
    public Airport updateAirportEntity(Long id, AirportRequest request) {
        Airport airport = airportQueryService.findAirportById(id);
        airportMapper.updateEntity(request, airport);
        return airportRepository.save(airport);
    }

    public AirportResponse updateAirport(Long id, AirportRequest airportRequest) {
        Airport airline = updateAirportEntity(id, airportRequest);
        return airportMapper.toResponse(airline);
    }


    @Transactional
    public void deleteAirport(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Airport", id);
        }
        airportRepository.deleteById(id);
    }
}