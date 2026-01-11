package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Follow entity representing social follow relationships between users.
 * 
 * Models the follower-followed relationship:
 * - follower: The user who is following
 * - followed: The user being followed
 * 
 * Business rules:
 * - Users cannot follow themselves (enforced in service layer)
 * - Each follow relationship is unique (enforced by database constraint)
 * - Used to create personalized feeds showing artworks from followed users
 */
@Getter
@Setter
@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "followed_id"})
})
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who is following (the follower) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /** The user being followed */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    /** Timestamp when the follow relationship was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
