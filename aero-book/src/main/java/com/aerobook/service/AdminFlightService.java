package com.aerobook.service;


import com.aerobook.entity.Booking;
import com.aerobook.entity.Flight;
import com.aerobook.domain.dto.request.BulkFlightCancelRequest;
import com.aerobook.domain.dto.request.BulkFlightStatusRequest;
import com.aerobook.domain.dto.response.BulkOperationResponse;
import com.aerobook.domain.dto.response.BulkOperationResponse.FailedOperation;
import com.aerobook.domain.enums.BookingStatus;
import com.aerobook.domain.enums.CancellationReason;
import com.aerobook.domain.enums.FlightStatus;
import com.aerobook.event.FlightStatusChangedEvent;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.repository.BookingRepository;
import com.aerobook.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFlightService {

    private final FlightRepository       flightRepository;
    private final BookingRepository      bookingRepository;
    private final BookingService         bookingService;
    private final ApplicationEventPublisher eventPublisher;

    // ----------------------------------------------------------------
    // Bulk status update — delay multiple flights at once
    // ----------------------------------------------------------------
    @Transactional
    public BulkOperationResponse bulkUpdateStatus(BulkFlightStatusRequest request) {
        List<Long>             successIds = new ArrayList<>();
        List<FailedOperation>  failures   = new ArrayList<>();

        for (Long flightId : request.flightIds()) {
            try {
                Flight flight = flightRepository.findById(flightId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Flight", flightId));

                validateStatusTransition(flight, request.status());

                FlightStatus previous = flight.getStatus();
                flight.setStatus(request.status());
                flight.setDelayMinutes(request.delayMinutes());
                flightRepository.save(flight);

                // Fetch affected bookings for notification fan-out
                List<Booking> affected = bookingRepository
                        .findAllByOutboundFlightIdAndStatus(
                                flightId, BookingStatus.CONFIRMED);

                // Publish status changed event — notifications fire async
                eventPublisher.publishEvent(new FlightStatusChangedEvent(
                        this, flight, previous, request.status(),
                        request.reason(), affected));

                successIds.add(flightId);
                log.info("Bulk status update — flight: {}, status: {}",
                        flightId, request.status());

            } catch (Exception e) {
                failures.add(new FailedOperation(flightId, e.getMessage()));
                log.warn("Bulk status update failed — flight: {}, error: {}",
                        flightId, e.getMessage());
            }
        }

        return new BulkOperationResponse(
                request.flightIds().size(),
                successIds.size(),
                failures.size(),
                successIds,
                failures,
                LocalDateTime.now()
        );
    }

    // ----------------------------------------------------------------
    // Bulk cancel — cancel multiple flights + cascade bookings
    // ----------------------------------------------------------------
    @Transactional
    public BulkOperationResponse bulkCancel(BulkFlightCancelRequest request) {
        List<Long>             successIds = new ArrayList<>();
        List<FailedOperation>  failures   = new ArrayList<>();

        for (Long flightId : request.flightIds()) {
            try {
                Flight flight = flightRepository.findById(flightId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Flight", flightId));

                if (flight.getStatus() == FlightStatus.CANCELLED) {
                    failures.add(new FailedOperation(
                            flightId, "Flight already cancelled"));
                    continue;
                }

                FlightStatus previous = flight.getStatus();
                flight.setStatus(FlightStatus.CANCELLED);
                flightRepository.save(flight);

                // Cancel all confirmed bookings on this flight
                List<Booking> affectedBookings = bookingRepository
                        .findAllByOutboundFlightIdAndStatus(
                                flightId, BookingStatus.CONFIRMED);

                affectedBookings.forEach(booking -> {
                    try {
                        bookingService.cancelBooking(
                                booking.getId(),
                                new com.aerobook.domain.dto.request.BookingCancelRequest(
                                        CancellationReason.FLIGHT_CANCELLED,
                                        request.reason()
                                )
                        );
                    } catch (Exception e) {
                        log.warn("Failed to cancel booking {} for flight {} — {}",
                                booking.getPnr(), flightId, e.getMessage());
                    }
                });

                // Publish event — notifications sent to all affected passengers
                eventPublisher.publishEvent(new FlightStatusChangedEvent(
                        this, flight, previous,
                        FlightStatus.CANCELLED,
                        request.reason(),
                        affectedBookings));

                successIds.add(flightId);
                log.info("Bulk cancel — flight: {}, bookings cancelled: {}",
                        flightId, affectedBookings.size());

            } catch (Exception e) {
                failures.add(new FailedOperation(flightId, e.getMessage()));
                log.warn("Bulk cancel failed — flight: {}, error: {}",
                        flightId, e.getMessage());
            }
        }

        return new BulkOperationResponse(
                request.flightIds().size(),
                successIds.size(),
                failures.size(),
                successIds,
                failures,
                LocalDateTime.now()
        );
    }

    // ----------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------
    private void validateStatusTransition(Flight flight, FlightStatus newStatus) {
        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new com.aerobook.exception.AeroBookException(
                    "Cannot update status of a cancelled flight",
                    org.springframework.http.HttpStatus.CONFLICT,
                    "FLIGHT_ALREADY_CANCELLED"
            );
        }
        if (flight.getStatus() == FlightStatus.LANDED) {
            throw new com.aerobook.exception.AeroBookException(
                    "Cannot update status of a landed flight",
                    org.springframework.http.HttpStatus.CONFLICT,
                    "FLIGHT_ALREADY_LANDED"
            );
        }
    }
}