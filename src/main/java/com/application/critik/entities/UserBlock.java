package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * UserBlock entity representing user blocking relationships.
 * 
 * When a user blocks another user:
 * - The blocked user cannot follow the blocker
 * - The blocked user cannot comment on the blocker's posts
 * - The blocked user's content is hidden from the blocker's feed
 * - The blocker cannot see the blocked user's profile or content
 * 
 * Business rules:
 * - Users cannot block themselves (enforced in service layer)
 * - Each block relationship is unique (enforced by database constraint)
 */
@Entity
@Table(name = "user_blocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "blocker_id", "blocked_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who initiated the block */
    @ManyToOne(optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    /** The user who was blocked */
    @ManyToOne(optional = false)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    /** Timestamp when the block was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
