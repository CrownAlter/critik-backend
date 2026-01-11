package com.application.critik.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Authentication response containing access token, refresh token, and user info")
public class AuthResponse {

    /** Short-lived JWT access token for API authentication */
    @Schema(description = "JWT access token (15 minutes validity)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    /** Long-lived refresh token for obtaining new access tokens */
    @Schema(description = "Refresh token for obtaining new access tokens (7 days validity)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
    
    /** Token type (always "Bearer") */
    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";
    
    /** Access token expiration time in seconds */
    @Schema(description = "Access token expiration time in seconds", example = "900")
    private Long expiresIn;
    
    /** User ID */
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    /** Username */
    @Schema(description = "Username", example = "johndoe")
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
