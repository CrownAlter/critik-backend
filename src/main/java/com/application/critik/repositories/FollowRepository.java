package com.application.critik.repositories;

import com.application.critik.entities.Follow;
import com.application.critik.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Follow entity database operations.
 * Manages the social follow relationships between users.
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * Check if a follow relationship exists between two users by their IDs.
     * 
     * @param followerId ID of the user who is following
     * @param followedId ID of the user being followed
     * @return true if follow relationship exists, false otherwise
     */
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Delete a follow relationship (unfollow).
     * 
     * @param follower The user who is following
     * @param followed The user being followed
     */
    void deleteByFollowerAndFollowed(User follower, User followed);

    /**
     * Find all users that a given user follows.
     * 
     * @param follower The user who is following others
     * @return List of Follow relationships where user is the follower
     */
    List<Follow> findByFollower(User follower);

    /**
     * Find all users who follow a given user.
     * 
     * @param followed The user being followed
     * @return List of Follow relationships where user is being followed
     */
    List<Follow> findByFollowed(User followed);

    /**
     * Get IDs of all users that a given user follows.
     * Used for efficient personalized feed queries.
     * 
     * @param userId ID of the user
     * @return List of user IDs that the user follows
     */
    @Query("SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId")
    List<Long> findFollowedUserIds(Long userId);
}
