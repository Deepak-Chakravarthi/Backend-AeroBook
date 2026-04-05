package com.aerobook.service;


import com.aerobook.domain.dto.response.FlightSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightSearchCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper                  objectMapper;

    // Cache TTL — 5 minutes for search results
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    // ----------------------------------------------------------------
    // Get from cache
    // ----------------------------------------------------------------
    public Optional<FlightSearchResponse> get(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                log.debug("Cache MISS — key: {}", cacheKey);
                return Optional.empty();
            }
            log.debug("Cache HIT — key: {}", cacheKey);
            FlightSearchResponse response = objectMapper.convertValue(
                    cached, FlightSearchResponse.class);
            return Optional.of(response);
        } catch (Exception e) {
            log.warn("Cache read failed for key: {} — {}", cacheKey, e.getMessage());
            return Optional.empty();
        }
    }

    // ----------------------------------------------------------------
    // Store in cache
    // ----------------------------------------------------------------
    public void put(String cacheKey, FlightSearchResponse response) {
        try {
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
            log.debug("Cache SET — key: {}, TTL: {}min", cacheKey, CACHE_TTL.toMinutes());
        } catch (Exception e) {
            log.warn("Cache write failed for key: {} — {}", cacheKey, e.getMessage());
            // Non-critical — search still works without cache
        }
    }

    // ----------------------------------------------------------------
    // Evict — called when flight data changes
    // ----------------------------------------------------------------
    public void evict(String cacheKey) {
        redisTemplate.delete(cacheKey);
        log.debug("Cache EVICT — key: {}", cacheKey);
    }

    public void evictByPattern(String pattern) {
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cache EVICT — pattern: {}, keys evicted: {}", pattern, keys.size());
        }
    }
}