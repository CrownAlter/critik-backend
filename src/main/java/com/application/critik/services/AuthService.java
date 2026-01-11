package com.application.critik.services;

import com.application.critik.dto.AuthResponse;
import com.application.critik.dto.TokenRefreshResponse;
import com.application.critik.entities.RefreshToken;
import com.application.critik.entities.User;
import com.application.critik.exceptions.DuplicateResourceException;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.repositories.UserRepository;
import com.application.critik.security.JwtUtil;
import com.application.critik.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Authentication service handling user registration, login, and token management.
 * 
 * Security features:
 * - Password strength validation (min 8 chars, mixed case, numbers, special chars)
 * - Email format validation
 * - Username normalization (lowercase, trimmed)
 * - BCrypt password hashing
 * - Short-lived JWT access tokens (15 minutes)
 * - Long-lived refresh tokens (7 days) with rotation
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    // Password must be at least 8 characters with uppercase, lowercase, digit, and special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    // Basic email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Registers a new user with validation.
     * 
     * @param username User's desired username (will be normalized to lowercase)
     * @param email User's email address
     * @param password User's password (must meet strength requirements)
     * @throws DuplicateResourceException if username or email already exists
     * @throws IllegalArgumentException if validation fails
     */
    public void register(String username, String email, String password) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Normalize username
        String normalized = username.trim().toLowerCase();

        // Validate username length
        if (normalized.length() < 3 || normalized.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password strength
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character (@#$%^&+=!)"
            );
        }

        // Check for duplicates
        if (userRepository.existsByUsername(normalized)) {
            throw new DuplicateResourceException("User", "username", normalized);
        }
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new DuplicateResourceException("User", "email", email);
        }

        User user = User.builder()
                .username(normalized)
                .email(email.trim().toLowerCase())
                .password(passwordEncoder.encode(password))
                .build();
        userRepository.save(user);
    }

    /**
     * Authenticates a user and returns a JWT token (legacy method).
     * 
     * @param username User's username
     * @param password User's password
     * @return JWT token for authenticated session
     * @throws BadCredentialsException if credentials are invalid
     * @deprecated Use loginWithRefreshToken instead
     */
    @Deprecated
    public String login(String username, String password) {
        UserDetails userDetails = validateCredentials(username, password);
        return jwtUtil.generateToken(userDetails);
    }

    /**
     * Authenticates a user and returns both access and refresh tokens.
     * 
     * @param username User's username
     * @param password User's password
     * @return AuthResponse with access token, refresh token, and user info
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse loginWithRefreshToken(String username, String password) {
        UserDetails userDetails = validateCredentials(username, password);
        User user = findUserByUsername(username);
        
        // Generate access token
        String accessToken = jwtUtil.generateToken(userDetails);
        
        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * Implements token rotation - old refresh token is invalidated and new one is issued.
     * 
     * @param refreshTokenString The refresh token string
     * @return TokenRefreshResponse with new access and refresh tokens
     * @throws RuntimeException if refresh token is invalid or expired
     */
    public TokenRefreshResponse refreshAccessToken(String refreshTokenString) {
        // Verify and get the refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenString);
        
        // Get user details for generating new access token
        UserDetails userDetails = userDetailsService.loadUserByUsername(
                refreshToken.getUser().getUsername()
        );
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(userDetails);
        
        // Rotate refresh token (invalidate old, create new)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationSeconds())
                .build();
    }

    /**
     * Logs out by revoking the provided refresh token.
     * 
     * @param refreshToken The refresh token to revoke
     */
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    /**
     * Logs out from all devices by revoking all refresh tokens for the user.
     * 
     * @param username Username of the user
     */
    public void logoutAll(String username) {
        refreshTokenService.revokeAllUserTokens(username);
    }

    /**
     * Validates user credentials.
     * 
     * @param username User's username
     * @param password User's password
     * @return UserDetails if valid
     * @throws BadCredentialsException if credentials are invalid
     */
    private UserDetails validateCredentials(String username, String password) {
        // SECURITY: Use generic error message to prevent username enumeration
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username.trim().toLowerCase());
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        
        return userDetails;
    }

    /**
     * Finds a user by username.
     * 
     * @param username Username to search for
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}
