package com.aerobook.controller;

import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.service.AirportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airports")
@AllArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<List<AirportResponse>> getAllAirports() {
        return ResponseEntity.ok(airportService.getAllAirports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponse> getAirportById(@PathVariable Long id) {
        return ResponseEntity.ok(airportService.getAirportById(id));
    }

    @GetMapping("/iata/{code}")
    public ResponseEntity<AirportResponse> getAirportByIataCode(@PathVariable String code) {
        return ResponseEntity.ok(airportService.getAirportByIataCode(code));
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
