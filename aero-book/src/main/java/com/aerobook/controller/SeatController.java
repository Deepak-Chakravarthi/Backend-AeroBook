package com.aerobook.controller;


import com.aerobook.annotations.ExemptAuthorization;
import com.aerobook.domain.dto.request.SeatHoldRequest;
import com.aerobook.domain.dto.request.SeatReleaseRequest;
import com.aerobook.domain.dto.request.get.SeatGetRequest;
import com.aerobook.domain.dto.response.SeatHoldResponse;
import com.aerobook.domain.dto.response.SeatInventoryResponse;
import com.aerobook.domain.dto.response.SeatResponse;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.service.SeatInventoryService;
import com.aerobook.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Seat controller.
 */
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final SeatInventoryService inventoryService;

    /**
     * Gets seats.
     *
     * @param flightId   the flight id
     * @param seatNumber the seat number
     * @param seatClass  the seat class
     * @param seatType   the seat type
     * @param status     the status
     * @param pageable   the pageable
     * @return the seats
     */
    @GetMapping
    @ExemptAuthorization(reason = "Seat map is publicly visible for flight selection")
    public ResponseEntity<List<SeatResponse>> getSeats(
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) String seatNumber,
            @RequestParam(required = false) String seatClass,
            @RequestParam(required = false) String seatType,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        SeatGetRequest request = SeatGetRequest.builder()
                .flightId(flightId).seatNumber(seatNumber)
                .seatClass(seatClass).seatType(seatType)
                .status(status).build();

        return ResponseEntity.ok(seatService.getSeats(request, pageable));
    }

    /**
     * Gets inventory.
     *
     * @param flightId the flight id
     * @return the inventory
     */
    @GetMapping("/inventory/{flightId}")
    @ExemptAuthorization(reason = "Seat availability is publicly visible")
    public ResponseEntity<List<SeatInventoryResponse>> getInventory(
            @PathVariable Long flightId) {
        return ResponseEntity.ok(inventoryService.getInventoryByFlight(flightId));
    }

    /**
     * Gets inventory by class.
     *
     * @param flightId  the flight id
     * @param seatClass the seat class
     * @return the inventory by class
     */
    @GetMapping("/inventory/{flightId}/{seatClass}")
    @ExemptAuthorization(reason = "Seat availability by class is publicly visible")
    public ResponseEntity<SeatInventoryResponse> getInventoryByClass(
            @PathVariable Long flightId,
            @PathVariable SeatClass seatClass) {
        return ResponseEntity.ok(
                inventoryService.getInventoryByFlightAndClass(flightId, seatClass));
    }

    /**
     * Hold seats response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/hold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeatHoldResponse> holdSeats(
            @Valid @RequestBody SeatHoldRequest request) {
        return ResponseEntity.ok(seatService.holdSeats(request));
    }

    /**
     * Release seats response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/release")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> releaseSeats(
            @Valid @RequestBody SeatReleaseRequest request) {
        seatService.releaseSeats(request);
        return ResponseEntity.noContent().build();
    }


    /**
     * Generate seat map response entity.
     *
     * @param flightId the flight id
     * @return the response entity
     */
    @PostMapping("/generate/{flightId}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> generateSeatMap(@PathVariable Long flightId) {
        var flight = seatService.generateSeatMapForFlight(flightId);
        return ResponseEntity.ok("Seat map generated for flight " + flightId);
    }
}
