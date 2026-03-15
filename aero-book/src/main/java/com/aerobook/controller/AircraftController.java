package com.aerobook.controller;


import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.request.get.AircraftGetRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import com.aerobook.service.AircraftSeatConfigService;
import com.aerobook.service.AircraftService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Aircraft controller.
 */
@RestController
@RequestMapping("/aircraft")
@AllArgsConstructor
public class AircraftController {

    private final AircraftService aircraftService;
    private final AircraftSeatConfigService seatConfigService;

    /**
     * Gets aircraft.
     *
     * @param id                 the id
     * @param registrationNumber the registration number
     * @param airlineId          the airline id
     * @param status             the status
     * @param pageable           the pageable
     * @return the aircraft
     */
    @GetMapping
    @AuthenticatedEndpoint
    public ResponseEntity<?> getAircraft(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(required = false) Long airlineId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        AircraftGetRequest request = AircraftGetRequest.builder()
                .id(id)
                .registrationNumber(registrationNumber)
                .airlineId(airlineId)
                .status(status)
                .build();

        return ResponseEntity.ok(aircraftService.getAircraft(request, pageable));
    }

    /**
     * Create aircraft response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AircraftResponse> createAircraft(
            @Valid @RequestBody AircraftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aircraftService.createAircraft(request));
    }

    /**
     * Update aircraft response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AircraftResponse> updateAircraft(
            @PathVariable Long id,
            @Valid @RequestBody AircraftRequest request) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, request));
    }

    /**
     * Delete aircraft response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets seat configs.
     *
     * @param aircraftId the aircraft id
     * @return the seat configs
     */
    @GetMapping("/{aircraftId}/seat-configs")
    @AuthenticatedEndpoint
    public ResponseEntity<List<AircraftSeatConfigResponse>> getSeatConfigs(@PathVariable Long aircraftId) {
        return ResponseEntity.ok(seatConfigService.getSeatConfigsByAircraft(aircraftId));
    }

    /**
     * Add seat config response entity.
     *
     * @param aircraftId the aircraft id
     * @param request    the request
     * @return the response entity
     */
    @PostMapping("/{aircraftId}/seat-configs")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AircraftSeatConfigResponse> addSeatConfig(
            @PathVariable Long aircraftId,
            @Valid @RequestBody AircraftSeatConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seatConfigService.addSeatConfig(aircraftId, request));
    }

    /**
     * Update seat config response entity.
     *
     * @param aircraftId the aircraft id
     * @param configId   the config id
     * @param request    the request
     * @return the response entity
     */
    @PutMapping("/{aircraftId}/seat-configs/{configId}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AircraftSeatConfigResponse> updateSeatConfig(
            @PathVariable Long aircraftId,
            @PathVariable Long configId,
            @Valid @RequestBody AircraftSeatConfigRequest request) {
        return ResponseEntity.ok(seatConfigService.updateSeatConfig(configId, request));
    }

    /**
     * Delete seat config response entity.
     *
     * @param aircraftId the aircraft id
     * @param configId   the config id
     * @return the response entity
     */
    @DeleteMapping("/{aircraftId}/seat-configs/{configId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSeatConfig(
            @PathVariable Long aircraftId,
            @PathVariable Long configId) {
        seatConfigService.deleteSeatConfig(configId);
        return ResponseEntity.noContent().build();
    }
}
