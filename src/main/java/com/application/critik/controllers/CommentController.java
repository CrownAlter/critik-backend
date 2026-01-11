package com.application.critik.controllers;

import com.application.critik.entities.Comment;
import com.application.critik.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for comment operations on artworks.
 * 
 * Endpoints:
 * - GET /artworks/{artworkId}/comments - Get all comments (tree structure)
 * - POST /artworks/{artworkId}/comments - Add top-level comment (authenticated)
 * - POST /artworks/{artworkId}/comments/{commentId}/replies - Reply to comment (authenticated)
 * - DELETE /artworks/{artworkId}/comments/{commentId} - Delete comment (owner only)
 * 
 * Comments support nested replies - users can reply to comments and to other replies.
 */
@RestController
@RequestMapping("/artworks/{artworkId}/comments")
@Tag(name = "Comments", description = "Comment and reply management on artworks")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * Add a top-level comment to an artwork.
     * Requires authentication.
     * 
     * @param artworkId ID of the artwork
     * @param body Request body containing "text" field
     * @param principal Authenticated user
     */
    @Operation(
            summary = "Add a comment",
            description = "Add a top-level comment to an artwork",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @Parameter(description = "ID of the artwork", required = true)
            @PathVariable Long artworkId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Comment text",
                    content = @Content(schema = @Schema(example = "{\"text\": \"Great artwork!\"}"))
            )
            @RequestBody Map<String, String> body,
            Principal principal) {
        Comment comment = commentService.addComment(principal.getName(), artworkId, body.get("text"));
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Reply to an existing comment.
     * Supports nested replies (reply to a reply).
     * Requires authentication.
     * 
     * @param artworkId ID of the artwork
     * @param commentId ID of the comment being replied to
     * @param body Request body containing "text" field
     * @param principal Authenticated user
     */
    @Operation(
            summary = "Reply to a comment",
            description = "Add a reply to an existing comment. Supports nested replies.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reply added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment or artwork not found", content = @Content)
    })
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<Comment> addReply(
            @Parameter(description = "ID of the artwork", required = true)
            @PathVariable Long artworkId,
            @Parameter(description = "ID of the comment to reply to", required = true)
            @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reply text",
                    content = @Content(schema = @Schema(example = "{\"text\": \"I agree!\"}"))
            )
            @RequestBody Map<String, String> body,
            Principal principal) {
        Comment reply = commentService.addReply(principal.getName(), artworkId, commentId, body.get("text"));
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    /**
     * Get all comments for an artwork.
     * Returns a tree structure where each top-level comment includes its nested replies.
     * Accessible to all users.
     * 
     * @param artworkId ID of the artwork
     */
    @Operation(
            summary = "Get all comments",
            description = "Get all comments for an artwork in a tree structure with nested replies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(
            @Parameter(description = "ID of the artwork", required = true)
            @PathVariable Long artworkId) {
        return ResponseEntity.ok(commentService.getComments(artworkId));
    }

    /**
     * Delete a comment.
     * Only the comment owner can delete their comment.
     * Deleting a comment also deletes all its replies.
     * 
     * @param artworkId ID of the artwork (for URL consistency)
     * @param commentId ID of the comment to delete
     */
    @Operation(
            summary = "Delete a comment",
            description = "Delete a comment and all its replies. Only the comment owner can perform this action.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the comment owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @Parameter(description = "ID of the artwork", required = true)
            @PathVariable Long artworkId,
            @Parameter(description = "ID of the comment to delete", required = true)
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }
}
