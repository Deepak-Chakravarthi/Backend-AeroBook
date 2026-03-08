package com.aerobook.service;

import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.request.get.AirlineGetRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.enitity.Airline;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AirlineMapper;
import com.aerobook.repository.AirlineRepository;
import com.aerobook.service.query.AirlineQueryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirlineMapper airlineMapper;
    private final AirlineQueryService airlineQueryService;

    public List<AirlineResponse> getAirlines(AirlineGetRequest request, Pageable pageable) {
        return airlineRepository.findAll(request.toSpecification(), pageable)
                .map(airlineMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "airline", key = "#request.iataCode.toUpperCase()")
    public AirlineResponse createAirline(AirlineRequest request) {

        String iataCode = request.getIataCode().toUpperCase();

        if (airlineQueryService.existsByIataCode(iataCode)) {
            throw new DuplicateResourceException("Airline", "IATA code", iataCode);
        }

        Airline airline = airlineMapper.toEntity(request);
        airline.setIataCode(iataCode);
        return airlineMapper.toResponse(airlineRepository.save(airline));
    }

    @Transactional
    @CacheEvict(value = "airlines", key = "#id")
    public AirlineResponse updateAirline(Long id, AirlineRequest request) {
        Airline airline = airlineQueryService.findAirlineById(id);
        request.validateIataCodeUpdate(airline.getIataCode(), request.getIataCode());

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
}
