package com.application.critik.security;

import com.application.critik.config.RateLimitConfig;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter that applies different limits based on endpoint type.
 * 
 * This filter runs before authentication and applies rate limits based on:
 * - Client IP address (for unauthenticated requests)
 * - Username (for authenticated requests)
 * 
 * Rate limit categories:
 * - LOGIN: /auth/login - Strict limits to prevent brute force
 * - REGISTER: /auth/register - Very strict to prevent spam accounts
 * - SEARCH: /search/** - Moderate limits to prevent scraping
 * - API: All other endpoints - Standard limits
 * 
 * Response headers include rate limit information:
 * - X-Rate-Limit-Remaining: Tokens remaining
 * - X-Rate-Limit-Retry-After-Seconds: Seconds until tokens are available (when limited)
 * 
 * SECURITY: This filter helps protect against:
 * - Brute force attacks on login
 * - Spam account creation
 * - API abuse and scraping
 * - DoS attacks (basic protection)
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> bucketCache;
    private final RateLimitConfig rateLimitConfig;

    @Autowired
    public RateLimitFilter(Cache<String, Bucket> bucketCache, RateLimitConfig rateLimitConfig) {
        this.bucketCache = bucketCache;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip rate limiting for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for static resources, docs, and actuator health
        if (path.startsWith("/uploads/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.equals("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Determine the rate limit category and get/create bucket
        RateLimitCategory category = determineCategory(path, method);
        String bucketKey = createBucketKey(request, category);
        
        Bucket bucket = bucketCache.get(bucketKey, key -> createBucketForCategory(category));

        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // Add rate limit headers
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            // Request allowed
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitTimeSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again in %d seconds.\",\"retryAfterSeconds\":%d}",
                    waitTimeSeconds, waitTimeSeconds
            ));
        }
    }

    /**
     * Determines the rate limit category based on the request path.
     */
    private RateLimitCategory determineCategory(String path, String method) {
        if (path.equals("/auth/login") && "POST".equalsIgnoreCase(method)) {
            return RateLimitCategory.LOGIN;
        } else if (path.equals("/auth/register") && "POST".equalsIgnoreCase(method)) {
            return RateLimitCategory.REGISTER;
        } else if (path.startsWith("/search")) {
            return RateLimitCategory.SEARCH;
        } else {
            return RateLimitCategory.API;
        }
    }

    /**
     * Creates a unique bucket key based on client identifier and category.
     * 
     * For auth endpoints: Uses IP address (since user isn't authenticated yet)
     * For API endpoints: Uses username if authenticated, otherwise IP
     */
    private String createBucketKey(HttpServletRequest request, RateLimitCategory category) {
        String identifier;
        
        if (category == RateLimitCategory.LOGIN || category == RateLimitCategory.REGISTER) {
            // For auth endpoints, always use IP (user isn't authenticated yet)
            identifier = getClientIP(request);
        } else {
            // For other endpoints, prefer username if authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                identifier = "user:" + auth.getName();
            } else {
                identifier = getClientIP(request);
            }
        }
        
        return category.name() + ":" + identifier;
    }

    /**
     * Gets the client's IP address, handling proxies and load balancers.
     * 
     * Checks X-Forwarded-For header first (set by proxies/load balancers),
     * then falls back to remote address.
     * 
     * SECURITY: In production, ensure your proxy/load balancer is configured
     * to set X-Forwarded-For correctly and strip any client-provided values.
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP.trim();
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Creates a new bucket with limits appropriate for the category.
     */
    private Bucket createBucketForCategory(RateLimitCategory category) {
        return switch (category) {
            case LOGIN -> rateLimitConfig.createLoginBucket();
            case REGISTER -> rateLimitConfig.createRegisterBucket();
            case SEARCH -> rateLimitConfig.createSearchBucket();
            case API -> rateLimitConfig.createApiBucket();
        };
    }

    /**
     * Rate limit categories with different limits.
     */
    private enum RateLimitCategory {
        LOGIN,      // Strict: 5/min - prevent brute force
        REGISTER,   // Very strict: 3/min - prevent spam accounts
        SEARCH,     // Moderate: 30/min - prevent scraping
        API         // Standard: 60/min - normal usage
    }
}
