package com.application.critik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ArtworkRevision entity for tracking edit history of artworks.
 * 
 * Whenever an artwork is edited, the previous state is saved as a revision.
 * This allows users to see the edit history and what changes were made.
 * 
 * Only text fields are tracked (images cannot be changed after upload).
 */
@Entity
@Table(name = "artwork_revisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The artwork that was edited */
    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    /** Previous title before edit */
    @Column(length = 200)
    private String previousTitle;

    /** Previous artist name before edit */
    @Column(length = 200)
    private String previousArtistName;

    /** Previous location name before edit */
    @Column(length = 300)
    private String previousLocationName;

    /** Previous interpretation before edit */
    @Column(columnDefinition = "TEXT")
    private String previousInterpretation;

    /** Previous tags before edit */
    @Column(length = 500)
    private String previousTags;

    /** Timestamp when the edit was made */
    @Column(nullable = false, updatable = false)
    private LocalDateTime editedAt;

    @PrePersist
    public void onCreate() {
        this.editedAt = LocalDateTime.now();
    }
}
