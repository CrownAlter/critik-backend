package com.application.critik.controllers;

import com.application.critik.dto.AuthRequest;
import com.application.critik.dto.AuthResponse;
import com.application.critik.dto.RefreshTokenRequest;
import com.application.critik.dto.TokenRefreshResponse;
import com.application.critik.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * 
 * Endpoints:
 * - POST /auth/register - Register new user
 * - POST /auth/login - Login and get access + refresh tokens
 * - POST /auth/refresh - Refresh access token using refresh token
 * - POST /auth/logout - Logout (revoke refresh token)
 * - POST /auth/logout-all - Logout from all devices (revoke all refresh tokens)
 * 
 * Security:
 * - Access tokens are short-lived (15 minutes)
 * - Refresh tokens are long-lived (7 days) and rotated on each use
 * - Rate limited to prevent brute force attacks
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user.
     * 
     * @param body Request body containing username, email, and password
     * @return Success message
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        authService.register(body.get("username"), body.get("email"), body.get("password"));
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /**
     * Login and obtain access and refresh tokens.
     * 
     * @param request Login credentials (username and password)
     * @return AuthResponse with access token, refresh token, and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        AuthResponse response = authService.loginWithRefreshToken(
                request.getUsername(), 
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh the access token using a valid refresh token.
     * 
     * The old refresh token is invalidated and a new one is issued (token rotation).
     * 
     * @param request Request containing the refresh token
     * @return New access token and refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout - revokes the provided refresh token.
     * 
     * @param request Request containing the refresh token to revoke
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Logout from all devices - revokes all refresh tokens for the user.
     * Requires authentication.
     * 
     * @param authentication Authenticated user
     * @return Success message
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll(Authentication authentication) {
        String username = authentication.getName();
        authService.logoutAll(username);
        return ResponseEntity.ok(Map.of("message", "Logged out from all devices successfully"));
    }
}
