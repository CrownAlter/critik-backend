package com.application.critik.repositories;

import com.application.critik.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by exact username match (case-sensitive).
     * @param username Username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by username ignoring case.
     * @param username Username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Find a user by email address.
     * @param email Email address to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username already exists (for duplicate prevention).
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists (for duplicate prevention).
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Search users by partial username match (case-insensitive).
     * @param username Username substring to search for
     * @return List of users matching the query
     */
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    /**
     * Search users by partial display name match (case-insensitive).
     * @param displayName Display name substring to search for
     * @return List of users matching the query
     */
    List<User> findByDisplayNameContainingIgnoreCase(String displayName);
}
