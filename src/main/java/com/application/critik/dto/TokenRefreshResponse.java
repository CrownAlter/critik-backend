package com.application.critik.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for token refresh operations.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response containing new access token and refresh token after token refresh")
public class TokenRefreshResponse {

    /** New short-lived JWT access token */
    @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    /** New refresh token (token rotation) */
    @Schema(description = "New refresh token (old token is invalidated)", example = "550e8400-e29b-41d4-a716-446655440001")
    private String refreshToken;
    
    /** Token type (always "Bearer") */
    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";
    
    /** Access token expiration time in seconds */
    @Schema(description = "Access token expiration time in seconds", example = "900")
    private Long expiresIn;
}
