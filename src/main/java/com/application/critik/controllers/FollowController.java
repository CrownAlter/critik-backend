package com.application.critik.controllers;

import com.application.critik.dto.PagedResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.entities.User;
import com.application.critik.services.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for follow/unfollow operations.
 * 
 * Endpoints:
 * - POST /follow/{userId} - Follow a user (authenticated)
 * - DELETE /follow/{userId} - Unfollow a user (authenticated)
 * - GET /follow/{userId}/followers - Get user's followers
 * - GET /follow/{userId}/following - Get users that a user follows
 * 
 * Security:
 * - Follow/unfollow operations use the authenticated user as the follower
 * - Users cannot manipulate follow relationships for other users
 */
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * Follow a user.
     * The authenticated user becomes the follower.
     * 
     * SECURITY: Uses authenticated user instead of accepting followerId in URL.
     * 
     * @param userId    ID of the user to follow
     * @param principal Authenticated user
     */
    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, String>> followUser(
            @PathVariable Long userId,
            Principal principal) {
        followService.followUser(principal.getName(), userId);
        return ResponseEntity.ok(Map.of("message", "Followed successfully"));
    }

    /**
     * Unfollow a user.
     * The authenticated user unfollows the specified user.
     * 
     * SECURITY: Uses authenticated user instead of accepting followerId in URL.
     * 
     * @param userId    ID of the user to unfollow
     * @param principal Authenticated user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @PathVariable Long userId,
            Principal principal) {
        followService.unfollowUser(principal.getName(), userId);
        return ResponseEntity.ok(Map.of("message", "Unfollowed successfully"));
    }

    /**
     * Get a user's followers (paginated).
     * Accessible to all authenticated users.
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<PagedResponse<UserDto>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> followers = followService.getFollowers(userId, pageable);

        List<UserDto> content = followers.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        PagedResponse<UserDto> response = new PagedResponse<>(
                content, followers.getNumber(), followers.getSize(),
                followers.getTotalElements(), followers.getTotalPages(),
                followers.isLast(), followers.isFirst());

        return ResponseEntity.ok(response);
    }

    /**
     * Get users that a user follows (paginated).
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<PagedResponse<UserDto>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> following = followService.getFollowing(userId, pageable);

        List<UserDto> content = following.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        PagedResponse<UserDto> response = new PagedResponse<>(
                content, following.getNumber(), following.getSize(),
                following.getTotalElements(), following.getTotalPages(),
                following.isLast(), following.isFirst());

        return ResponseEntity.ok(response);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail()) // Typically email shouldn't be exposed publicly, but following DTO standards
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .bannerUrl(user.getBannerUrl())
                .build();
    }
}
