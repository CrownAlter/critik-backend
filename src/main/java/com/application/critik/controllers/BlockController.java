package com.application.critik.controllers;

import com.application.critik.dto.PagedResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.services.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for user blocking functionality.
 * Allows users to block/unblock other users and manage their block list.
 */
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class BlockController {

    private final UserBlockService userBlockService;

    /**
     * Block a user.
     * POST /api/blocks/{userId}
     */
    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, String>> blockUser(
            @PathVariable Long userId,
            Authentication authentication) {

        Long currentUserId = getUserIdFromAuth(authentication);
        userBlockService.blockUser(currentUserId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User blocked successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Unblock a user.
     * DELETE /api/blocks/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> unblockUser(
            @PathVariable Long userId,
            Authentication authentication) {

        Long currentUserId = getUserIdFromAuth(authentication);
        userBlockService.unblockUser(currentUserId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User unblocked successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Check if a user is blocked.
     * GET /api/blocks/{userId}/status
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<Map<String, Boolean>> checkBlockStatus(
            @PathVariable Long userId,
            Authentication authentication) {

        Long currentUserId = getUserIdFromAuth(authentication);
        boolean isBlocked = userBlockService.isBlocked(currentUserId, userId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isBlocked", isBlocked);
        return ResponseEntity.ok(response);
    }

    /**
     * Get list of blocked users (paginated).
     * GET /api/blocks?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<PagedResponse<UserDto>> getBlockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long currentUserId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<UserDto> blockedUsers = userBlockService.getBlockedUsers(currentUserId, pageable);

        return ResponseEntity.ok(blockedUsers);
    }

    /**
     * Helper method to extract user ID from authentication.
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }
}
