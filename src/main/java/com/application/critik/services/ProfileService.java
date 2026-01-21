package com.application.critik.services;

import com.application.critik.dto.ProfileResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.dto.UserStatsDto;
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
 * - Profile viewing is available to all users (authenticated get additional
 * info)
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final FollowRepository followRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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
                        currentUser.getId(), user.getId());
            }
        }

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .bannerUrl(user.getBannerUrl())
                .build();

        // Calculate user stats
        UserStatsDto stats = calculateUserStats(user);

        return ProfileResponse.builder()
                .user(userDto)
                .artworks(artworks)
                .isFollowing(isFollowing)
                .stats(stats)
                .build();
    }

    /**
     * Calculate user statistics.
     */
    private UserStatsDto calculateUserStats(User user) {
        long followerCount = followRepository.countByFollowed(user);
        long followingCount = followRepository.countByFollower(user);
        long artworkCount = artworkRepository.countByUser(user);
        long totalReactions = artworkRepository.countTotalReactionsByUser(user);

        return UserStatsDto.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .artworkCount(artworkCount)
                .totalReactions(totalReactions)
                .build();
    }

    /**
     * Updates a user's profile.
     * 
     * SECURITY: Only the profile owner can update their profile.
     * 
     * @param id            User ID to update
     * @param updateRequest Fields to update
     * @return Updated user DTO
     * @throws ResourceNotFoundException  if user not found
     * @throws UnauthorizedException      if current user doesn't own the profile
     * @throws DuplicateResourceException if new email already exists
     * @throws IllegalArgumentException   if email format is invalid
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
                .avatarUrl(user.getAvatarUrl())
                .bannerUrl(user.getBannerUrl())
                .build();
    }

    /**
     * Update user avatar URL.
     */
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verify ownership
        verifyOwnership(user);

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    /**
     * Update user banner URL.
     */
    public void updateBanner(Long userId, String bannerUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verify ownership
        verifyOwnership(user);

        user.setBannerUrl(bannerUrl);
        userRepository.save(user);
    }

    /**
     * Verify that the current authenticated user owns the profile.
     */
    private void verifyOwnership(User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("You must be logged in to perform this action");
        }

        String currentUsername = auth.getName();
        if (!user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new UnauthorizedException("You can only modify your own profile");
        }
    }
}
