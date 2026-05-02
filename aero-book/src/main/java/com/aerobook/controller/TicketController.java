package com.aerobook.controller;


import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.get.TicketGetRequest;
import com.aerobook.domain.dto.response.TicketResponse;
import com.aerobook.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Ticket controller.
 */
@RestController
@RequestMapping(ApiConstants.TICKETS)
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Gets tickets.
     *
     * @param id           the id
     * @param ticketNumber the ticket number
     * @param bookingId    the booking id
     * @param passengerId  the passenger id
     * @param flightId     the flight id
     * @param seatClass    the seat class
     * @param status       the status
     * @param isReturnLeg  the is return leg
     * @param pageable     the pageable
     * @return the tickets
     */

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT', 'AIRLINE_ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTickets(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String ticketNumber,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) String seatClass,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isReturnLeg,
            Pageable pageable) {

        TicketGetRequest request = TicketGetRequest.builder()
                .id(id).ticketNumber(ticketNumber)
                .bookingId(bookingId).passengerId(passengerId)
                .flightId(flightId).seatClass(seatClass)
                .status(status).isReturnLeg(isReturnLeg)
                .build();

        return ResponseEntity.ok(ticketService.getTickets(request, pageable));
    }

    /**
     * Gets ticket by id.
     *
     * @param id the id
     * @return the ticket by id
     */

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    /**
     * Gets tickets by booking.
     *
     * @param bookingId the booking id
     * @return the tickets by booking
     */

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketResponse>> getTicketsByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ticketService.getTicketsByBooking(bookingId));
    }

    /**
     * Issue tickets response entity.
     *
     * @param bookingId the booking id
     * @return the response entity
     */

    @PostMapping("/issue/{bookingId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<List<TicketResponse>> issueTickets(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ticketService.issueTickets(bookingId));
    }

    /**
     * Cancel tickets response entity.
     *
     * @param bookingId the booking id
     * @return the response entity
     */
    @PostMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<Void> cancelTickets(@PathVariable Long bookingId) {
        ticketService.cancelTickets(bookingId);
        return ResponseEntity.noContent().build();
    }
}
