package com.aerobook.scheduler;


import com.aerobook.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    /**
     * Runs every minute — expires stale bookings
     * whose seat hold TTL has passed.
     */
    @Scheduled(fixedDelay = 60000)
    public void expireStaleBookings() {
        log.debug("BookingExpiryScheduler — checking for expired bookings");
        bookingService.expireStaleBookings();
    }
}
