package com.aerobook.service;

import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.request.get.RouteGetRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.enitity.Airport;
import com.aerobook.enitity.Route;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.RouteMapper;
import com.aerobook.repository.RouteRepository;
import com.aerobook.service.query.AirportQueryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The type Route service.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final AirportService airportService;
    private final AirportQueryService airportQueryService;

    /**
     * Gets routes.
     *
     * @param request  the request
     * @param pageable the pageable
     * @return the routes
     */
    public List<RouteResponse> getRoutes(RouteGetRequest request, Pageable pageable) {
        return routeRepository.findAll(request.toSpecification(), pageable)
                .map(routeMapper::toResponse)
                .toList();
    }

    /**
     * Create route route response.
     *
     * @param request the request
     * @return the route response
     */
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
        Airport origin = airportQueryService.findAirportById(request.originAirportId());
        Airport destination = airportQueryService.findAirportById(request.destinationAirportId());
        Route route = routeMapper.toEntity(request);
        route.setOrigin(origin);
        route.setDestination(destination);
        return routeMapper.toResponse(routeRepository.save(route));
    }

    /**
     * Update route route response.
     *
     * @param id      the id
     * @param request the request
     * @return the route response
     */
    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
        Airport origin = airportQueryService.findAirportById(request.originAirportId());
        Airport destination = airportQueryService.findAirportById(request.destinationAirportId());
        routeMapper.updateEntity(request, route);
        route.setOrigin(origin);
        route.setDestination(destination);
        return routeMapper.toResponse(routeRepository.save(route));
    }

    /**
     * Delete route.
     *
     * @param id the id
     */
    @Transactional
    public void deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", id);
        }
        routeRepository.deleteById(id);
    }
}
