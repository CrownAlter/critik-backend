package com.application.critik.repositories;

import com.application.critik.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Comment entity database operations.
 * Supports hierarchical comments with parent-child relationships (replies).
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Find all comments for an artwork, ordered by creation date (newest first).
     * Includes both top-level comments and replies.
     * 
     * @param artworkId ID of the artwork
     * @return List of all comments for the artwork
     */
    List<Comment> findByArtworkIdOrderByCreatedAtDesc(Long artworkId);
    
    /**
     * Find only top-level comments (no parent) for an artwork.
     * This is used to build comment trees with replies.
     * 
     * @param artworkId ID of the artwork
     * @return List of top-level comments (excluding replies)
     */
    List<Comment> findByArtworkIdAndParentCommentIsNullOrderByCreatedAtDesc(Long artworkId);
    
    /**
     * Find replies to a specific comment.
     * Ordered by creation date (oldest first) to show conversation flow.
     * 
     * @param parentCommentId ID of the parent comment
     * @return List of replies to the comment
     */
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    /**
     * Delete all comments for an artwork (cascade delete when artwork is removed).
     * Includes both top-level comments and all nested replies.
     * 
     * @param artworkId ID of the artwork
     */
    @Modifying(clearAutomatically = true)
    void deleteByArtworkId(Long artworkId);
}
