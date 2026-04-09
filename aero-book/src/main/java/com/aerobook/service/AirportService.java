package com.aerobook.service;


import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.request.get.AirportGetRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.entity.Airport;
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

/**
 * The type Airport service.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;
    private final AirportQueryService airportQueryService;

    /**
     * Gets airports.
     *
     * @param request  the request
     * @param pageable the pageable
     * @return the airports
     */
    public List<AirportResponse> getAirports(AirportGetRequest request, Pageable pageable) {
        return airportRepository.findAll(request.toSpecification(), pageable)
                .map(airportMapper::toResponse)
                .toList();
    }

    /**
     * Create airport entity airport.
     *
     * @param request the request
     * @return the airport
     */
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

    /**
     * Create airport airport response.
     *
     * @param airportRequest the airport request
     * @return the airport response
     */
    public AirportResponse createAirport(AirportRequest airportRequest) {
        Airport airline = createAirportEntity(airportRequest);
        return airportMapper.toResponse(airline);
    }

    /**
     * Update airport entity airport.
     *
     * @param id      the id
     * @param request the request
     * @return the airport
     */
    @Transactional
    @CachePut(value = "airportById", key = "#id")
    public Airport updateAirportEntity(Long id, AirportRequest request) {
        Airport airport = airportQueryService.findAirportById(id);
        airportMapper.updateEntity(request, airport);
        return airportRepository.save(airport);
    }

    /**
     * Update airport airport response.
     *
     * @param id             the id
     * @param airportRequest the airport request
     * @return the airport response
     */
    public AirportResponse updateAirport(Long id, AirportRequest airportRequest) {
        Airport airline = updateAirportEntity(id, airportRequest);
        return airportMapper.toResponse(airline);
    }


    /**
     * Delete airport.
     *
     * @param id the id
     */
    @Transactional
    public void deleteAirport(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Airport", id);
        }
        airportRepository.deleteById(id);
    }
}