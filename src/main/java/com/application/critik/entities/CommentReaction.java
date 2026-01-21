package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CommentReaction entity representing user reactions to comments.
 * 
 * Similar to artwork reactions, users can react to comments with:
 * - AGREE: User agrees with the comment
 * - DISAGREE: User disagrees with the comment
 * 
 * Constraints:
 * - Each user can have only ONE reaction per comment
 * - Reactions can be changed or removed
 * - When comment is deleted, all associated reactions are deleted (cascade)
 */
@Entity
@Table(name = "comment_reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "comment_id", "user_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The comment being reacted to */
    @ManyToOne(optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    /** The user who created the reaction */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Type of reaction (AGREE or DISAGREE) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    /** Timestamp when the reaction was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
