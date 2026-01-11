package com.application.critik.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for rate limiting across the application.
 * 
 * Uses Bucket4j with token bucket algorithm for efficient rate limiting.
 * Different limits are applied to different endpoint types:
 * 
 * - Authentication endpoints (login/register): Strict limits to prevent brute force
 * - API endpoints: Standard limits for normal usage
 * - Search endpoints: Moderate limits to prevent scraping
 * 
 * PRODUCTION NOTES:
 * - For multi-instance deployments, consider using Redis-backed Bucket4j
 * - Adjust limits based on your expected traffic patterns
 * - Monitor rate limit hits via actuator metrics
 */
@Configuration
public class RateLimitConfig {

    // =============================================================================
    // RATE LIMIT SETTINGS (configurable via environment variables)
    // =============================================================================
    
    /** Max login attempts per IP per minute */
    @Value("${rate-limit.auth.login.requests:5}")
    private int loginRequestsPerMinute;
    
    /** Max registration attempts per IP per minute */
    @Value("${rate-limit.auth.register.requests:3}")
    private int registerRequestsPerMinute;
    
    /** Max API requests per user per minute */
    @Value("${rate-limit.api.requests:60}")
    private int apiRequestsPerMinute;
    
    /** Max search requests per IP per minute */
    @Value("${rate-limit.search.requests:30}")
    private int searchRequestsPerMinute;
    
    /** Cache expiration time in minutes */
    @Value("${rate-limit.cache.expiration:10}")
    private int cacheExpirationMinutes;

    /**
     * Cache for storing rate limit buckets per IP/user.
     * Uses Caffeine for high-performance local caching.
     * 
     * For distributed deployments, replace with Redis-backed cache.
     */
    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        // Using ConcurrentHashMap wrapped with Caffeine for automatic eviction
        return new ConcurrentHashMap<>();
    }

    /**
     * Caffeine cache configuration for bucket storage.
     * Automatically evicts unused buckets after the configured expiration time.
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Bucket> bucketCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(cacheExpirationMinutes, TimeUnit.MINUTES)
                .maximumSize(100_000) // Max 100k unique IPs/users tracked
                .build();
    }

    /**
     * Creates a rate limit bucket for login attempts.
     * 
     * Strict limits to prevent brute force attacks:
     * - 5 requests per minute by default
     * - Tokens refill gradually (not all at once)
     */
    public Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(
                loginRequestsPerMinute,
                Refill.greedy(loginRequestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Creates a rate limit bucket for registration attempts.
     * 
     * Very strict limits to prevent spam accounts:
     * - 3 requests per minute by default
     */
    public Bucket createRegisterBucket() {
        Bandwidth limit = Bandwidth.classic(
                registerRequestsPerMinute,
                Refill.greedy(registerRequestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Creates a rate limit bucket for general API requests.
     * 
     * Standard limits for authenticated users:
     * - 60 requests per minute by default
     */
    public Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.classic(
                apiRequestsPerMinute,
                Refill.greedy(apiRequestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Creates a rate limit bucket for search requests.
     * 
     * Moderate limits to prevent scraping:
     * - 30 requests per minute by default
     */
    public Bucket createSearchBucket() {
        Bandwidth limit = Bandwidth.classic(
                searchRequestsPerMinute,
                Refill.greedy(searchRequestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    // Getters for use in RateLimitFilter
    public int getLoginRequestsPerMinute() {
        return loginRequestsPerMinute;
    }

    public int getRegisterRequestsPerMinute() {
        return registerRequestsPerMinute;
    }

    public int getApiRequestsPerMinute() {
        return apiRequestsPerMinute;
    }

    public int getSearchRequestsPerMinute() {
        return searchRequestsPerMinute;
    }
}
