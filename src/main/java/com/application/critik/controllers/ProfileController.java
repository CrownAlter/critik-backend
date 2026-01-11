package com.application.critik.controllers;

import com.application.critik.dto.ProfileResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.dto.UserUpdateRequest;
import com.application.critik.services.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile operations.
 * 
 * Endpoints:
 * - GET /users/{username} - Get public user profile with artworks
 * - PUT /users/{id}/edit - Update user profile (owner only)
 * 
 * Security:
 * - Profile viewing is public (no authentication required)
 * - Profile editing requires authentication and ownership verification
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get a user's public profile.
     * Includes user information, their artworks, and follow status.
     * 
     * @param username Username of the user to retrieve
     * @return ProfileResponse containing user info, artworks, and follow status
     */
    @Operation(
            summary = "Get user profile",
            description = "Get a user's public profile including their artworks and follow status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getProfile(
            @Parameter(description = "Username of the user", required = true)
            @PathVariable String username) {
        return ResponseEntity.ok(profileService.getProfile(username));
    }

    /**
     * Update user profile information.
     * Only the profile owner can perform this action.
     * 
     * @param id User ID of the profile to update
     * @param updateRequest Request containing fields to update (displayName, email, bio)
     * @return Updated user information
     */
    @Operation(
            summary = "Update user profile",
            description = "Update profile information. Only the profile owner can perform this action.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the profile owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PutMapping("/{id}/edit")
    public ResponseEntity<UserDto> updateProfile(
            @Parameter(description = "User ID of the profile to update", required = true)
            @PathVariable Long id,
            @RequestBody UserUpdateRequest updateRequest) {
        return ResponseEntity.ok(profileService.updateProfile(id, updateRequest));
    }

}
