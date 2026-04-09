package com.aerobook.service;

import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.request.get.AircraftGetRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.entity.Aircraft;
import com.aerobook.entity.Airline;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AircraftMapper;
import com.aerobook.repository.AircraftRepository;
import com.aerobook.service.query.AircraftQueryService;
import com.aerobook.service.query.AirlineQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The type Aircraft service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftMapper aircraftMapper;
    private final AircraftQueryService aircraftQueryService;
    private final AirlineQueryService airlineQueryService;

    /**
     * Gets aircraft.
     *
     * @param request  the request
     * @param pageable the pageable
     * @return the aircraft
     */
    public List<AircraftResponse> getAircraft(AircraftGetRequest request, Pageable pageable) {
        return aircraftRepository.findAll(request.toSpecification(), pageable)
                .map(aircraftMapper::toResponse)
                .toList();
    }

    /**
     * Method to createAircraftEntity
     *
     * @param request the request
     * @return aircraft
     */
    @Transactional
    @CachePut(value = "aircraft", key = "#result.id")
    public Aircraft createAircraftEntity(AircraftRequest request) {
        if (aircraftRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException(
                    "Aircraft", "registration number", request.getRegistrationNumber());
        }
        Airline airline = airlineQueryService.findAirlineById(request.getAirlineId());
        Aircraft aircraft = aircraftMapper.toEntity(request);
        aircraft.setAirline(airline);
        return aircraftRepository.save(aircraft);
    }

    /**
     * Create aircraft aircraft response.
     *
     * @param request the request
     * @return the aircraft response
     */
    public AircraftResponse createAircraft(AircraftRequest request) {
        Aircraft aircraft = createAircraftEntity(request);
        return aircraftMapper.toResponse(aircraft);
    }

    /**
     * Update aircraft entity aircraft.
     *
     * @param id      the id
     * @param request the request
     * @return the aircraft
     */
    @Transactional
    @CachePut(value = "aircraft", key = "#id")
    public Aircraft updateAircraftEntity(Long id, AircraftRequest request) {

        Aircraft aircraft = aircraftQueryService.findAircraftById(id);

        validateRegistrationUpdate(
                aircraft.getRegistrationNumber(),
                request.getRegistrationNumber()
        );
        Airline airline = airlineQueryService.findAirlineById(request.getAirlineId());
        aircraftMapper.updateEntity(request, aircraft);
        aircraft.setAirline(airline);
        return aircraft;
    }

    /**
     * Update aircraft aircraft response.
     *
     * @param id      the id
     * @param request the request
     * @return the aircraft response
     */
    public AircraftResponse updateAircraft(Long id, AircraftRequest request) {
        Aircraft aircraft = updateAircraftEntity(id, request);
        return aircraftMapper.toResponse(aircraft);
    }

    /**
     * Delete aircraft.
     *
     * @param id the id
     */
    @Transactional
    public void deleteAircraft(Long id) {
        if (!aircraftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aircraft", id);
        }
        aircraftRepository.deleteById(id);
    }

    /**
     * Validate registration update.
     *
     * @param existing        the existing
     * @param newRegistration the new registration
     */
    public void validateRegistrationUpdate(String existing, String newRegistration) {

        if (!existing.equals(newRegistration)
                && aircraftRepository.existsByRegistrationNumber(newRegistration)) {
            throw new DuplicateResourceException("Aircraft", "registration number", newRegistration);
        }
    }
}