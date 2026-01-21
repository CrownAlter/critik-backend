package com.application.critik.controllers;

import com.application.critik.dto.CommentReactionCountDto;
import com.application.critik.entities.ReactionType;
import com.application.critik.services.CommentReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for comment reactions.
 * Allows users to react to comments with AGREE/DISAGREE.
 */
@RestController
@RequestMapping("/api/comments/{commentId}/reactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class CommentReactionController {

    private final CommentReactionService commentReactionService;

    /**
     * Add or update a reaction to a comment.
     * POST /api/comments/{commentId}/reactions
     * Body: { "type": "AGREE" or "DISAGREE" }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addReaction(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        String typeStr = request.get("type");

        if (typeStr == null) {
            throw new IllegalArgumentException("Reaction type is required");
        }

        ReactionType type = ReactionType.valueOf(typeStr.toUpperCase());
        commentReactionService.addOrUpdateReaction(commentId, userId, type);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reaction added successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a reaction from a comment.
     * DELETE /api/comments/{commentId}/reactions
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeReaction(
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        commentReactionService.removeReaction(commentId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reaction removed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get reaction counts for a comment.
     * GET /api/comments/{commentId}/reactions/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<CommentReactionCountDto> getReactionCounts(
            @PathVariable Long commentId) {

        CommentReactionCountDto counts = commentReactionService.getReactionCounts(commentId);
        return ResponseEntity.ok(counts);
    }

    /**
     * Get current user's reaction to a comment.
     * GET /api/comments/{commentId}/reactions/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserReaction(
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> reaction = commentReactionService.getUserReaction(commentId, userId);
        return ResponseEntity.ok(reaction);
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
