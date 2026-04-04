package com.aerobook.scheduler;

import com.aerobook.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SeatHoldExpiryScheduler {

    private final SeatService seatService;

    /**
     * Runs every minute — releases any seats whose hold TTL has expired.
     * Acts as a safety net in case Redis TTL fires but DB was not updated.
     */
    @Scheduled(fixedDelay = 60000)
    public void releaseExpiredHolds() {
        log.debug("SeatHoldExpiryScheduler — checking for expired holds");
        seatService.releaseExpiredHolds();
    }
}
