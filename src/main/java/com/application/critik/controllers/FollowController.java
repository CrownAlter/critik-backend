package com.application.critik.controllers;

import com.application.critik.entities.User;
import com.application.critik.services.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

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
     * @param userId ID of the user to follow
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
     * @param userId ID of the user to unfollow
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
     * Get a user's followers.
     * Accessible to all authenticated users.
     * 
     * @param userId ID of the user
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<User>> getFollowers(@PathVariable Long userId) {
        List<User> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Get users that a user follows.
     * Accessible to all authenticated users.
     * 
     * @param userId ID of the user
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<User>> getFollowing(@PathVariable Long userId) {
        List<User> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
}
