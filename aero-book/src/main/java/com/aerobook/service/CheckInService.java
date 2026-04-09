package com.aerobook.service;


import com.aerobook.domain.dto.request.get.CheckInGetRequest;
import com.aerobook.domain.dto.request.CheckInRequest;
import com.aerobook.domain.dto.response.CheckInResponse;
import com.aerobook.domain.enums.CheckInStatus;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.SeatStatus;
import com.aerobook.domain.enums.TicketStatus;
import com.aerobook.enitity.CheckIn;
import com.aerobook.enitity.Flight;
import com.aerobook.enitity.Seat;
import com.aerobook.enitity.Ticket;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.CheckInMapper;
import com.aerobook.repository.CheckInRepository;
import com.aerobook.repository.SeatRepository;
import com.aerobook.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final TicketRepository  ticketRepository;
    private final SeatRepository    seatRepository;
    private final CheckInMapper     checkInMapper;

    private static final int CHECK_IN_OPEN_HOURS  = 24;
    private static final int CHECK_IN_CLOSE_MINS  = 60;    // closes 60 min before departure

    // ----------------------------------------------------------------
    // Get check-ins — filterable
    // ----------------------------------------------------------------
    public List<CheckInResponse> getCheckIns(CheckInGetRequest request,
                                             Pageable pageable) {
        return checkInRepository.findAll(request.toSpecification(), pageable)
                .map(checkInMapper::toResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Get check-in by id
    // ----------------------------------------------------------------
    public CheckInResponse getCheckInById(Long id) {
        return checkInMapper.toResponse(findCheckInById(id));
    }

    // ----------------------------------------------------------------
    // Get check-in by ticket
    // ----------------------------------------------------------------
    public CheckInResponse getCheckInByTicket(Long ticketId) {
        CheckIn checkIn = checkInRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CheckIn", "ticketId", String.valueOf(ticketId)));
        return checkInMapper.toResponse(checkIn);
    }

    // ----------------------------------------------------------------
    // Perform check-in
    // ----------------------------------------------------------------
    @Transactional
    public CheckInResponse checkIn(CheckInRequest request) {
        Ticket ticket = ticketRepository.findByIdWithDetails(request.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", request.ticketId()));

        // Validate ticket is issued
        validateTicketForCheckIn(ticket);

        // Validate check-in window
        validateCheckInWindow(ticket.getFlight());

        // Validate no duplicate check-in
        if (checkInRepository.existsByTicketId(request.ticketId())) {
            throw new AeroBookException(
                    "Passenger already checked in for this ticket",
                    HttpStatus.CONFLICT,
                    "ALREADY_CHECKED_IN"
            );
        }

        // Validate and assign seat
        Seat seat = validateAndAssignSeat(
                ticket.getFlight().getId(),
                request.seatNumber(),
                ticket.getSeatClass()
        );

        // Determine boarding group from seat
        String boardingGroup = resolveBoardingGroup(
                ticket.getSeatClass(), seat.getRowNumber());

        // Create check-in record
        CheckIn checkIn = CheckIn.builder()
                .ticket(ticket)
                .booking(ticket.getBooking())
                .passenger(ticket.getPassenger())
                .flight(ticket.getFlight())
                .seatClass(ticket.getSeatClass())
                .status(CheckInStatus.NOT_CHECKED_IN)
                .createdAt(LocalDateTime.now())
                .build();

        checkIn.complete(request.seatNumber(), boardingGroup);

        // Mark seat as checked in
        seat.setStatus(SeatStatus.CHECKED_IN);
        seatRepository.save(seat);

        // Update ticket status
        ticket.markCheckedIn();
        ticketRepository.save(ticket);

        CheckIn saved = checkInRepository.save(checkIn);
        log.info("Check-in completed — ticket: {}, seat: {}, group: {}",
                ticket.getTicketNumber(), request.seatNumber(), boardingGroup);

        return checkInMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------
    public CheckIn findCheckInById(Long id) {
        return checkInRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckIn", id));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void validateTicketForCheckIn(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.ISSUED) {
            throw new AeroBookException(
                    "Ticket must be in ISSUED status for check-in. " +
                            "Current status: " + ticket.getStatus(),
                    HttpStatus.CONFLICT,
                    "TICKET_NOT_VALID_FOR_CHECKIN"
            );
        }
    }

    private void validateCheckInWindow(Flight flight) {
        LocalDateTime now           = LocalDateTime.now();
        LocalDateTime departureTime = flight.getDepartureTime();
        LocalDateTime checkInOpens  = departureTime.minusHours(CHECK_IN_OPEN_HOURS);
        LocalDateTime checkInCloses = departureTime.minusMinutes(CHECK_IN_CLOSE_MINS);

        if (now.isBefore(checkInOpens)) {
            throw new AeroBookException(
                    "Check-in not open yet. Opens at: " + checkInOpens
                            + " (24 hours before departure)",
                    HttpStatus.CONFLICT,
                    "CHECKIN_NOT_OPEN"
            );
        }

        if (now.isAfter(checkInCloses)) {
            throw new AeroBookException(
                    "Check-in closed. Closes 60 minutes before departure. " +
                            "Departure: " + departureTime,
                    HttpStatus.CONFLICT,
                    "CHECKIN_CLOSED"
            );
        }
    }

    private Seat validateAndAssignSeat(Long flightId,
                                       String seatNumber,
                                       SeatClass seatClass) {
        Seat seat = seatRepository.findByFlightIdAndSeatNumber(
                        flightId, seatNumber)
                .orElseThrow(() -> new AeroBookException(
                        "Seat " + seatNumber + " not found on this flight",
                        HttpStatus.NOT_FOUND,
                        "SEAT_NOT_FOUND"
                ));

        // Validate seat class matches ticket
        if (seat.getSeatClass() != seatClass) {
            throw new AeroBookException(
                    "Seat " + seatNumber + " is " + seat.getSeatClass()
                            + " class but ticket is for " + seatClass,
                    HttpStatus.CONFLICT,
                    "SEAT_CLASS_MISMATCH"
            );
        }

        // Validate seat is available or was previously held by this booking
        if (seat.getStatus() != SeatStatus.AVAILABLE
                && seat.getStatus() != SeatStatus.BOOKED) {
            throw new AeroBookException(
                    "Seat " + seatNumber + " is not available. Status: "
                            + seat.getStatus(),
                    HttpStatus.CONFLICT,
                    "SEAT_NOT_AVAILABLE"
            );
        }

        return seat;
    }

    private String resolveBoardingGroup(SeatClass seatClass, int rowNumber) {
        return switch (seatClass) {
            case FIRST    -> "A";
            case BUSINESS -> "B";
            case ECONOMY  -> rowNumber <= 15 ? "C" : "D";
        };
    }
}