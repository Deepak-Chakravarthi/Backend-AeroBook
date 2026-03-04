package com.aerobook.service;

import com.aerobook.domain.dto.request.RouteGetRequest;
import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.domain.enums.RouteStatus;
import com.aerobook.enitity.Airport;
import com.aerobook.enitity.Route;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.RouteMapper;
import com.aerobook.repository.RouteRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.aerobook.domain.enums.RouteStatus.parseStatus;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final AirportService airportService;

    public Object getRoute(RouteGetRequest request) {

        if (request.getId() != null) {
            Route route = routeRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Route", request.getId()));
            return routeMapper.toResponse(route);
        }

        if (request.getOriginCode() != null) {
            return routeRepository.findAllByOriginIataCode(request.getOriginCode().toUpperCase())
                    .stream()
                    .map(routeMapper::toResponse)
                    .toList();
        }

        if (request.getDestinationCode() != null) {
            return routeRepository.findAllByDestinationIataCode(request.getDestinationCode().toUpperCase())
                    .stream()
                    .map(routeMapper::toResponse)
                    .toList();
        }

        if (request.getStatus() != null) {
            RouteStatus status = parseStatus(request.getStatus());
            return routeRepository.findAllByStatus(status)
                    .stream()
                    .map(routeMapper::toResponse)
                    .toList();
        }

        throw new AeroBookException(
                "No valid search parameter found",
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST"
        );
    }

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        if (request.originAirportId().equals(request.destinationAirportId())) {
            throw new AeroBookException("Origin and destination airports cannot be the same",
                    HttpStatus.BAD_REQUEST, "INVALID_ROUTE");
        }
        if (routeRepository.existsByOriginIdAndDestinationId(
                request.originAirportId(), request.destinationAirportId())) {
            throw new DuplicateResourceException("Route", "origin→destination",
                    request.originAirportId() + "→" + request.destinationAirportId());
        }
        Airport origin = airportService.findAirportById(request.originAirportId());
        Airport destination = airportService.findAirportById(request.destinationAirportId());
        Route route = routeMapper.toEntity(request);
        route.setOrigin(origin);
        route.setDestination(destination);
        return routeMapper.toResponse(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
        Airport origin = airportService.findAirportById(request.originAirportId());
        Airport destination = airportService.findAirportById(request.destinationAirportId());
        routeMapper.updateEntity(request, route);
        route.setOrigin(origin);
        route.setDestination(destination);
        return routeMapper.toResponse(routeRepository.save(route));
    }

    @Transactional
    public void deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", id);
        }
        routeRepository.deleteById(id);
    }
}
