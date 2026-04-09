package com.aerobook.service;

import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.request.get.AirlineGetRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.entity.Airline;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AirlineMapper;
import com.aerobook.repository.AirlineRepository;
import com.aerobook.service.query.AirlineQueryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The type Airline service.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirlineMapper airlineMapper;
    private final AirlineQueryService airlineQueryService;

    /**
     * Gets airlines.
     *
     * @param request  the request
     * @param pageable the pageable
     * @return the airlines
     */
    public List<AirlineResponse> getAirlines(AirlineGetRequest request, Pageable pageable) {
        return airlineRepository.findAll(request.toSpecification(), pageable)
                .map(airlineMapper::toResponse)
                .toList();
    }

    /**
     * Create airline entity airline.
     *
     * @param request the request
     * @return the airline
     */
    @Transactional
    @CachePut(value = "airline", key = "#request.id")
    public Airline createAirlineEntity(AirlineRequest request) {

        String iataCode = request.getIataCode().toUpperCase();

        if (airlineRepository.existsByIataCode(iataCode)) {
            throw new DuplicateResourceException("Airline", "IATA code", iataCode);
        }

        Airline airline = airlineMapper.toEntity(request);
        airline.setIataCode(iataCode);
        return airlineRepository.save(airline);
    }

    /**
     * Update airline enitity airline.
     *
     * @param id      the id
     * @param request the request
     * @return the airline
     */
    @Transactional
    @CachePut(value = "airlineById", key = "#id")
    public Airline updateAirlineEnitity(Long id, AirlineRequest request) {
        Airline airline = airlineQueryService.findAirlineById(id);
        validateIataCodeUpdate(
                airline.getIataCode(),
                request.getIataCode()
        );

        airlineMapper.updateEntity(request, airline);
        return airlineRepository.save(airline);
    }

    /**
     * Create airline airline response.
     *
     * @param request the request
     * @return the airline response
     */
    public AirlineResponse createAirline(AirlineRequest request) {
        Airline airline = createAirlineEntity(request);
        return airlineMapper.toResponse(airline);
    }

    /**
     * Update airline airline response.
     *
     * @param id      the id
     * @param request the request
     * @return the airline response
     */
    public AirlineResponse updateAirline(Long id, AirlineRequest request) {
        Airline airline = updateAirlineEnitity(id, request);
        return airlineMapper.toResponse(airline);
    }

    /**
     * Delete airline.
     *
     * @param id the id
     */
    @Transactional
    public void deleteAirline(Long id) {
        if (!airlineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Airline", id);
        }
        airlineRepository.deleteById(id);
    }

    /**
     * Validate iata code update.
     *
     * @param existing    the existing
     * @param newIataCode the new iata code
     */
    public void validateIataCodeUpdate(String existing, String newIataCode) {

        if (!existing.equalsIgnoreCase(newIataCode)
                && airlineRepository.existsByIataCode(newIataCode)) {

            throw new DuplicateResourceException("Aircraft", "IATA Code", newIataCode);
        }
    }
}
