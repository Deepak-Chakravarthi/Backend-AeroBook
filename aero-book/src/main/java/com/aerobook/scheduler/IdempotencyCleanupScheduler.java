package com.aerobook.scheduler;


import com.aerobook.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyCleanupScheduler {

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    /**
     * Runs every hour — deletes expired idempotency records.
     */
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupExpiredRecords() {
        int deleted = idempotencyRecordRepository
                .deleteExpiredRecords(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired idempotency records", deleted);
        }
    }
}
