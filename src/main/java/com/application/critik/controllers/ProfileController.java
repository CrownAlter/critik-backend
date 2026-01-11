package com.application.critik.controllers;

import com.application.critik.dto.ProfileResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.dto.UserUpdateRequest;
import com.application.critik.services.ProfileService;
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
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get a user's public profile.
     * Includes user information, their artworks, and follow status.
     * 
     * @param username Username of the user to retrieve
     * @return ProfileResponse containing user info, artworks, and follow status
     */
    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String username) {
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
    @PutMapping("/{id}/edit")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest updateRequest) {
        return ResponseEntity.ok(profileService.updateProfile(id, updateRequest));
    }

}
