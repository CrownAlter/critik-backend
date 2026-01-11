package com.application.critik.services;

import com.application.critik.entities.RefreshToken;
import com.application.critik.entities.User;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.repositories.RefreshTokenRepository;
import com.application.critik.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens.
 * 
 * Security features:
 * - Token rotation: each refresh invalidates old token and creates new one
 * - Automatic cleanup of expired/revoked tokens
 * - Ability to revoke all tokens for a user (logout everywhere)
 * - Configurable expiration time
 * 
 * PRODUCTION NOTES:
 * - Consider Redis for token storage in distributed environments
 * - Monitor for unusual token activity (many active tokens per user)
 * - Log token refresh events for security audit
 */
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new refresh token for a user.
     * 
     * @param username Username of the user
     * @return Created refresh token
     */
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Finds a refresh token by its token string.
     * 
     * @param token Token string
     * @return Optional containing the token if found
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findValidByToken(token);
    }

    /**
     * Verifies and returns a refresh token.
     * 
     * @param token Token string to verify
     * @return The refresh token if valid
     * @throws RuntimeException if token is invalid, expired, or revoked
     */
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }

        return refreshToken;
    }

    /**
     * Rotates a refresh token - invalidates the old one and creates a new one.
     * This is a security best practice to limit the window of token theft.
     * 
     * @param oldToken The old refresh token to rotate
     * @return New refresh token
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke the old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Create a new token for the same user
        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(newToken);
    }

    /**
     * Revokes a specific refresh token.
     * 
     * @param token Token string to revoke
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.revokeByToken(token);
    }

    /**
     * Revokes all refresh tokens for a user.
     * Use this for "logout everywhere" or when user changes password.
     * 
     * @param username Username of the user
     */
    @Transactional
    public void revokeAllUserTokens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    /**
     * Scheduled cleanup of expired and revoked tokens.
     * Runs every hour by default.
     */
    @Scheduled(fixedRateString = "${jwt.refresh-cleanup-interval:3600000}") // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpired(Instant.now());
        refreshTokenRepository.deleteAllRevoked();
    }

    /**
     * Gets the refresh token duration in milliseconds.
     */
    public long getRefreshTokenDurationMs() {
        return refreshTokenDurationMs;
    }
}
