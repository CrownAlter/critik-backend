package com.application.critik.config;

import com.application.critik.security.JwtRequestFilter;
import com.application.critik.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the Critik application.
 * 
 * Features:
 * - JWT-based stateless authentication
 * - BCrypt password encoding (strength 12)
 * - CORS configuration for frontend integration
 * - Rate limiting to prevent abuse
 * - Public endpoints for auth and public feed
 * 
 * Security Decisions:
 * - CSRF is disabled because we use stateless JWT authentication (no cookies
 * for auth)
 * - Sessions are stateless - each request must include JWT token
 * - Passwords are hashed with BCrypt (industry standard, adaptive hashing)
 * - Rate limiting applied before authentication to block abuse early
 * 
 * Filter Chain Order:
 * 1. RateLimitFilter - Blocks requests exceeding rate limits
 * 2. JwtRequestFilter - Validates JWT tokens and sets authentication
 * 3. Spring Security filters - Authorization checks
 * 
 * PRODUCTION NOTES:
 * - Update CORS allowedOrigins to match your production frontend URL
 * - Ensure JWT_SECRET is at least 256 bits (32+ characters) in production
 * - Tune rate limits based on your traffic patterns
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    /**
     * CORS allowed origins - configure via environment variable for different
     * environments.
     * Default is localhost for development.
     */
    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    /**
     * Main security filter chain configuration.
     * 
     * Public endpoints (no authentication required):
     * - /auth/** - Registration and login
     * - /uploads/** - Static file serving for uploaded images
     * - /artworks/feed - Public artwork feed
     * - GET /artworks/{id} - View single artwork
     * - GET /artworks/{id}/comments - View comments
     * - GET /artworks/{id}/reactions - View reaction counts
     * - /actuator/health - Health check endpoint
     * 
     * All other endpoints require authentication.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled - safe because we use stateless JWT auth (not cookie-based)
                .csrf(csrf -> csrf.disable())
                // Enable CORS with our configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // OpenAPI / Swagger UI
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/artworks/feed").permitAll()
                        .requestMatchers(HttpMethod.GET, "/artworks/feed/{userId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/artworks/{artworkId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/artworks/{artworkId}/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/artworks/{artworkId}/reactions").permitAll()
                        // Search endpoints - public for discoverability
                        .requestMatchers(HttpMethod.GET, "/search/**").permitAll()
                        // User profiles - public viewing
                        .requestMatchers(HttpMethod.GET, "/users/{username}").permitAll()
                        // Actuator health endpoint for load balancers/monitoring
                        .requestMatchers("/actuator/health").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                // Stateless session - no server-side session storage
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add rate limit filter first (before any authentication)
        // This ensures rate limiting blocks abuse before any processing
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        // Add JWT filter after rate limiting but before authorization
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration for cross-origin requests.
     * 
     * PRODUCTION: Update allowedOrigins to your production frontend URL.
     * Can be configured via CORS_ALLOWED_ORIGINS environment variable.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse comma-separated origins from config
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        // Expose rate limit headers to frontend
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-Rate-Limit-Remaining",
                "X-Rate-Limit-Retry-After-Seconds"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Password encoder using BCrypt.
     * 
     * BCrypt is recommended because:
     * - Adaptive: can increase work factor as hardware improves
     * - Salt is built-in: each hash includes unique salt
     * - Slow by design: resists brute force attacks
     * 
     * Strength 12 = 2^12 iterations (good balance of security and performance)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
