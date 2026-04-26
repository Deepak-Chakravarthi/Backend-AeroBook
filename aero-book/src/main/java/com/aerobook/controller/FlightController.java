package com.aerobook.controller;

import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.annotations.ExemptAuthorization;
import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.*;
import com.aerobook.domain.dto.request.get.FlightGetRequest;
import com.aerobook.domain.dto.response.FlightFareResponse;
import com.aerobook.domain.dto.response.FlightResponse;
import com.aerobook.service.FlightFareService;
import com.aerobook.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.time.LocalDate.parse;

/**
 * The type Flight controller.
 */
@RestController
@RequestMapping(ApiConstants.FLIGHTS)
@RequiredArgsConstructor
public class FlightController {

    private final FlightService     flightService;
    private final FlightFareService flightFareService;

    /**
     * Gets flights.
     *
     * @param id              the id
     * @param flightNumber    the flight number
     * @param airlineId       the airline id
     * @param aircraftId      the aircraft id
     * @param routeId         the route id
     * @param departureDate   the departure date
     * @param status          the status
     * @param originCode      the origin code
     * @param destinationCode the destination code
     * @param pageable        the pageable
     * @return the flights
     */
    @GetMapping
    @ExemptAuthorization(reason = "Flight listing is publicly accessible without login")
    public ResponseEntity<List<FlightResponse>> getFlights(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String flightNumber,
            @RequestParam(required = false) Long airlineId,
            @RequestParam(required = false) Long aircraftId,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) String departureDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String originCode,
            @RequestParam(required = false) String destinationCode,
            Pageable pageable) {

        FlightGetRequest request = FlightGetRequest.builder()
                .id(id)
                .flightNumber(flightNumber)
                .airlineId(airlineId)
                .aircraftId(aircraftId)
                .routeId(routeId)
                .departureDate(departureDate != null
                        ? parse(departureDate) : null)
                .status(status)
                .originCode(originCode)
                .destinationCode(destinationCode)
                .build();

        return ResponseEntity.ok(flightService.getFlights(request, pageable));
    }

    /**
     * Gets flight by id.
     *
     * @param id the id
     * @return the flight by id
     */
    @GetMapping("/{id}")
    @ExemptAuthorization(reason = "Flight details are publicly accessible without login")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    /**
     * Gets fares.
     *
     * @param flightId the flight id
     * @return the fares
     */
    @GetMapping("/{flightId}/fares")
    @ExemptAuthorization(reason = "Fare details are publicly accessible without login")
    public ResponseEntity<List<FlightFareResponse>> getFares(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightFareService.getFaresByFlight(flightId));
    }

    /**
     * Create flight response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> createFlight(
            @Valid @RequestBody FlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightService.createFlight(request));
    }

    /**
     * Update flight response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(id, request));
    }

    /**
     * Update flight status response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> updateFlightStatus(
            @PathVariable Long id,
            @Valid @RequestBody FlightStatusUpdateRequest request) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, request));
    }

    /**
     * Delete flight response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add fare response entity.
     *
     * @param flightId the flight id
     * @param request  the request
     * @return the response entity
     */
    @PostMapping("/{flightId}/fares")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightFareResponse> addFare(
            @PathVariable Long flightId,
            @Valid @RequestBody FlightFareRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightFareService.addFare(flightId, request));
    }

    /**
     * Update fare response entity.
     *
     * @param flightId the flight id
     * @param fareId   the fare id
     * @param request  the request
     * @return the response entity
     */
    @PutMapping("/{flightId}/fares/{fareId}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightFareResponse> updateFare(
            @PathVariable Long flightId,
            @PathVariable Long fareId,
            @Valid @RequestBody FlightFareRequest request) {
        return ResponseEntity.ok(flightFareService.updateFare(fareId, request));
    }

    /**
     * Delete fare response entity.
     *
     * @param flightId the flight id
     * @param fareId   the fare id
     * @return the response entity
     */
    @DeleteMapping("/{flightId}/fares/{fareId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFare(
            @PathVariable Long flightId,
            @PathVariable Long fareId) {
        flightFareService.deleteFare(fareId);
        return ResponseEntity.noContent().build();
    }
}
