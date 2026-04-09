package com.aerobook.service;


import com.aerobook.domain.dto.request.*;
import com.aerobook.domain.dto.request.get.BookingGetRequest;
import com.aerobook.domain.dto.response.BookingResponse;
import com.aerobook.domain.dto.response.BookingSummaryResponse;
import com.aerobook.domain.enums.*;
import com.aerobook.entity.Booking;
import com.aerobook.entity.Flight;
import com.aerobook.entity.User;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.BookingMapper;
import com.aerobook.repository.BookingRepository;
import com.aerobook.security.UserPrincipal;
import com.aerobook.util.PNRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper     bookingMapper;
    private final PNRGenerator      pnrGenerator;
    private final FlightService     flightService;
    private final UserService       userService;
    private final SeatService       seatService;
    private final SeatInventoryService seatInventoryService;

    // ----------------------------------------------------------------
    // Get bookings — admin sees all, passenger sees own only
    // ----------------------------------------------------------------
    public List<BookingSummaryResponse> getBookings(BookingGetRequest request,
                                                    Pageable pageable) {
        return bookingRepository.findAll(request.toSpecification(), pageable)
                .map(bookingMapper::toSummaryResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Get own bookings
    // ----------------------------------------------------------------
    public List<BookingSummaryResponse> getMyBookings() {
        Long userId = resolveCurrentUserId();
        return bookingRepository.findActiveBookingsByUser(userId)
                .stream()
                .map(bookingMapper::toSummaryResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Get booking by id
    // ----------------------------------------------------------------
    public BookingResponse getBookingById(Long id) {
        Booking booking = findBookingById(id);
        validateBookingAccess(booking);
        return bookingMapper.toResponse(booking);
    }

    // ----------------------------------------------------------------
    // Get booking by PNR
    // ----------------------------------------------------------------
    public BookingResponse getBookingByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnrWithDetails(pnr)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking", "PNR", pnr));
        validateBookingAccess(booking);
        return bookingMapper.toResponse(booking);
    }

    // ----------------------------------------------------------------
    // Step 1 — Create booking (PENDING)
    // ----------------------------------------------------------------
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        request.validate();

        User user = userService.findUserById(resolveCurrentUserId());

        Flight outboundFlight = flightService.findFlightById(
                request.outboundFlightId());
        Flight returnFlight   = request.returnFlightId() != null
                ? flightService.findFlightById(request.returnFlightId())
                : null;

        // Validate flights are not cancelled
        validateFlightBookable(outboundFlight);
        if (returnFlight != null) validateFlightBookable(returnFlight);

        // Calculate fare
        BigDecimal[] outboundFare = resolveFare(
                outboundFlight, request.outboundSeatClass());
        BigDecimal[] returnFare   = returnFlight != null
                ? resolveFare(returnFlight, request.returnSeatClass())
                : new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};

        BigDecimal totalBase  = outboundFare[0].add(returnFare[0]);
        BigDecimal totalTax   = outboundFare[1].add(returnFare[1]);
        BigDecimal totalFare  = totalBase.add(totalTax);

        // Generate unique PNR
        String pnr = generateUniquePnr();

        Booking booking = Booking.builder()
                .pnr(pnr)
                .bookingType(request.bookingType())
                .status(BookingStatus.PENDING)
                .user(user)
                .outboundFlight(outboundFlight)
                .outboundSeatClass(request.outboundSeatClass())
                .returnFlight(returnFlight)
                .returnSeatClass(request.returnSeatClass())
                .passengerFirstName(request.passengerFirstName())
                .passengerLastName(request.passengerLastName())
                .passengerEmail(request.passengerEmail())
                .passengerPhone(request.passengerPhone())
                .passengerDob(request.passengerDob())
                .passportNumber(request.passportNumber())
                .nationality(request.nationality())
                .baseFare(totalBase)
                .tax(totalTax)
                .totalFare(totalFare)
                .createdAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created — PNR: {}, user: {}", pnr, user.getEmail());
        return bookingMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Step 2 — Lock seats (PENDING → SEAT_LOCKED)
    // ----------------------------------------------------------------
    @Transactional
    public BookingResponse lockSeats(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateBookingAccess(booking);

        // Hold outbound seat
        SeatHoldRequest outboundHoldRequest = SeatHoldRequest.builder()
                .flightId(booking.getOutboundFlight().getId())
                .seatClass(booking.getOutboundSeatClass())
                .seatCount(1)
                .build();

        var outboundHold = seatService.holdSeats(outboundHoldRequest);
        booking.setOutboundSeatNumber(outboundHold.seatNumbers().get(0));

        // Hold return seat if RETURN booking
        String returnHoldRef = null;
        if (booking.getReturnFlight() != null) {
            SeatHoldRequest returnHoldRequest = SeatHoldRequest.builder()
                    .flightId(booking.getReturnFlight().getId())
                    .seatClass(booking.getReturnSeatClass())
                    .seatCount(1)
                    .build();
            var returnHold = seatService.holdSeats(returnHoldRequest);
            booking.setReturnSeatNumber(returnHold.seatNumbers().get(0));
            returnHoldRef = returnHold.bookingRef();
        }

        booking.lockSeats(
                outboundHold.bookingRef(),
                returnHoldRef,
                outboundHold.heldUntil()
        );

        Booking saved = bookingRepository.save(booking);
        log.info("Seats locked — PNR: {}, holdRef: {}, until: {}",
                booking.getPnr(), outboundHold.bookingRef(),
                outboundHold.heldUntil());

        return bookingMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Step 3 — Initiate payment (SEAT_LOCKED → PAYMENT_INITIATED)
    // ----------------------------------------------------------------
    @Transactional
    public BookingResponse initiatePayment(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateBookingAccess(booking);

        if (booking.isSeatHoldExpired()) {
            booking.expire();
            bookingRepository.save(booking);
            throw new AeroBookException(
                    "Seat hold has expired for booking: " + booking.getPnr()
                            + ". Please create a new booking.",
                    HttpStatus.CONFLICT,
                    "SEAT_HOLD_EXPIRED"
            );
        }

        booking.initiatePayment();
        Booking saved = bookingRepository.save(booking);
        log.info("Payment initiated — PNR: {}", booking.getPnr());
        return bookingMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Step 4 — Confirm booking (PAYMENT_INITIATED → CONFIRMED)
    // Called by PaymentService after successful payment
    // ----------------------------------------------------------------
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);

        // Confirm physical seats
        seatService.confirmSeats(
                booking.getOutboundFlight().getId(),
                booking.getOutboundSeatHoldRef()
        );

        if (booking.getReturnFlight() != null
                && booking.getReturnSeatHoldRef() != null) {
            seatService.confirmSeats(
                    booking.getReturnFlight().getId(),
                    booking.getReturnSeatHoldRef()
            );
        }

        booking.confirm(
                booking.getOutboundSeatNumber(),
                booking.getReturnSeatNumber()
        );

        Booking saved = bookingRepository.save(booking);
        log.info("Booking confirmed — PNR: {}", booking.getPnr());
        return bookingMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Cancel booking
    // ----------------------------------------------------------------
    @Transactional
    public BookingResponse cancelBooking(Long bookingId,
                                         BookingCancelRequest request) {
        Booking booking = findBookingById(bookingId);
        validateBookingAccess(booking);

        // Release held seats if still held
        releaseBookingSeats(booking);

        booking.cancel(request.reason(), request.remarks());
        Booking saved = bookingRepository.save(booking);
        log.info("Booking cancelled — PNR: {}, reason: {}",
                booking.getPnr(), request.reason());

        return bookingMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Expire bookings — called by scheduler
    // ----------------------------------------------------------------
    @Transactional
    public void expireStaleBookings() {
        List<Booking> expired = bookingRepository.findExpiredBookings(
                LocalDateTime.now());

        expired.forEach(booking -> {
            releaseBookingSeats(booking);
            booking.expire();
            bookingRepository.save(booking);
            log.info("Booking expired — PNR: {}", booking.getPnr());
        });

        if (!expired.isEmpty()) {
            log.info("Expired {} stale bookings", expired.size());
        }
    }

    // ----------------------------------------------------------------
    // Internal — used by PaymentService
    // ----------------------------------------------------------------
    public Booking findBookingById(Long id) {
        return bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void validateFlightBookable(Flight flight) {
        if (flight.getStatus() == FlightStatus.CANCELLED
                || flight.getStatus() == FlightStatus.DIVERTED) {
            throw new AeroBookException(
                    "Flight " + flight.getFlightNumber()
                            + " is not available for booking. Status: " + flight.getStatus(),
                    HttpStatus.CONFLICT,
                    "FLIGHT_NOT_BOOKABLE"
            );
        }
    }

    private BigDecimal[] resolveFare(Flight flight, SeatClass seatClass) {
        return flight.getFares().stream()
                .filter(f -> f.getSeatClass() == seatClass)
                .findFirst()
                .map(f -> new BigDecimal[]{f.getBaseFare(), f.getTax()})
                .orElseThrow(() -> new AeroBookException(
                        "No fare found for flight " + flight.getFlightNumber()
                                + " in class " + seatClass,
                        HttpStatus.BAD_REQUEST,
                        "FARE_NOT_FOUND"
                ));
    }

    private String generateUniquePnr() {
        String pnr;
        do {
            pnr = pnrGenerator.generate();
        } while (bookingRepository.existsByPnr(pnr));
        return pnr;
    }

    private void releaseBookingSeats(Booking booking) {
        if (booking.getOutboundSeatHoldRef() != null) {
            try {
                seatService.releaseSeats(new SeatReleaseRequest(
                        booking.getOutboundFlight().getId(),
                        booking.getOutboundSeatHoldRef()
                ));
            } catch (Exception e) {
                log.warn("Failed to release outbound seats for PNR: {}",
                        booking.getPnr());
            }
        }
        if (booking.getReturnFlight() != null
                && booking.getReturnSeatHoldRef() != null) {
            try {
                seatService.releaseSeats(new SeatReleaseRequest(
                        booking.getReturnFlight().getId(),
                        booking.getReturnSeatHoldRef()
                ));
            } catch (Exception e) {
                log.warn("Failed to release return seats for PNR: {}",
                        booking.getPnr());
            }
        }
    }

    private void validateBookingAccess(Booking booking) {
        Long currentUserId = resolveCurrentUserId();
        boolean isAdmin    = isAdminOrAgent();

        if (!isAdmin && !booking.getUser().getId().equals(currentUserId)) {
            throw new AeroBookException(
                    "Access denied — you can only access your own bookings",
                    HttpStatus.FORBIDDEN,
                    "BOOKING_ACCESS_DENIED"
            );
        }
    }

    private Long resolveCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }

    private boolean isAdminOrAgent() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_AGENT")
                        || a.getAuthority().equals("ROLE_AIRLINE_ADMIN"));
    }
}
