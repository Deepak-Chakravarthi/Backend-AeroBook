package com.aerobook.service;


import com.aerobook.domain.dto.request.get.TicketGetRequest;
import com.aerobook.domain.dto.response.TicketResponse;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.TicketStatus;
import com.aerobook.enitity.Booking;
import com.aerobook.enitity.Flight;
import com.aerobook.enitity.Passenger;
import com.aerobook.enitity.Ticket;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.PassengerMapper;
import com.aerobook.repository.TicketRepository;
import com.aerobook.util.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository      ticketRepository;
    private final PassengerMapper       passengerMapper;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final BookingService        bookingService;
    private final PassengerService      passengerService;

    // ----------------------------------------------------------------
    // Get tickets — filterable
    // ----------------------------------------------------------------
    public List<TicketResponse> getTickets(TicketGetRequest request,
                                           Pageable pageable) {
        return ticketRepository.findAll(request.toSpecification(), pageable)
                .map(passengerMapper::toTicketResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Get ticket by id
    // ----------------------------------------------------------------
    public TicketResponse getTicketById(Long id) {
        return passengerMapper.toTicketResponse(findTicketById(id));
    }

    // ----------------------------------------------------------------
    // Get tickets by booking
    // ----------------------------------------------------------------
    public List<TicketResponse> getTicketsByBooking(Long bookingId) {
        return ticketRepository.findAllByBookingIdWithDetails(bookingId)
                .stream()
                .map(passengerMapper::toTicketResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Issue tickets — called when booking is CONFIRMED
    // Generates one ticket per flight leg
    // ----------------------------------------------------------------
    @Transactional
    public List<TicketResponse> issueTickets(Long bookingId) {
        Booking booking = bookingService.findBookingById(bookingId);

        validateBookingConfirmed(booking);
        validateNoExistingTickets(bookingId);

        List<Passenger> passengers = passengerService
                .findPassengerById(bookingId) != null
                ? List.of(passengerService.findPassengerById(bookingId))
                : List.of();

        if (passengers.isEmpty()) {
            throw new AeroBookException(
                    "No passengers found for booking: " + booking.getPnr(),
                    HttpStatus.BAD_REQUEST,
                    "NO_PASSENGERS_FOUND"
            );
        }

        List<Ticket> tickets = new ArrayList<>();

        for (Passenger passenger : passengers) {
            // Outbound ticket
            tickets.add(buildTicket(
                    passenger, booking,
                    booking.getOutboundFlight(),
                    booking.getOutboundSeatClass(),
                    booking.getOutboundSeatNumber(),
                    false
            ));

            // Return ticket if RETURN booking
            if (booking.getReturnFlight() != null) {
                tickets.add(buildTicket(
                        passenger, booking,
                        booking.getReturnFlight(),
                        booking.getReturnSeatClass(),
                        booking.getReturnSeatNumber(),
                        true
                ));
            }
        }

        List<Ticket> saved = ticketRepository.saveAll(tickets);
        log.info("Issued {} tickets for booking: {}", saved.size(), booking.getPnr());

        return saved.stream()
                .map(passengerMapper::toTicketResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Cancel tickets — called when booking is cancelled
    // ----------------------------------------------------------------
    @Transactional
    public void cancelTickets(Long bookingId) {
        List<Ticket> tickets = ticketRepository
                .findAllByBookingIdAndStatus(bookingId, TicketStatus.ISSUED);

        tickets.forEach(Ticket::cancel);
        ticketRepository.saveAll(tickets);
        log.info("Cancelled {} tickets for booking id: {}", tickets.size(), bookingId);
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------
    public Ticket findTicketById(Long id) {
        return ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------
    private Ticket buildTicket(Passenger passenger,
                               Booking booking,
                               Flight flight,
                               SeatClass seatClass,
                               String seatNumber,
                               boolean isReturnLeg) {
        String ticketNumber = generateUniqueTicketNumber(booking.getPnr());

        // Resolve fare from flight
        var fare = flight.getFares().stream()
                .filter(f -> f.getSeatClass() == seatClass)
                .findFirst()
                .orElseThrow(() -> new AeroBookException(
                        "Fare not found for flight " + flight.getFlightNumber()
                                + " class " + seatClass,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "FARE_RESOLUTION_ERROR"
                ));

        return Ticket.builder()
                .ticketNumber(ticketNumber)
                .passenger(passenger)
                .booking(booking)
                .flight(flight)
                .seatClass(seatClass)
                .seatNumber(seatNumber)
                .status(TicketStatus.ISSUED)
                .fare(fare.getBaseFare())
                .tax(fare.getTax())
                .totalFare(fare.getTotalFare())
                .isReturnLeg(isReturnLeg)
                .build();
    }

    private String generateUniqueTicketNumber(String pnr) {
        String ticketNumber;
        do {
            ticketNumber = ticketNumberGenerator.generate(pnr);
        } while (ticketRepository.existsByTicketNumber(ticketNumber));
        return ticketNumber;
    }

    private void validateBookingConfirmed(Booking booking) {
        if (booking.getStatus() != com.aerobook.domain.enums.BookingStatus.CONFIRMED) {
            throw new AeroBookException(
                    "Tickets can only be issued for CONFIRMED bookings. " +
                            "Current status: " + booking.getStatus(),
                    HttpStatus.CONFLICT,
                    "BOOKING_NOT_CONFIRMED"
            );
        }
    }

    private void validateNoExistingTickets(Long bookingId) {
        List<Ticket> existing = ticketRepository.findAllByBookingId(bookingId);
        if (!existing.isEmpty()) {
            throw new AeroBookException(
                    "Tickets already issued for this booking",
                    HttpStatus.CONFLICT,
                    "TICKETS_ALREADY_ISSUED"
            );
        }
    }
}
