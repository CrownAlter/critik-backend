package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Refresh Token entity for secure JWT refresh flow.
 * 
 * Security features:
 * - Long-lived tokens (7 days by default) stored securely in database
 * - Can be revoked individually or all tokens for a user
 * - One-time use: each refresh generates a new token (rotation)
 * - Tracks creation time for audit purposes
 * 
 * Flow:
 * 1. User logs in -> receives short-lived access token + refresh token
 * 2. Access token expires -> client uses refresh token to get new access token
 * 3. Refresh token is rotated (old one invalidated, new one issued)
 * 4. If refresh token is expired or revoked, user must log in again
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    /**
     * Checks if the token has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    /**
     * Checks if the token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }
}
