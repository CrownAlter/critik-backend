package com.application.critik.services;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.Reaction;
import com.application.critik.entities.ReactionType;
import com.application.critik.entities.User;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.ReactionRepository;
import com.application.critik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing reactions (AGREE/DISAGREE) on artworks.
 * 
 * Features:
 * - Add/change reaction to an artwork
 * - Remove reaction from an artwork
 * - Get reaction counts for an artwork
 * - Get user's reaction for an artwork
 * 
 * Each user can have only one reaction per artwork.
 * Setting a new reaction replaces the existing one.
 */
@Service
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Sets or updates a user's reaction to an artwork.
     * If the user already has a reaction, it will be updated.
     * 
     * @param username Username of the reacting user
     * @param artworkId ID of the artwork
     * @param type Reaction type (AGREE or DISAGREE)
     */
    public void setReaction(String username, Long artworkId, ReactionType type) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));

        Reaction existing = reactionRepository.findByArtworkIdAndUserId(artworkId, user.getId()).orElse(null);

        if (existing != null) {
            existing.setType(type);
            reactionRepository.save(existing);
        } else {
            Reaction reaction = Reaction.builder()
                    .user(user)
                    .artwork(artwork)
                    .type(type)
                    .createdAt(LocalDateTime.now())
                    .build();
            reactionRepository.save(reaction);
        }
    }

    /**
     * Removes a user's reaction from an artwork.
     * 
     * @param username Username of the user
     * @param artworkId ID of the artwork
     * @return true if a reaction was removed, false if no reaction existed
     */
    public boolean removeReaction(String username, Long artworkId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        // Verify artwork exists
        if (!artworkRepository.existsById(artworkId)) {
            throw new ResourceNotFoundException("Artwork", artworkId);
        }

        Optional<Reaction> existing = reactionRepository.findByArtworkIdAndUserId(artworkId, user.getId());
        
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            return true;
        }
        return false;
    }

    /**
     * Gets the current user's reaction to an artwork.
     * 
     * @param username Username of the user
     * @param artworkId ID of the artwork
     * @return The user's reaction type, or null if no reaction
     */
    public ReactionType getUserReaction(String username, Long artworkId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        return reactionRepository.findByArtworkIdAndUserId(artworkId, user.getId())
                .map(Reaction::getType)
                .orElse(null);
    }

    /**
     * Gets reaction counts for an artwork.
     * 
     * @param artworkId ID of the artwork
     * @return Map of reaction type to count
     */
    public Map<ReactionType, Long> getReactionCounts(Long artworkId) {
        // Verify artwork exists
        if (!artworkRepository.existsById(artworkId)) {
            throw new ResourceNotFoundException("Artwork", artworkId);
        }

        Map<ReactionType, Long> counts = new HashMap<>();
        counts.put(ReactionType.AGREE, reactionRepository.countByArtworkIdAndType(artworkId, ReactionType.AGREE));
        counts.put(ReactionType.DISAGREE, reactionRepository.countByArtworkIdAndType(artworkId, ReactionType.DISAGREE));
        return counts;
    }
}

