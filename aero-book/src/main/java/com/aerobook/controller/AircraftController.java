package com.aerobook.controller;


import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import com.aerobook.service.AircraftService;
import com.aerobook.service.AircraftSeatConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aircraft")
@RequiredArgsConstructor
public class AircraftController {

    private final AircraftService aircraftService;
    private final AircraftSeatConfigService seatConfigService;

    @GetMapping
    public ResponseEntity<List<AircraftResponse>> getAllAircraft() {
        return ResponseEntity.ok(aircraftService.getAllAircraft());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftResponse> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraftById(id));
    }

    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<List<AircraftResponse>> getAircraftByAirline(@PathVariable Long airlineId) {
        return ResponseEntity.ok(aircraftService.getAircraftByAirline(airlineId));
    }

    @PostMapping
    public ResponseEntity<AircraftResponse> createAircraft(@Valid @RequestBody AircraftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aircraftService.createAircraft(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftResponse> updateAircraft(@PathVariable Long id,
                                                           @Valid @RequestBody AircraftRequest request) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }

    // Seat config endpoints nested under aircraft
    @GetMapping("/{aircraftId}/seat-configs")
    public ResponseEntity<List<AircraftSeatConfigResponse>> getSeatConfigs(@PathVariable Long aircraftId) {
        return ResponseEntity.ok(seatConfigService.getSeatConfigsByAircraft(aircraftId));
    }

    @PostMapping("/{aircraftId}/seat-configs")
    public ResponseEntity<AircraftSeatConfigResponse> addSeatConfig(@PathVariable Long aircraftId,
                                                                    @Valid @RequestBody AircraftSeatConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatConfigService.addSeatConfig(aircraftId, request));
    }

    @PutMapping("/{aircraftId}/seat-configs/{configId}")
    public ResponseEntity<AircraftSeatConfigResponse> updateSeatConfig(@PathVariable Long aircraftId,
                                                                       @PathVariable Long configId,
                                                                       @Valid @RequestBody AircraftSeatConfigRequest request) {
        return ResponseEntity.ok(seatConfigService.updateSeatConfig(configId, request));
    }

    @DeleteMapping("/{aircraftId}/seat-configs/{configId}")
    public ResponseEntity<Void> deleteSeatConfig(@PathVariable Long aircraftId,
                                                 @PathVariable Long configId) {
        seatConfigService.deleteSeatConfig(configId);
        return ResponseEntity.noContent().build();
    }
}
