package com.application.critik.repositories;

import com.application.critik.entities.CommentReaction;
import com.application.critik.entities.Comment;
import com.application.critik.entities.User;
import com.application.critik.entities.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    /**
     * Find a user's reaction to a comment
     */
    Optional<CommentReaction> findByCommentAndUser(Comment comment, User user);

    /**
     * Check if a user has reacted to a comment
     */
    boolean existsByCommentAndUser(Comment comment, User user);

    /**
     * Delete a user's reaction to a comment
     */
    void deleteByCommentAndUser(Comment comment, User user);

    /**
     * Count reactions by type for a comment
     */
    long countByCommentAndType(Comment comment, ReactionType type);

    /**
     * Get reaction counts for a comment
     */
    @Query("SELECT cr.type, COUNT(cr) FROM CommentReaction cr WHERE cr.comment = :comment GROUP BY cr.type")
    Map<ReactionType, Long> countReactionsByComment(@Param("comment") Comment comment);

    /**
     * Count total reactions for a comment
     */
    long countByComment(Comment comment);
}
