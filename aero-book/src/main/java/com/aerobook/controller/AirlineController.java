package com.aerobook.controller;

import com.aerobook.domain.dto.request.AirlineGetRequest;
import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.service.AirlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/airlines")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    /**
     * GET /api/v1/airlines?id=1
     * GET /api/v1/airlines?iataCode=AI
     * GET /api/v1/airlines?status=ACTIVE
     * GET /api/v1/airlines?country=India
     *
     * Exactly one param must be passed.
     */
    @GetMapping
    public ResponseEntity<?> getAirline(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String iataCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String country) {

        AirlineGetRequest request = AirlineGetRequest.builder()
                .id(id)
                .iataCode(iataCode)
                .status(status)
                .country(country)
                .build();

        request.validate();

        return ResponseEntity.ok(airlineService.getAirline(request));
    }

    @PostMapping
    public ResponseEntity<AirlineResponse> createAirline(@Valid @RequestBody AirlineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airlineService.createAirline(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirlineResponse> updateAirline(@PathVariable Long id,
                                                         @Valid @RequestBody AirlineRequest request) {
        return ResponseEntity.ok(airlineService.updateAirline(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirline(@PathVariable Long id) {
        airlineService.deleteAirline(id);
        return ResponseEntity.noContent().build();
    }
}
