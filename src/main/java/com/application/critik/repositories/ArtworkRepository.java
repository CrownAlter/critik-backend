package com.application.critik.repositories;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for Artwork entity database operations.
 * Provides methods for artwork retrieval, search, and personalized feeds.
 */
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    /**
     * Find all artworks by a specific user (paginated).
     * 
     * @param user     User entity
     * @param pageable Pagination parameters
     * @return Page of artworks created by the user
     */
    Page<Artwork> findByUser(User user, Pageable pageable);

    /**
     * Find all artworks by a specific user (non-paginated).
     * 
     * @param user User entity
     * @return List of artworks created by the user
     */
    List<Artwork> findByUser(User user);

    /**
     * Get personalized feed for a user showing artworks from followed users
     * (paginated).
     * Returns artworks from users that the given user follows, ordered by creation
     * date.
     * Excludes artworks from blocked users.
     * 
     * @param userId         ID of the user requesting the feed
     * @param blockedUserIds List of user IDs that are blocked
     * @param pageable       Pagination parameters
     * @return Page of artworks from followed users, newest first
     */
    @Query("SELECT a FROM Artwork a WHERE a.user.id IN " +
            "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId) " +
            "AND a.user.id NOT IN :blockedUserIds " +
            "ORDER BY a.createdAt DESC")
    Page<Artwork> findFeedForUser(@Param("userId") Long userId,
            @Param("blockedUserIds") List<Long> blockedUserIds,
            Pageable pageable);

    /**
     * Get public feed excluding blocked users (paginated).
     * 
     * @param blockedUserIds List of user IDs that are blocked
     * @param pageable       Pagination parameters
     * @return Page of artworks
     */
    @Query("SELECT a FROM Artwork a WHERE a.user.id NOT IN :blockedUserIds ORDER BY a.createdAt DESC")
    Page<Artwork> findPublicFeedExcludingBlocked(@Param("blockedUserIds") List<Long> blockedUserIds,
            Pageable pageable);

    /**
     * Get all artworks ordered by creation date (paginated).
     * 
     * @param pageable Pagination parameters
     * @return Page of artworks
     */
    Page<Artwork> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find artworks by user ID (paginated).
     * 
     * @param id       User ID
     * @param pageable Pagination parameters
     * @return Page of artworks created by the user
     */
    Page<Artwork> findByUserId(Long id, Pageable pageable);

    /**
     * Find artworks by user ID (non-paginated).
     * 
     * @param id User ID
     * @return List of artworks created by the user
     */
    List<Artwork> findByUserId(Long id);

    /**
     * Find artworks by username (paginated).
     * 
     * @param username Username of the artwork creator
     * @param pageable Pagination parameters
     * @return Page of artworks created by the user
     */
    Page<Artwork> findByUserUsername(String username, Pageable pageable);

    /**
     * Find artworks by username (non-paginated).
     * 
     * @param username Username of the artwork creator
     * @return List of artworks created by the user
     */
    List<Artwork> findByUserUsername(String username);

    /**
     * Search artworks by title (case-insensitive partial match, paginated).
     * 
     * @param title    Title substring to search for
     * @param pageable Pagination parameters
     * @return Page of matching artworks
     */
    Page<Artwork> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Search artworks by location name (case-insensitive partial match, paginated).
     * 
     * @param location Location substring to search for
     * @param pageable Pagination parameters
     * @return Page of matching artworks
     */
    Page<Artwork> findByLocationNameContainingIgnoreCase(String location, Pageable pageable);

    /**
     * Search artworks by tags (case-insensitive partial match, paginated).
     * 
     * @param tag      Tag substring to search for
     * @param pageable Pagination parameters
     * @return Page of matching artworks
     */
    Page<Artwork> findByTagsContainingIgnoreCase(String tag, Pageable pageable);

    /**
     * Get artworks sorted by popularity (total reaction count).
     * Popularity = total number of reactions (AGREE + DISAGREE)
     * 
     * @param pageable Pagination parameters
     * @return Page of artworks sorted by popularity
     */
    @Query("SELECT a FROM Artwork a LEFT JOIN Reaction r ON r.artwork.id = a.id " +
            "GROUP BY a.id ORDER BY COUNT(r) DESC, a.createdAt DESC")
    Page<Artwork> findAllByPopularity(Pageable pageable);

    /**
     * Get artworks sorted by controversy (similar AGREE/DISAGREE counts).
     * Controversial = posts with high engagement but balanced reactions
     * 
     * @param pageable Pagination parameters
     * @return Page of artworks sorted by controversy
     */
    @Query("SELECT a FROM Artwork a LEFT JOIN Reaction r ON r.artwork.id = a.id " +
            "GROUP BY a.id " +
            "HAVING COUNT(r) > 0 " +
            "ORDER BY ABS(SUM(CASE WHEN r.type = 'AGREE' THEN 1 ELSE -1 END)) ASC, COUNT(r) DESC")
    Page<Artwork> findAllByControversy(Pageable pageable);

    /**
     * Count total artworks by a user
     */
    long countByUser(User user);

    /**
     * Count total reactions received on all artworks by a user
     */
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.artwork.user = :user")
    long countTotalReactionsByUser(@Param("user") User user);
}
