package com.aerobook.controller;


import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.get.CheckInGetRequest;
import com.aerobook.domain.dto.request.CheckInRequest;
import com.aerobook.domain.dto.response.BoardingPassResponse;
import com.aerobook.domain.dto.response.CheckInResponse;
import com.aerobook.service.BoardingPassService;
import com.aerobook.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Check in controller.
 */
@RestController
@RequestMapping(ApiConstants.CHECK_IN)
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService     checkInService;
    private final BoardingPassService boardingPassService;

    /**
     * Gets check ins.
     *
     * @param id          the id
     * @param ticketId    the ticket id
     * @param bookingId   the booking id
     * @param passengerId the passenger id
     * @param flightId    the flight id
     * @param status      the status
     * @param pageable    the pageable
     * @return the check ins
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT', 'AIRLINE_ADMIN')")
    public ResponseEntity<List<CheckInResponse>> getCheckIns(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        CheckInGetRequest request = CheckInGetRequest.builder()
                .id(id).ticketId(ticketId).bookingId(bookingId)
                .passengerId(passengerId).flightId(flightId)
                .status(status).build();

        return ResponseEntity.ok(checkInService.getCheckIns(request, pageable));
    }

    /**
     * Gets check in by id.
     *
     * @param id the id
     * @return the check in by id
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckInResponse> getCheckInById(@PathVariable Long id) {
        return ResponseEntity.ok(checkInService.getCheckInById(id));
    }

    /**
     * Gets check in by ticket.
     *
     * @param ticketId the ticket id
     * @return the check in by ticket
     */

    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckInResponse> getCheckInByTicket(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(checkInService.getCheckInByTicket(ticketId));
    }

    /**
     * Check in response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<CheckInResponse> checkIn(
            @Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkInService.checkIn(request));
    }

    /**
     * Issue boarding pass response entity.
     *
     * @param checkInId the check in id
     * @return the response entity
     */
    @PostMapping("/{checkInId}/boarding-pass")
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<BoardingPassResponse> issueBoardingPass(
            @PathVariable Long checkInId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardingPassService.issueBoardingPass(checkInId));
    }

    /**
     * Gets boarding pass.
     *
     * @param checkInId the check in id
     * @return the boarding pass
     */

    @GetMapping("/{checkInId}/boarding-pass")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BoardingPassResponse> getBoardingPass(
            @PathVariable Long checkInId) {
        return ResponseEntity.ok(
                boardingPassService.getBoardingPassByCheckIn(checkInId));
    }
}