package com.application.critik.controllers;

import com.application.critik.entities.User;
import com.application.critik.services.FollowService;
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
@Tag(name = "Follow", description = "User follow/unfollow operations")
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
    @Operation(
            summary = "Follow a user",
            description = "Follow another user. The authenticated user becomes the follower.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Followed successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot follow yourself or already following", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, String>> followUser(
            @Parameter(description = "ID of the user to follow", required = true)
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
    @Operation(
            summary = "Unfollow a user",
            description = "Unfollow a user that you are currently following",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unfollowed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or follow relationship not found", content = @Content)
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @Parameter(description = "ID of the user to unfollow", required = true)
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
    @Operation(
            summary = "Get user's followers",
            description = "Get a list of users who follow the specified user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved followers"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<User>> getFollowers(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId) {
        List<User> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Get users that a user follows.
     * Accessible to all authenticated users.
     * 
     * @param userId ID of the user
     */
    @Operation(
            summary = "Get users that user follows",
            description = "Get a list of users that the specified user follows"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved following list"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<User>> getFollowing(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId) {
        List<User> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
}
