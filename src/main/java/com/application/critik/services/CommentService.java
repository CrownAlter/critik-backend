package com.application.critik.services;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.Comment;
import com.application.critik.entities.User;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.exceptions.UnauthorizedException;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.CommentRepository;
import com.application.critik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing comments on artworks.
 * 
 * Features:
 * - Add top-level comments to artworks
 * - Reply to existing comments (nested replies supported)
 * - Delete comments (owner only)
 * - Get comments with their reply trees
 * 
 * Security:
 * - Only authenticated users can comment
 * - Only comment owners can delete their comments
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Adds a top-level comment to an artwork.
     * 
     * @param username Username of the commenter
     * @param artworkId ID of the artwork
     * @param text Comment text
     * @return Created comment
     */
    public Comment addComment(String username, Long artworkId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text is required");
        }
        if (text.length() > 2000) {
            throw new IllegalArgumentException("Comment cannot exceed 2000 characters");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));

        Comment comment = Comment.builder()
                .user(user)
                .artwork(artwork)
                .commentText(text.trim())
                .parentComment(null) // Top-level comment
                .build();

        return commentRepository.save(comment);
    }

    /**
     * Adds a reply to an existing comment.
     * Supports nested replies (reply to a reply).
     * 
     * @param username Username of the commenter
     * @param artworkId ID of the artwork
     * @param parentCommentId ID of the comment being replied to
     * @param text Reply text
     * @return Created reply comment
     */
    public Comment addReply(String username, Long artworkId, Long parentCommentId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply text is required");
        }
        if (text.length() > 2000) {
            throw new IllegalArgumentException("Reply cannot exceed 2000 characters");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", parentCommentId));

        // Verify the parent comment belongs to the same artwork
        if (!parentComment.getArtwork().getId().equals(artworkId)) {
            throw new IllegalArgumentException("Parent comment does not belong to this artwork");
        }

        Comment reply = Comment.builder()
                .user(user)
                .artwork(artwork)
                .commentText(text.trim())
                .parentComment(parentComment)
                .build();

        return commentRepository.save(reply);
    }

    /**
     * Deletes a comment.
     * 
     * SECURITY: Only the comment owner can delete it.
     * Note: Deleting a comment also deletes all its replies (cascade).
     * 
     * @param commentId ID of the comment to delete
     */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // SECURITY: Verify ownership
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("You must be logged in to delete a comment");
        }

        String currentUsername = auth.getName();
        if (!comment.getUser().getUsername().equalsIgnoreCase(currentUsername)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    /**
     * Gets all top-level comments for an artwork with their replies.
     * Returns a tree structure where each comment includes its nested replies.
     * 
     * @param artworkId ID of the artwork
     * @return List of top-level comments (each with nested replies)
     */
    public List<Comment> getComments(Long artworkId) {
        // Verify artwork exists
        if (!artworkRepository.existsById(artworkId)) {
            throw new ResourceNotFoundException("Artwork", artworkId);
        }
        // Get only top-level comments (replies are loaded via entity relationship)
        return commentRepository.findByArtworkIdAndParentCommentIsNullOrderByCreatedAtDesc(artworkId);
    }

    /**
     * Gets all comments for an artwork (flat list, including replies).
     * Useful for getting total comment count.
     * 
     * @param artworkId ID of the artwork
     * @return List of all comments
     */
    public List<Comment> getAllComments(Long artworkId) {
        return commentRepository.findByArtworkIdOrderByCreatedAtDesc(artworkId);
    }
}
