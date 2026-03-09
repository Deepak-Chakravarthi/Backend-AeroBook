package com.aerobook.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * The type Cache configuration.
 */
@Configuration
public class CacheConfiguration {

    private static final Integer MAX_SIZE= 100000;

    /**
     * Cache manager cache manager.
     *
     * @return the cache manager
     */
    @Bean
    public CacheManager cacheManager() {

        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(MAX_SIZE)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
        );

        return manager;
    }
}
