package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bookmark entity representing saved artworks by users.
 * 
 * Users can save artworks to view later in their bookmarks section.
 * Each user can bookmark an artwork only once (enforced by unique constraint).
 */
@Entity
@Table(name = "bookmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "artwork_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who bookmarked the artwork */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The artwork that was bookmarked */
    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    /** Timestamp when the bookmark was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
