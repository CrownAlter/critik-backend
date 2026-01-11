package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Reaction entity representing user reactions to artworks.
 * 
 * Users can react to artworks with predefined reaction types:
 * - AGREE: User agrees with the interpretation
 * - DISAGREE: User disagrees with the interpretation
 * 
 * Constraints:
 * - Each user can have only ONE reaction per artwork
 * - Reactions can be changed or removed
 * - When artwork is deleted, all associated reactions are deleted (cascade)
 */
@Entity
@Table(name = "reactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The artwork being reacted to */
    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_id")
    private Artwork artwork;

    /** The user who created the reaction */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** Type of reaction (AGREE or DISAGREE) */
    @Enumerated(EnumType.STRING)
    private ReactionType type;

    /** Timestamp when the reaction was created */
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}

