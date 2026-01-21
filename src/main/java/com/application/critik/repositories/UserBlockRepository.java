package com.application.critik.repositories;

import com.application.critik.entities.UserBlock;
import com.application.critik.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    /**
     * Find a block relationship between two users
     */
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);

    /**
     * Check if blocker has blocked the blocked user
     */
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    /**
     * Get all users blocked by a user (paginated)
     */
    Page<UserBlock> findByBlockerOrderByCreatedAtDesc(User blocker, Pageable pageable);

    /**
     * Get all users who blocked a specific user
     */
    List<UserBlock> findByBlocked(User blocked);

    /**
     * Delete a block relationship
     */
    void deleteByBlockerAndBlocked(User blocker, User blocked);

    /**
     * Get list of user IDs that the given user has blocked
     */
    @Query("SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker = :user")
    List<Long> findBlockedUserIds(@Param("user") User user);

    /**
     * Get list of user IDs that have blocked the given user
     */
    @Query("SELECT ub.blocker.id FROM UserBlock ub WHERE ub.blocked = :user")
    List<Long> findBlockerUserIds(@Param("user") User user);
}
