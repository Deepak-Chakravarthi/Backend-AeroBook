package com.aerobook.controller;


import com.aerobook.domain.dto.request.get.PassengerGetRequest;
import com.aerobook.domain.dto.request.PassengerRequest;
import com.aerobook.domain.dto.response.PassengerResponse;
import com.aerobook.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    // ── Admin — view all passengers ───────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT', 'AIRLINE_ADMIN')")
    public ResponseEntity<List<PassengerResponse>> getPassengers(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String passengerType,
            @RequestParam(required = false) String passportNumber,
            Pageable pageable) {

        PassengerGetRequest request = PassengerGetRequest.builder()
                .bookingId(bookingId).userId(userId)
                .firstName(firstName).lastName(lastName)
                .passengerType(passengerType)
                .passportNumber(passportNumber)
                .build();

        return ResponseEntity.ok(passengerService.getPassengers(request, pageable));
    }

    // ── Get by id ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PassengerResponse> getPassengerById(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    // ── Get by booking ────────────────────────────────────────────────
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PassengerResponse>> getPassengersByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(
                passengerService.getPassengersByBooking(bookingId));
    }

    // ── Add passenger to booking ──────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<PassengerResponse> addPassenger(
            @Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(passengerService.addPassenger(request));
    }

    // ── Update passenger ──────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<PassengerResponse> updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.ok(passengerService.updatePassenger(id, request));
    }

    // ── Delete passenger ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.noContent().build();
    }
}