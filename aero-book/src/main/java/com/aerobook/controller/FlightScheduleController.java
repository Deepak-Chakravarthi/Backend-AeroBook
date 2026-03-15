package com.aerobook.controller;


import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.domain.dto.request.FlightScheduleRequest;
import com.aerobook.domain.dto.response.FlightScheduleResponse;
import com.aerobook.service.FlightScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/flight-schedules")
@RequiredArgsConstructor
public class FlightScheduleController {

    private final FlightScheduleService scheduleService;

    @GetMapping
    @AuthenticatedEndpoint(reason = "Any logged-in user can view schedules")
    public ResponseEntity<List<FlightScheduleResponse>> getAllActiveSchedules() {
        return ResponseEntity.ok(scheduleService.getAllActiveSchedules());
    }

    @GetMapping("/{id}")
    @AuthenticatedEndpoint(reason = "Any logged-in user can view schedule details")
    public ResponseEntity<FlightScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleResponse> createSchedule(
            @Valid @RequestBody FlightScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(request));
    }

    @PostMapping("/{id}/generate-flights")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> generateFlights(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until) {

        int count = scheduleService.generateFlightsFromSchedule(id, from, until);
        return ResponseEntity.ok("Generated " + count + " flights from schedule " + id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateSchedule(@PathVariable Long id) {
        scheduleService.deactivateSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
