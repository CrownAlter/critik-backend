package com.application.critik.services;

import com.application.critik.dto.CommentReactionCountDto;
import com.application.critik.entities.Comment;
import com.application.critik.entities.CommentReaction;
import com.application.critik.entities.ReactionType;
import com.application.critik.entities.User;
import com.application.critik.exceptions.DuplicateResourceException;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.repositories.CommentReactionRepository;
import com.application.critik.repositories.CommentRepository;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing reactions to comments.
 */
@Service
@RequiredArgsConstructor
public class CommentReactionService {

    private final CommentReactionRepository commentReactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * Add or update a reaction to a comment.
     * 
     * @param commentId Comment ID
     * @param userId    User ID
     * @param type      Reaction type (AGREE or DISAGREE)
     * @throws ResourceNotFoundException if comment or user not found
     */
    @Transactional
    public void addOrUpdateReaction(Long commentId, Long userId, ReactionType type) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already reacted
        Optional<CommentReaction> existingReaction = commentReactionRepository.findByCommentAndUser(comment, user);

        if (existingReaction.isPresent()) {
            // Update existing reaction
            CommentReaction reaction = existingReaction.get();
            reaction.setType(type);
            commentReactionRepository.save(reaction);
        } else {
            // Create new reaction
            CommentReaction reaction = CommentReaction.builder()
                    .comment(comment)
                    .user(user)
                    .type(type)
                    .build();
            commentReactionRepository.save(reaction);
        }
    }

    /**
     * Remove a user's reaction from a comment.
     * 
     * @param commentId Comment ID
     * @param userId    User ID
     * @throws ResourceNotFoundException if comment or user not found
     */
    @Transactional
    public void removeReaction(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        commentReactionRepository.deleteByCommentAndUser(comment, user);
    }

    /**
     * Get reaction counts for a comment.
     * 
     * @param commentId Comment ID
     * @return Reaction counts (AGREE, DISAGREE, total)
     * @throws ResourceNotFoundException if comment not found
     */
    public CommentReactionCountDto getReactionCounts(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        long agreeCount = commentReactionRepository.countByCommentAndType(comment, ReactionType.AGREE);
        long disagreeCount = commentReactionRepository.countByCommentAndType(comment, ReactionType.DISAGREE);

        return CommentReactionCountDto.builder()
                .agree(agreeCount)
                .disagree(disagreeCount)
                .total(agreeCount + disagreeCount)
                .build();
    }

    /**
     * Get the current user's reaction to a comment.
     * 
     * @param commentId Comment ID
     * @param userId    User ID
     * @return Map with hasReaction and type fields
     */
    public Map<String, Object> getUserReaction(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        Map<String, Object> response = new HashMap<>();

        if (comment == null || user == null) {
            response.put("hasReaction", false);
            return response;
        }

        Optional<CommentReaction> reaction = commentReactionRepository.findByCommentAndUser(comment, user);

        if (reaction.isPresent()) {
            response.put("hasReaction", true);
            response.put("type", reaction.get().getType().toString());
        } else {
            response.put("hasReaction", false);
        }

        return response;
    }
}
