package com.aerobook.service;


import com.aerobook.domain.dto.request.AirportGetRequest;
import com.aerobook.enitity.Airport;
import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AirportMapper;
import com.aerobook.repository.AirportRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;

    public Object getAirport(AirportGetRequest request) {

        if (request.getId() != null) {
            return airportMapper.toResponse(findAirportById(request.getId()));
        }

        if (request.getIataCode() != null) {
            Airport airport = airportRepository.findByIataCode(request.getIataCode().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Airport", "iataCode", request.getIataCode()));
            return airportMapper.toResponse(airport);
        }

        if (request.getCity() != null) {
            return airportRepository.findAllByCityContainingIgnoreCase(request.getCity())
                    .stream()
                    .map(airportMapper::toResponse)
                    .toList();
        }

        if (request.getCountry() != null) {
            return airportRepository.findAllByCountry(request.getCountry())
                    .stream()
                    .map(airportMapper::toResponse)
                    .toList();
        }

        throw new AeroBookException(
                "No valid search parameter found",
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST"
        );
    }

    @Transactional
    public AirportResponse createAirport(AirportRequest request) {
        if (airportRepository.existsByIataCode(request.iataCode().toUpperCase())) {
            throw new DuplicateResourceException("Airport", "IATA code", request.iataCode());
        }
        Airport airport = airportMapper.toEntity(request);
        airport.setIataCode(request.iataCode().toUpperCase());
        return airportMapper.toResponse(airportRepository.save(airport));
    }

    @Transactional
    public AirportResponse updateAirport(Long id, AirportRequest request) {
        Airport airport = findAirportById(id);
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

    public Airport findAirportById(Long id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", id));
    }
}