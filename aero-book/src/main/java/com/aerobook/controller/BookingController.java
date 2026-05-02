package com.aerobook.controller;


import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.BookingCancelRequest;
import com.aerobook.domain.dto.request.get.BookingGetRequest;
import com.aerobook.domain.dto.request.BookingRequest;
import com.aerobook.domain.dto.response.BookingResponse;
import com.aerobook.domain.dto.response.BookingSummaryResponse;
import com.aerobook.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Booking controller.
 */
@RestController
@RequestMapping(ApiConstants.BOOKINGS)
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Gets bookings.
     *
     * @param id          the id
     * @param pnr         the pnr
     * @param userId      the user id
     * @param status      the status
     * @param bookingType the booking type
     * @param pageable    the pageable
     * @return the bookings
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT', 'AIRLINE_ADMIN')")
    public ResponseEntity<List<BookingSummaryResponse>> getBookings(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String pnr,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bookingType,
            Pageable pageable) {

        BookingGetRequest request = BookingGetRequest.builder()
                .id(id).pnr(pnr).userId(userId)
                .status(status).bookingType(bookingType)
                .build();

        return ResponseEntity.ok(bookingService.getBookings(request, pageable));
    }

    /**
     * Gets my bookings.
     *
     * @return the my bookings
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingSummaryResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    /**
     * Gets booking by id.
     *
     * @param id the id
     * @return the booking by id
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * Gets booking by pnr.
     *
     * @param pnr the pnr
     * @return the booking by pnr
     */
    @GetMapping("/pnr/{pnr}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> getBookingByPnr(@PathVariable String pnr) {
        return ResponseEntity.ok(bookingService.getBookingByPnr(pnr));
    }

    /**
     * Create booking response entity.
     *
     * @param request the request
     * @return the response entity
     */

    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request));
    }

    /**
     * Lock seats response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @PostMapping("/{id}/lock-seats")
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<BookingResponse> lockSeats(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.lockSeats(id));
    }

    /**
     * Initiate payment response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @PostMapping("/{id}/initiate-payment")
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<BookingResponse> initiatePayment(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.initiatePayment(id));
    }

    /**
     * Confirm booking response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    /**
     * Cancel booking response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER', 'AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingCancelRequest request) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, request));
    }
}
