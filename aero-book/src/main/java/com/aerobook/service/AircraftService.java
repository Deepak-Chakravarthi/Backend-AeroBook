package com.aerobook.service;

import com.aerobook.domain.dto.request.AircraftGetRequest;
import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.domain.enums.AircraftStatus;
import com.aerobook.enitity.Aircraft;
import com.aerobook.enitity.Airline;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AircraftMapper;
import com.aerobook.repository.AircraftRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftMapper aircraftMapper;
    private final AirlineService airlineService;

    public Object getAircraft(AircraftGetRequest request) {

        if (request.getId() != null) {
            return aircraftMapper.toResponse(findAircraftById(request.getId()));
        }

        if (request.getRegistrationNumber() != null) {
            Aircraft aircraft = aircraftRepository
                    .findByRegistrationNumber(request.getRegistrationNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aircraft", "registrationNumber", request.getRegistrationNumber()));
            return aircraftMapper.toResponse(aircraft);
        }

        if (request.getAirlineId() != null) {
            return aircraftRepository.findAllByAirlineId(request.getAirlineId())
                    .stream()
                    .map(aircraftMapper::toResponse)
                    .toList();
        }

        if (request.getStatus() != null) {
            AircraftStatus status = AircraftStatus.parseStatus(request.getStatus());
            return aircraftRepository.findAllByStatus(status)
                    .stream()
                    .map(aircraftMapper::toResponse)
                    .toList();
        }

        throw new AeroBookException(
                "No valid search parameter found",
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST"
        );
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