package com.aerobook.controller;


import com.aerobook.domain.dto.request.get.TicketGetRequest;
import com.aerobook.domain.dto.response.TicketResponse;
import com.aerobook.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // ── Get tickets — filterable ──────────────────────────────────────
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

    // ── Get by id ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    // ── Get by booking ────────────────────────────────────────────────
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketResponse>> getTicketsByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ticketService.getTicketsByBooking(bookingId));
    }

    // ── Issue tickets — on booking confirmation ───────────────────────
    @PostMapping("/issue/{bookingId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<List<TicketResponse>> issueTickets(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ticketService.issueTickets(bookingId));
    }

    // ── Cancel tickets ────────────────────────────────────────────────
    @PostMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<Void> cancelTickets(@PathVariable Long bookingId) {
        ticketService.cancelTickets(bookingId);
        return ResponseEntity.noContent().build();
    }
}
