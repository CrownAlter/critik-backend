package com.application.critik.dto;

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
public class TokenRefreshResponse {

    /** New short-lived JWT access token */
    private String accessToken;
    
    /** New refresh token (token rotation) */
    private String refreshToken;
    
    /** Token type (always "Bearer") */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /** Access token expiration time in seconds */
    private Long expiresIn;
}
