package com.aerobook.service;

import com.aerobook.domain.dto.request.AirlineGetRequest;
import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.domain.enums.AirlineStatus;
import com.aerobook.enitity.Airline;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AirlineMapper;
import com.aerobook.repository.AirlineRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirlineMapper airlineMapper;

    public Object getAirline(AirlineGetRequest request) {

        if (request.getId() != null) {
            return airlineMapper.toResponse(findAirlineById(request.getId()));
        }

        if (request.getIataCode() != null) {
            Airline airline = airlineRepository.findByIataCode(request.getIataCode().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Airline", "iataCode", request.getIataCode()));
            return airlineMapper.toResponse(airline);
        }

        if (request.getStatus() != null) {
            AirlineStatus status = AirlineStatus.parseStatus(request.getStatus());
            return airlineRepository.findAllByStatus(status)
                    .stream()
                    .map(airlineMapper::toResponse)
                    .toList();
        }

        if (request.getCountry() != null) {
            return airlineRepository.findAllByCountryIgnoreCase(request.getCountry())
                    .stream()
                    .map(airlineMapper::toResponse)
                    .toList();
        }

        throw new AeroBookException(
                "No valid search parameter found",
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST"
        );
    }

    @Transactional
    public AirlineResponse createAirline(AirlineRequest request) {
        if (airlineRepository.existsByIataCode(request.iataCode().toUpperCase())) {
            throw new DuplicateResourceException("Airline", "IATA code", request.iataCode());
        }
        Airline airline = airlineMapper.toEntity(request);
        airline.setIataCode(request.iataCode().toUpperCase());
        return airlineMapper.toResponse(airlineRepository.save(airline));
    }

    @Transactional
    public AirlineResponse updateAirline(Long id, AirlineRequest request) {
        Airline airline = findAirlineById(id);
        // If IATA code is changing, check for conflicts
        if (!airline.getIataCode().equals(request.iataCode().toUpperCase())
                && airlineRepository.existsByIataCode(request.iataCode().toUpperCase())) {
            throw new DuplicateResourceException("Airline", "IATA code", request.iataCode());
        }
        airlineMapper.updateEntity(request, airline);
        return airlineMapper.toResponse(airlineRepository.save(airline));
    }

    @Transactional
    public void deleteAirline(Long id) {
        if (!airlineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Airline", id);
        }
        airlineRepository.deleteById(id);
    }

    // Internal helper — used by other services in this module
    public Airline findAirlineById(Long id) {
        return airlineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airline", id));
    }
}
