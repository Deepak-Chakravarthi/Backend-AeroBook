package com.aerobook.controller;


import com.aerobook.domain.dto.request.get.AirportGetRequest;
import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    /**
     * GET /api/v1/airports?id=1
     * GET /api/v1/airports?iataCode=DEL
     * GET /api/v1/airports?city=Mumbai
     * GET /api/v1/airports?country=India
     * <p>
     * Exactly one param must be passed.
     */
    @GetMapping
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

        return ResponseEntity.ok(airportService.getAirports(request,pageable));
    }

    @PostMapping
    public ResponseEntity<AirportResponse> createAirport(@Valid @RequestBody AirportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.createAirport(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirportResponse> updateAirport(@PathVariable Long id,
                                                         @Valid @RequestBody AirportRequest request) {
        return ResponseEntity.ok(airportService.updateAirport(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirport(@PathVariable Long id) {
        airportService.deleteAirport(id);
        return ResponseEntity.noContent().build();
    }
}
