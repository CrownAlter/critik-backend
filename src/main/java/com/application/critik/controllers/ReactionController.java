package com.application.critik.controllers;

import com.application.critik.entities.ReactionType;
import com.application.critik.services.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reactions", description = "Artwork reaction operations (AGREE/DISAGREE)")
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
    @Operation(
            summary = "Set or update reaction",
            description = "Set or update your reaction to an artwork (AGREE or DISAGREE). Replaces existing reaction if present.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reaction saved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Map<String, String>> setReaction(
            @Parameter(description = "ID of the artwork", required = true)
            @PathVariable Long artworkId,
            @Parameter(description = "Reaction type (AGREE or DISAGREE)", required = true)
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
    @Operation(
            summary = "Remove reaction",
            description = "Remove your reaction from an artwork",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reaction removed or no reaction to remove"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeReaction(
            @Parameter(description = "ID of the artwork", required = true)
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
    @Operation(
            summary = "Get my reaction",
            description = "Get your reaction to an artwork",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user reaction"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserReaction(
            @Parameter(description = "ID of the artwork", required = true)
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
    @Operation(
            summary = "Get reaction counts",
            description = "Get the count of AGREE and DISAGREE reactions for an artwork"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reaction counts"),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Map<ReactionType, Long>> getReactionCounts(
            @Parameter(description = "ID of the artwork", required = true)
            @PathVariable Long artworkId) {
        return ResponseEntity.ok(reactionService.getReactionCounts(artworkId));
    }
}
