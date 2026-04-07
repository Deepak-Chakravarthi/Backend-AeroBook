package com.aerobook.service;


import com.aerobook.enitity.IdempotencyRecord;
import com.aerobook.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper                objectMapper;

    private static final int EXPIRY_HOURS = 24;

    // ----------------------------------------------------------------
    // Check if key already processed
    // ----------------------------------------------------------------
    public Optional<IdempotencyRecord> findExistingRecord(String key) {
        return idempotencyRecordRepository.findByIdempotencyKey(key);
    }

    // ----------------------------------------------------------------
    // Store processed response
    // ----------------------------------------------------------------
    @Transactional
    public void store(String key, Object response,
                      int httpStatus, String requestPath) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);

            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .responseBody(responseBody)
                    .httpStatus(httpStatus)
                    .requestPath(requestPath)
                    .expiresAt(LocalDateTime.now().plusHours(EXPIRY_HOURS))
                    .createdAt(LocalDateTime.now())
                    .build();

            idempotencyRecordRepository.save(record);
            log.debug("Idempotency record stored — key: {}", key);

        } catch (Exception e) {
            log.warn("Failed to store idempotency record for key: {} — {}",
                    key, e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Parse cached response
    // ----------------------------------------------------------------
    public <T> T parseResponse(IdempotencyRecord record, Class<T> clazz) {
        try {
            return objectMapper.readValue(record.getResponseBody(), clazz);
        } catch (Exception e) {
            log.warn("Failed to parse cached idempotency response: {}",
                    e.getMessage());
            return null;
        }
    }
}
