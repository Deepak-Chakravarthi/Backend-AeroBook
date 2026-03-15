package com.aerobook.controller;


import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.annotations.ExemptAuthorization;
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

/**
 * The type Flight schedule controller.
 */
@RestController
@RequestMapping("/flight-schedules")
@RequiredArgsConstructor
public class FlightScheduleController {

    private final FlightScheduleService scheduleService;

    /**
     * Gets all active schedules.
     *
     * @return the all active schedules
     */
    @GetMapping
    @ExemptAuthorization(reason = "Schedule listing is publicly accessible without login")
    public ResponseEntity<List<FlightScheduleResponse>> getAllActiveSchedules() {
        return ResponseEntity.ok(scheduleService.getAllActiveSchedules());
    }

    /**
     * Gets schedule by id.
     *
     * @param id the id
     * @return the schedule by id
     */
    @GetMapping("/{id}")
    @ExemptAuthorization(reason = "Schedule listing is publicly accessible without login")
     public ResponseEntity<FlightScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    /**
     * Create schedule response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleResponse> createSchedule(
            @Valid @RequestBody FlightScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(request));
    }

    /**
     * Generate flights response entity.
     *
     * @param id    the id
     * @param from  the from
     * @param until the until
     * @return the response entity
     */
    @PostMapping("/{id}/generate-flights")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> generateFlights(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until) {

        int count = scheduleService.generateFlightsFromSchedule(id, from, until);
        return ResponseEntity.ok("Generated " + count + " flights from schedule " + id);
    }

    /**
     * Deactivate schedule response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateSchedule(@PathVariable Long id) {
        scheduleService.deactivateSchedule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete schedule response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
