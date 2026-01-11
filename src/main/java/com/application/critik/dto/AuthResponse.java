package com.application.critik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Authentication response containing access and refresh tokens.
 * 
 * Token types:
 * - accessToken: Short-lived JWT (15 minutes) for API authentication
 * - refreshToken: Long-lived token (7 days) for obtaining new access tokens
 * 
 * Security:
 * - Access tokens should be stored in memory only (not localStorage)
 * - Refresh tokens can be stored in httpOnly cookies or secure storage
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    /** Short-lived JWT access token for API authentication */
    private String accessToken;
    
    /** Long-lived refresh token for obtaining new access tokens */
    private String refreshToken;
    
    /** Token type (always "Bearer") */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /** Access token expiration time in seconds */
    private Long expiresIn;
    
    /** User ID */
    private Long userId;
    
    /** Username */
    private String username;

    /**
     * Legacy constructor for backward compatibility.
     * @deprecated Use builder pattern instead
     */
    @Deprecated
    public AuthResponse(String token, Long userId, String username) {
        this.accessToken = token;
        this.userId = userId;
        this.username = username;
        this.tokenType = "Bearer";
    }
}
