package com.aerobook.service;

import com.aerobook.entity.Booking;
import com.aerobook.entity.Passenger;
import com.aerobook.entity.User;
import com.aerobook.domain.dto.request.get.PassengerGetRequest;
import com.aerobook.domain.dto.request.PassengerRequest;
import com.aerobook.domain.dto.response.PassengerResponse;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.PassengerMapper;
import com.aerobook.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper     passengerMapper;
    private final BookingService      bookingService;
    private final UserService         userService;

    // ----------------------------------------------------------------
    // Get passengers — filterable
    // ----------------------------------------------------------------
    public List<PassengerResponse> getPassengers(PassengerGetRequest request,
                                                 Pageable pageable) {
        return passengerRepository.findAll(request.toSpecification(), pageable)
                .map(passengerMapper::toResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Get passenger by id
    // ----------------------------------------------------------------
    public PassengerResponse getPassengerById(Long id) {
        Passenger passenger = passengerRepository.findByIdWithTickets(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", id));
        return passengerMapper.toResponse(passenger);
    }

    // ----------------------------------------------------------------
    // Get passengers by booking
    // ----------------------------------------------------------------
    public List<PassengerResponse> getPassengersByBooking(Long bookingId) {
        return passengerRepository.findAllByBookingIdWithTickets(bookingId)
                .stream()
                .map(passengerMapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Add passenger to booking
    // ----------------------------------------------------------------
    @Transactional
    public PassengerResponse addPassenger(PassengerRequest request) {
        Booking booking = bookingService.findBookingById(request.bookingId());

        validateBookingAcceptsPassengers(booking);

        User linkedUser = request.userId() != null
                ? userService.findUserById(request.userId())
                : null;

        Passenger passenger = passengerMapper.toEntity(request);
        passenger.setBooking(booking);
        passenger.setUser(linkedUser);

        Passenger saved = passengerRepository.save(passenger);
        log.info("Passenger added — booking: {}, passenger: {} {}",
                booking.getPnr(), saved.getFirstName(), saved.getLastName());

        return passengerMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // Update passenger
    // ----------------------------------------------------------------
    @Transactional
    public PassengerResponse updatePassenger(Long id, PassengerRequest request) {
        Passenger passenger = findPassengerById(id);
        validateBookingAcceptsPassengers(passenger.getBooking());

        passenger.setFirstName(request.firstName());
        passenger.setLastName(request.lastName());
        passenger.setGender(request.gender());
        passenger.setDateOfBirth(request.dateOfBirth());
        passenger.setPassportNumber(request.passportNumber());
        passenger.setPassportExpiry(request.passportExpiry());
        passenger.setNationality(request.nationality());
        passenger.setEmail(request.email());
        passenger.setPhone(request.phone());
        passenger.setPassengerType(request.passengerType());

        return passengerMapper.toResponse(passengerRepository.save(passenger));
    }

    // ----------------------------------------------------------------
    // Delete passenger
    // ----------------------------------------------------------------
    @Transactional
    public void deletePassenger(Long id) {
        Passenger passenger = findPassengerById(id);
        validateBookingAcceptsPassengers(passenger.getBooking());
        passengerRepository.deleteById(id);
        log.info("Passenger deleted — id: {}", id);
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------
    public Passenger findPassengerById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", id));
    }

    // ----------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------
    private void validateBookingAcceptsPassengers(Booking booking) {
        if (!booking.isActive()) {
            throw new AeroBookException(
                    "Cannot modify passengers for booking in status: "
                            + booking.getStatus(),
                    HttpStatus.CONFLICT,
                    "BOOKING_NOT_ACTIVE"
            );
        }
    }
}
