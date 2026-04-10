package com.aerobook.service;


import com.aerobook.domain.dto.request.FlightRequest;
import com.aerobook.domain.dto.request.FlightStatusUpdateRequest;
import com.aerobook.domain.dto.request.get.FlightGetRequest;
import com.aerobook.domain.dto.response.FlightResponse;
import com.aerobook.domain.enums.BookingStatus;
import com.aerobook.domain.enums.FlightStatus;
import com.aerobook.entity.Aircraft;
import com.aerobook.entity.Airline;
import com.aerobook.entity.Flight;
import com.aerobook.entity.Route;
import com.aerobook.event.FlightCompletedEvent;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.FlightMapper;
import com.aerobook.repository.BookingRepository;
import com.aerobook.repository.FlightRepository;
import com.aerobook.service.query.AircraftQueryService;
import com.aerobook.service.query.AirlineQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * The type Flight service.
 */
@Service
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final AirlineQueryService airlineQueryService;
    private final AircraftQueryService aircraftQueryService;
    private final RouteService routeService;
    private final ApplicationEventPublisher eventPublisher;
    @Lazy
    private final SeatInventoryService seatInventoryService;
    @Lazy
    private final SeatService seatService;
    @Lazy
    private final FlightSearchCacheService flightSearchCacheService;

    private final BookingRepository bookingRepository;

    private final LoyaltyService loyaltyService;

    public FlightService(
            FlightRepository flightRepository,
            FlightMapper flightMapper,
            AirlineQueryService airlineQueryService,
            AircraftQueryService aircraftQueryService,
            RouteService routeService,
            @Lazy SeatInventoryService seatInventoryService,
            @Lazy SeatService seatService,
            @Lazy FlightSearchCacheService flightSearchCacheService,
            ApplicationEventPublisher applicationEventPublisher, ApplicationEventPublisher eventPublisher, BookingRepository bookingRepository, LoyaltyService loyaltyService) {
        this.flightRepository      = flightRepository;
        this.flightMapper          = flightMapper;
        this.airlineQueryService        = airlineQueryService;
        this.aircraftQueryService       = aircraftQueryService;
        this.routeService          = routeService;
        this.seatInventoryService  = seatInventoryService;
        this.seatService           = seatService;
        this.flightSearchCacheService = flightSearchCacheService;
        this.eventPublisher = eventPublisher;
        this.bookingRepository = bookingRepository;
        this.loyaltyService = loyaltyService;
    }

    /**
     * Gets flights.
     *
     * @param request  the request
     * @param pageable the pageable
     * @return the flights
     */
    public List<FlightResponse> getFlights(FlightGetRequest request, Pageable pageable) {
        return flightRepository.findAll(request.toSpecification(), pageable)
                .map(flightMapper::toResponse)
                .getContent();
    }

    /**
     * Gets flight by id.
     *
     * @param id the id
     * @return the flight by id
     */
    public FlightResponse getFlightById(Long id) {
        return flightMapper.toResponse(findFlightById(id));
    }

    /**
     * Create flight flight response.
     *
     * @param request the request
     * @return the flight response
     */
    @Transactional
    public FlightResponse createFlight(FlightRequest request) {
        validateFlightNumberUnique(request.flightNumber(), request.departureDate());

        Airline airline = airlineQueryService.findAirlineById(request.airlineId());
        Aircraft aircraft = aircraftQueryService.findAircraftById(request.aircraftId());
        Route route = routeService.findRouteById(request.routeId());

        validateAircraftBelongsToAirline(aircraft, airline);

        Flight flight = flightMapper.toEntity(request);
        flight.setAirline(airline);
        flight.setAircraft(aircraft);
        flight.setRoute(route);
        flight.setCreatedAt(java.time.LocalDateTime.now());

        Flight savedFlight = flightRepository.save(flight);
        seatInventoryService.initializeInventory(savedFlight);
        seatService.generateSeatMap(savedFlight);

        return flightMapper.toResponse(savedFlight);
    }

    /**
     * Update flight flight response.
     *
     * @param id      the id
     * @param request the request
     * @return the flight response
     */
    @Transactional
    public FlightResponse updateFlight(Long id, FlightRequest request) {
        Flight flight = findFlightById(id);

        if (!flight.getFlightNumber().equals(request.flightNumber())
                || !flight.getDepartureDate().equals(request.departureDate())) {
            validateFlightNumberUnique(request.flightNumber(), request.departureDate());
        }

        Airline airline = airlineQueryService.findAirlineById(request.airlineId());
        Aircraft aircraft = aircraftQueryService.findAircraftById(request.aircraftId());
        Route route = routeService.findRouteById(request.routeId());

        validateAircraftBelongsToAirline(aircraft, airline);

        flightMapper.updateEntity(request, flight);
        flight.setAirline(airline);
        flight.setAircraft(aircraft);
        flight.setRoute(route);

        FlightResponse response = flightMapper.toResponse(flightRepository.save(flight));

        // Evict search cache for this route
        evictSearchCache(flight);

        return response;
    }

    /**
     * Update flight status flight response.
     *
     * @param id      the id
     * @param request the request
     * @return the flight response
     */
    @Transactional
    public FlightResponse updateFlightStatus(Long id, FlightStatusUpdateRequest request) {
        Flight flight = findFlightById(id);

        if (request.status() == com.aerobook.domain.enums.FlightStatus.DELAYED
                && (request.delayMinutes() == null || request.delayMinutes() <= 0)) {
            throw new AeroBookException(
                    "Delay minutes must be provided and positive when status is DELAYED",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DELAY"
            );
        }

        flight.setStatus(request.status());
        flight.setDelayMinutes(request.delayMinutes());


        Flight saved = flightRepository.save(flight);

        // When flight lands — award miles to all passengers
        if (request.status() == FlightStatus.LANDED) {
            eventPublisher.publishEvent(new FlightCompletedEvent(this, saved));
        }

        evictSearchCache(saved);
        return flightMapper.toResponse(saved);
    }

    /**
     * Delete flight.
     *
     * @param id the id
     */
    @Transactional
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight", id);
        }
        flightRepository.deleteById(id);
    }

    /**
     * Find flight by id flight.
     *
     * @param id the id
     * @return the flight
     */
    public Flight findFlightById(Long id) {
        return flightRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", id));
    }

    private void validateFlightNumberUnique(String flightNumber,
                                            java.time.LocalDate departureDate) {
        if (flightRepository.existsByFlightNumberAndDepartureDate(
                flightNumber, departureDate)) {
            throw new DuplicateResourceException(
                    "Flight", "flightNumber + departureDate",
                    flightNumber + " on " + departureDate);
        }
    }

    private void validateAircraftBelongsToAirline(Aircraft aircraft, Airline airline) {
        if (!aircraft.getAirline().getId().equals(airline.getId())) {
            throw new AeroBookException(
                    "Aircraft " + aircraft.getRegistrationNumber()
                            + " does not belong to airline " + airline.getIataCode(),
                    HttpStatus.BAD_REQUEST,
                    "AIRCRAFT_AIRLINE_MISMATCH"
            );
        }
    }

    /**
     * Exists by flight number and date boolean.
     *
     * @param flightNumber the flight number
     * @param date         the date
     * @return the boolean
     */
    public boolean existsByFlightNumberAndDate(String flightNumber, LocalDate date) {
        return flightRepository.existsByFlightNumberAndDepartureDate(flightNumber, date);
    }

    private void evictSearchCache(Flight flight) {
        String origin      = flight.getRoute().getOrigin().getIataCode();
        String destination = flight.getRoute().getDestination().getIataCode();
        // Evict all search cache entries for this route
        flightSearchCacheService.evictByPattern(
                "search:*:" + origin + ":" + destination + ":*");
    }

    // In FlightService — called by listener
    @Transactional
    public void awardMilesForCompletedFlight(Flight flight) {
        // Find all CONFIRMED bookings on this flight
        bookingRepository.findAllByOutboundFlightIdAndStatus(
                        flight.getId(), BookingStatus.CONFIRMED)
                .forEach(booking -> {
                    try {
                        loyaltyService.awardMilesForFlight(
                                booking.getUser().getId(),
                                flight,
                                booking.getOutboundSeatClass()
                        );
                    } catch (Exception e) {
                        log.error("Failed to award miles — booking: {}, error: {}",
                                booking.getPnr(), e.getMessage());
                    }
                });
    }

}
