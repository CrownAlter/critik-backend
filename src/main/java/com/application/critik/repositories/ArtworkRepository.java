package com.application.critik.repositories;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for Artwork entity database operations.
 * Provides methods for artwork retrieval, search, and personalized feeds.
 */
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    /**
     * Find all artworks by a specific user.
     * @param user User entity
     * @return List of artworks created by the user
     */
    List<Artwork> findByUser(User user);

    /**
     * Get personalized feed for a user showing artworks from followed users.
     * Returns artworks from users that the given user follows, ordered by creation date.
     * 
     * @param userId ID of the user requesting the feed
     * @return List of artworks from followed users, newest first
     */
    @Query("SELECT a FROM Artwork a WHERE a.user.id IN " +
            "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId) " +
            "ORDER BY a.createdAt DESC")
    List<Artwork> findFeedForUser(Long userId);

    /**
     * Get top 10 most recent artworks (fallback for users with no follows).
     * @return List of 10 most recent artworks
     */
    @Query("SELECT a FROM Artwork a ORDER BY a.createdAt DESC")
    List<Artwork> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find artworks by user ID.
     * @param id User ID
     * @return List of artworks created by the user
     */
    List<Artwork> findByUserId(Long id);

    /**
     * Find artworks by username.
     * @param username Username of the artwork creator
     * @return List of artworks created by the user
     */
    List<Artwork> findByUserUsername(String username);
    
    /**
     * Search artworks by title (case-insensitive partial match).
     * @param title Title substring to search for
     * @return List of matching artworks
     */
    List<Artwork> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Search artworks by location name (case-insensitive partial match).
     * @param location Location substring to search for
     * @return List of matching artworks
     */
    List<Artwork> findByLocationNameContainingIgnoreCase(String location);
    
    /**
     * Search artworks by tags (case-insensitive partial match).
     * @param tag Tag substring to search for
     * @return List of matching artworks
     */
    List<Artwork> findByTagsContainingIgnoreCase(String tag);
}
