package com.aerobook.controller;

import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.service.AirlineService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airlines")
@AllArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    @GetMapping
    public ResponseEntity<List<AirlineResponse>> getAllAirlines() {
        return ResponseEntity.ok(airlineService.getAllAirlines());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AirlineResponse>> getActiveAirlines() {
        return ResponseEntity.ok(airlineService.getActiveAirlines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineResponse> getAirlineById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.getAirlineById(id));
    }

    @GetMapping("/iata/{code}")
    public ResponseEntity<AirlineResponse> getAirlineByIataCode(@PathVariable String code) {
        return ResponseEntity.ok(airlineService.getAirlineByIataCode(code));
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
