package com.application.critik.controllers;

import com.application.critik.entities.ReactionType;
import com.application.critik.services.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for reaction operations on artworks.
 * 
 * Endpoints:
 * - GET /artworks/{artworkId}/reactions - Get reaction counts
 * - GET /artworks/{artworkId}/reactions/me - Get current user's reaction (authenticated)
 * - POST /artworks/{artworkId}/reactions - Set/update reaction (authenticated)
 * - DELETE /artworks/{artworkId}/reactions - Remove reaction (authenticated)
 * 
 * Reactions allow users to AGREE or DISAGREE with an artwork/interpretation.
 */
@RestController
@RequestMapping("/artworks/{artworkId}/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    /**
     * Set or update reaction to an artwork.
     * If the user already has a reaction, it will be replaced.
     * Requires authentication.
     * 
     * @param artworkId ID of the artwork
     * @param type Reaction type (AGREE or DISAGREE)
     * @param principal Authenticated user
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> setReaction(
            @PathVariable Long artworkId,
            @RequestParam ReactionType type,
            Principal principal) {
        reactionService.setReaction(principal.getName(), artworkId, type);
        return ResponseEntity.ok(Map.of("message", "Reaction saved", "type", type.name()));
    }

    /**
     * Remove reaction from an artwork.
     * Requires authentication.
     * 
     * @param artworkId ID of the artwork
     * @param principal Authenticated user
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeReaction(
            @PathVariable Long artworkId,
            Principal principal) {
        boolean removed = reactionService.removeReaction(principal.getName(), artworkId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Reaction removed"));
        } else {
            return ResponseEntity.ok(Map.of("message", "No reaction to remove"));
        }
    }

    /**
     * Get the current user's reaction to an artwork.
     * Requires authentication.
     * 
     * @param artworkId ID of the artwork
     * @param principal Authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserReaction(
            @PathVariable Long artworkId,
            Principal principal) {
        ReactionType type = reactionService.getUserReaction(principal.getName(), artworkId);
        Map<String, Object> response = new HashMap<>();
        response.put("hasReaction", type != null);
        response.put("type", type);
        return ResponseEntity.ok(response);
    }

    /**
     * Get reaction counts for an artwork.
     * Accessible to all users.
     * 
     * @param artworkId ID of the artwork
     */
    @GetMapping
    public ResponseEntity<Map<ReactionType, Long>> getReactionCounts(@PathVariable Long artworkId) {
        return ResponseEntity.ok(reactionService.getReactionCounts(artworkId));
    }
}
