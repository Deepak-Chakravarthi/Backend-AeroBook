package com.aerobook.controller;

import com.aerobook.annotations.AuthenticatedEndpoint;
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

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService     flightService;
    private final FlightFareService flightFareService;

    @GetMapping
    @AuthenticatedEndpoint(reason = "Any logged-in user can view flights")
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
                        ? java.time.LocalDate.parse(departureDate) : null)
                .status(status)
                .originCode(originCode)
                .destinationCode(destinationCode)
                .build();

        return ResponseEntity.ok(flightService.getFlights(request, pageable));
    }

    @GetMapping("/{id}")
    @AuthenticatedEndpoint(reason = "Any logged-in user can view flight details")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @GetMapping("/{flightId}/fares")
    @AuthenticatedEndpoint(reason = "Any logged-in user can view fares")
    public ResponseEntity<List<FlightFareResponse>> getFares(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightFareService.getFaresByFlight(flightId));
    }

    // ── CUD — AIRLINE_ADMIN / SUPER_ADMIN ────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> createFlight(
            @Valid @RequestBody FlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightService.createFlight(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> updateFlightStatus(
            @PathVariable Long id,
            @Valid @RequestBody FlightStatusUpdateRequest request) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    // ── Fare management ───────────────────────────────────────────────
    @PostMapping("/{flightId}/fares")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightFareResponse> addFare(
            @PathVariable Long flightId,
            @Valid @RequestBody FlightFareRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightFareService.addFare(flightId, request));
    }

    @PutMapping("/{flightId}/fares/{fareId}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightFareResponse> updateFare(
            @PathVariable Long flightId,
            @PathVariable Long fareId,
            @Valid @RequestBody FlightFareRequest request) {
        return ResponseEntity.ok(flightFareService.updateFare(fareId, request));
    }

    @DeleteMapping("/{flightId}/fares/{fareId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFare(
            @PathVariable Long flightId,
            @PathVariable Long fareId) {
        flightFareService.deleteFare(fareId);
        return ResponseEntity.noContent().build();
    }
}
