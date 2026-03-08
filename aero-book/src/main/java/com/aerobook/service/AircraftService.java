package com.aerobook.service;

import com.aerobook.domain.dto.request.get.AircraftGetRequest;
import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.enitity.Aircraft;
import com.aerobook.enitity.Airline;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AircraftMapper;
import com.aerobook.repository.AircraftRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftMapper aircraftMapper;
    private final AircraftQueryService aircraftQueryService;
    private final AirlineQueryService airlineQueryService;

    public List<AircraftResponse> getAircraft(AircraftGetRequest request, Pageable pageable) {
        return aircraftRepository.findAll(request.toSpecification(),pageable)
                .map(aircraftMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "aircraftRegistration", key = "#request.registrationNumber")
    public AircraftResponse createAircraft(AircraftRequest request) {
        if (aircraftQueryService.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Aircraft", "registration number", request.getRegistrationNumber());
        }
        Airline airline = airlineQueryService.findAirlineById(request.getAirlineId());
        Aircraft aircraft = aircraftMapper.toEntity(request);
        aircraft.setAirline(airline);
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Transactional
    @CacheEvict(value = {"aircraft", "aircraftRegistration"}, key = "#id")
    public AircraftResponse updateAircraft(Long id, AircraftRequest request) {

        Aircraft aircraft = aircraftQueryService.findAircraftById(id);

        request.validateRegistrationUpdate(
                aircraft.getRegistrationNumber(),
                request.getRegistrationNumber()
        );
        Airline airline = airlineQueryService.findAirlineById(request.getAirlineId());
        aircraftMapper.updateEntity(request, aircraft);
        aircraft.setAirline(airline);
        return aircraftMapper.toResponse(aircraft);
    }

    @Transactional
    public void deleteAircraft(Long id) {
        if (!aircraftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aircraft", id);
        }
        aircraftRepository.deleteById(id);
    }
}