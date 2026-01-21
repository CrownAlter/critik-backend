package com.application.critik.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Artwork entity representing art posts on the Critik platform.
 * 
 * Users can post artworks with:
 * - Title and image
 * - Artist name (original creator of the artwork)
 * - Location information (name and coordinates)
 * - Personal interpretation/description
 * - Tags for categorization and search
 */
@Entity
@Table(name = "artworks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who posted this artwork (not necessarily the artist) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    /**
     * Name of the original artist who created the artwork.
     * This allows users to credit artists when posting their work.
     */
    @Size(max = 200, message = "Artist name cannot exceed 200 characters")
    private String artistName;

    private String imageUrl;

    private Double locationLat;

    private Double locationLon;

    @Size(max = 300, message = "Location name cannot exceed 300 characters")
    private String locationName;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Interpretation cannot exceed 5000 characters")
    private String interpretation;

    /** Comma-separated tags for categorization */
    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Flag indicating if the artwork has been edited after creation */
    @Column(nullable = false)
    @Builder.Default
    private boolean edited = false;

    /** Timestamp of the last edit (null if never edited) */
    private LocalDateTime lastEditedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
