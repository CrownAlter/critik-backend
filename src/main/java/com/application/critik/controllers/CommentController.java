package com.application.critik.controllers;

import com.application.critik.entities.Comment;
import com.application.critik.services.CommentService;
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
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Long artworkId,
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
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<Comment> addReply(
            @PathVariable Long artworkId,
            @PathVariable Long commentId,
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
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long artworkId) {
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
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long artworkId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }
}
