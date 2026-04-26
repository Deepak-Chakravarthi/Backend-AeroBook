package com.aerobook.controller;

import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.request.get.AirlineGetRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.service.AirlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * The type Airline controller.
 */
@RestController
@RequestMapping(ApiConstants.AIRLINES)
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;


    /**
     * Gets airline.
     *
     * @param id       the id
     * @param iataCode the iata code
     * @param status   the status
     * @param country  the country
     * @param pageable the pageable
     * @return the airline
     */
    @GetMapping
    @AuthenticatedEndpoint
    public ResponseEntity<?> getAirline(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String iataCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String country,
            Pageable pageable) {

        AirlineGetRequest request = AirlineGetRequest.builder()
                .id(id)
                .iataCode(iataCode)
                .status(status)
                .country(country)
                .build();

        return ResponseEntity.ok(airlineService.getAirlines(request, pageable));
    }

    /**
     * Create airline response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AirlineResponse> createAirline(
            @Valid @RequestBody AirlineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(airlineService.createAirline(request));
    }

    /**
     * Update airline response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AirlineResponse> updateAirline(
            @PathVariable Long id,
            @Valid @RequestBody AirlineRequest request) {
        return ResponseEntity.ok(airlineService.updateAirline(id, request));
    }

    /**
     * Delete airline response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAirline(@PathVariable Long id) {
        airlineService.deleteAirline(id);
        return ResponseEntity.noContent().build();
    }
}
