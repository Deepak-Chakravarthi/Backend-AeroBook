package com.aerobook.scheduler;


import com.aerobook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationService notificationService;

    /**
     * Runs every 5 minutes — retries failed notifications
     * up to MAX_RETRIES times before moving to DLQ.
     */
    @Scheduled(fixedDelay = 300000)
    public void retryFailedNotifications() {
        log.debug("NotificationRetryScheduler — retrying failed notifications");
        notificationService.retryFailedNotifications();
    }
}