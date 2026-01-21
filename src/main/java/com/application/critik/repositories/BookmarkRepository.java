package com.application.critik.repositories;

import com.application.critik.entities.Bookmark;
import com.application.critik.entities.User;
import com.application.critik.entities.Artwork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * Find a bookmark by user and artwork
     */
    Optional<Bookmark> findByUserAndArtwork(User user, Artwork artwork);

    /**
     * Check if a user has bookmarked an artwork
     */
    boolean existsByUserAndArtwork(User user, Artwork artwork);

    /**
     * Check if a user has bookmarked an artwork (by IDs)
     */
    boolean existsByUserIdAndArtworkId(Long userId, Long artworkId);

    /**
     * Get all bookmarks for a user (paginated)
     */
    Page<Bookmark> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Delete a bookmark by user and artwork
     */
    void deleteByUserAndArtwork(User user, Artwork artwork);

    /**
     * Count total bookmarks for a user
     */
    long countByUser(User user);
}
