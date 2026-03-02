package com.aerobook.service;

import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.enitity.Aircraft;
import com.aerobook.enitity.Airline;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AircraftMapper;
import com.aerobook.repository.AircraftRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftMapper aircraftMapper;
    private final AirlineService airlineService;

    public List<AircraftResponse> getAllAircraft() {
        return aircraftRepository.findAll().stream()
                .map(aircraftMapper::toResponse)
                .toList();
    }

    public List<AircraftResponse> getAircraftByAirline(Long airlineId) {
        return aircraftRepository.findAllByAirlineId(airlineId).stream()
                .map(aircraftMapper::toResponse)
                .toList();
    }

    public AircraftResponse getAircraftById(Long id) {
        return aircraftMapper.toResponse(findAircraftById(id));
    }

    @Transactional
    public AircraftResponse createAircraft(AircraftRequest request) {
        if (aircraftRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new DuplicateResourceException("Aircraft", "registration number", request.registrationNumber());
        }
        Airline airline = airlineService.findAirlineById(request.airlineId());
        Aircraft aircraft = aircraftMapper.toEntity(request);
        aircraft.setAirline(airline);
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Transactional
    public AircraftResponse updateAircraft(Long id, AircraftRequest request) {
        Aircraft aircraft = findAircraftById(id);
        if (!aircraft.getRegistrationNumber().equals(request.registrationNumber())
                && aircraftRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new DuplicateResourceException("Aircraft", "registration number", request.registrationNumber());
        }
        Airline airline = airlineService.findAirlineById(request.airlineId());
        aircraftMapper.updateEntity(request, aircraft);
        aircraft.setAirline(airline);
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Transactional
    public void deleteAircraft(Long id) {
        if (!aircraftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aircraft", id);
        }
        aircraftRepository.deleteById(id);
    }

    public Aircraft findAircraftById(Long id) {
        return aircraftRepository.findByIdWithSeatConfigs(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft", id));
    }
}