package com.application.critik.services;

import com.application.critik.dto.ProfileResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.dto.UserUpdateRequest;
import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.exceptions.DuplicateResourceException;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.exceptions.UnauthorizedException;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.FollowRepository;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Profile service for viewing and managing user profiles.
 * 
 * Security features:
 * - Users can only edit their own profiles
 * - Email changes are validated for format and uniqueness
 * - Profile viewing is available to all users (authenticated get additional info)
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final FollowRepository followRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Gets a user's public profile.
     * 
     * @param username Username of the profile to view
     * @return ProfileResponse containing user info, artworks, and follow status
     * @throws ResourceNotFoundException if user not found
     */
    public ProfileResponse getProfile(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        List<Artwork> artworks = artworkRepository.findByUserId(user.getId());

        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isFollowing = false;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String currentUsername = auth.getName();
            User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                    .orElse(null);

            if (currentUser != null) {
                isFollowing = followRepository.existsByFollowerIdAndFollowedId(
                        currentUser.getId(), user.getId()
                );
            }
        }

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .bio(user.getBio())
                .build();

        return new ProfileResponse(userDto, artworks, isFollowing);
    }

    /**
     * Updates a user's profile.
     * 
     * SECURITY: Only the profile owner can update their profile.
     * 
     * @param id User ID to update
     * @param updateRequest Fields to update
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user not found
     * @throws UnauthorizedException if current user doesn't own the profile
     * @throws DuplicateResourceException if new email already exists
     * @throws IllegalArgumentException if email format is invalid
     */
    public UserDto updateProfile(Long id, UserUpdateRequest updateRequest) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("You must be logged in to update a profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // SECURITY: Verify the authenticated user owns this profile
        String currentUsername = auth.getName();
        if (!user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new UnauthorizedException("You can only edit your own profile");
        }

        // Update display name if provided
        if (updateRequest.getDisplayName() != null) {
            String displayName = updateRequest.getDisplayName().trim();
            if (displayName.length() > 100) {
                throw new IllegalArgumentException("Display name cannot exceed 100 characters");
            }
            user.setDisplayName(displayName.isEmpty() ? null : displayName);
        }

        // Update email if provided with validation
        if (updateRequest.getEmail() != null) {
            String newEmail = updateRequest.getEmail().trim().toLowerCase();
            if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
            // Check if email is already taken by another user
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User", "email", newEmail);
            }
            user.setEmail(newEmail);
        }

        // Update bio if provided
        if (updateRequest.getBio() != null) {
            String bio = updateRequest.getBio().trim();
            if (bio.length() > 500) {
                throw new IllegalArgumentException("Bio cannot exceed 500 characters");
            }
            user.setBio(bio.isEmpty() ? null : bio);
        }

        userRepository.save(user);

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .bio(user.getBio())
                .build();
    }

}
