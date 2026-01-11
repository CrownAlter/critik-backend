package com.application.critik.repositories;

import com.application.critik.entities.Reaction;
import com.application.critik.entities.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Reaction entity database operations.
 * Handles user reactions (AGREE/DISAGREE) to artworks.
 * Each user can have only one reaction per artwork.
 */
@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    /**
     * Find a user's reaction to a specific artwork.
     * Used to check if user has already reacted and what type.
     * 
     * @param artworkId ID of the artwork
     * @param userId ID of the user
     * @return Optional containing the reaction if exists
     */
    Optional<Reaction> findByArtworkIdAndUserId(Long artworkId, Long userId);
    
    /**
     * Count reactions of a specific type for an artwork.
     * Used to display reaction counts (e.g., "5 Agree, 2 Disagree").
     * 
     * @param artworkId ID of the artwork
     * @param type Type of reaction (AGREE or DISAGREE)
     * @return Count of reactions of the specified type
     */
    long countByArtworkIdAndType(Long artworkId, ReactionType type);
    
    /**
     * Find all reactions for an artwork.
     * 
     * @param artworkId ID of the artwork
     * @return List of all reactions to the artwork
     */
    List<Reaction> findByArtworkId(Long artworkId);
    
    /**
     * Delete all reactions for an artwork (cascade delete when artwork is removed).
     * 
     * @param artworkId ID of the artwork
     */
    @Modifying(clearAutomatically = true)
    void deleteByArtworkId(Long artworkId);
}

