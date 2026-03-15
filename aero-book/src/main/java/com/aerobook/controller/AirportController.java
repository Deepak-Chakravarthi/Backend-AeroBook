package com.aerobook.controller;


import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.request.get.AirportGetRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * The type Airport controller.
 */
@RestController
@RequestMapping("/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    /**
     * Method to get Airport
     *
     * @param id       the id
     * @param iataCode the iata code
     * @param city     the city
     * @param country  the country
     * @param pageable the pageable
     * @return the airport
     */
    @GetMapping
    @AuthenticatedEndpoint
    public ResponseEntity<?> getAirport(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String iataCode,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            Pageable pageable) {

        AirportGetRequest request = AirportGetRequest.builder()
                .id(id)
                .iataCode(iataCode)
                .city(city)
                .country(country)
                .build();

        return ResponseEntity.ok(airportService.getAirports(request, pageable));
    }

    /**
     * Create airport response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AirportResponse> createAirport(
            @Valid @RequestBody AirportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(airportService.createAirport(request));
    }

    /**
     * Update airport response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AirportResponse> updateAirport(
            @PathVariable Long id,
            @Valid @RequestBody AirportRequest request) {
        return ResponseEntity.ok(airportService.updateAirport(id, request));
    }

    /**
     * Delete airport response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAirport(@PathVariable Long id) {
        airportService.deleteAirport(id);
        return ResponseEntity.noContent().build();
    }
}
