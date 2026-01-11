package com.application.critik.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT utility class for token generation and validation.
 * 
 * Security features:
 * - Short-lived access tokens (configurable, default 15 minutes)
 * - HS256 signing algorithm with secure key
 * - Token expiration validation
 * 
 * PRODUCTION NOTES:
 * - JWT_SECRET must be at least 256 bits (32 characters) for HS256
 * - Consider RS256 for better security in distributed systems
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:900000}") // 15 minutes default for access tokens
    private long expiration;

    /**
     * Generates a JWT access token for the given user.
     * 
     * @param userDetails User details
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     * 
     * @param token JWT token
     * @return Username from token subject
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token against user details.
     * 
     * @param token JWT token
     * @param userDetails User details to validate against
     * @return true if token is valid
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) &&
                !isTokenExpired(token);
    }

    /**
     * Checks if a token has expired.
     * 
     * @param token JWT token
     * @return true if expired
     */
    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    /**
     * Gets the token expiration time in milliseconds.
     * 
     * @return Expiration time in milliseconds
     */
    public long getExpirationMs() {
        return expiration;
    }

    /**
     * Gets the token expiration time in seconds.
     * 
     * @return Expiration time in seconds
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }
}
