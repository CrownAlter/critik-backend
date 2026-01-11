package com.application.critik.entities;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Artwork entity")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Artwork ID", example = "1")
    private Long id;

    /** The user who posted this artwork (not necessarily the artist) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "User who posted the artwork")
    private User user;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Schema(description = "Artwork title", example = "Sunset Over Mountains", required = true)
    private String title;

    /** 
     * Name of the original artist who created the artwork.
     * This allows users to credit artists when posting their work.
     */
    @Size(max = 200, message = "Artist name cannot exceed 200 characters")
    @Schema(description = "Name of the original artist", example = "Vincent van Gogh")
    private String artistName;

    @Schema(description = "Image URL path", example = "/uploads/artwork123.jpg")
    private String imageUrl;

    @Schema(description = "Latitude coordinate", example = "48.8566")
    private Double locationLat;

    @Schema(description = "Longitude coordinate", example = "2.3522")
    private Double locationLon;

    @Size(max = 300, message = "Location name cannot exceed 300 characters")
    @Schema(description = "Location name", example = "Louvre Museum, Paris")
    private String locationName;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Interpretation cannot exceed 5000 characters")
    @Schema(description = "User's interpretation or description", example = "This piece captures the essence of...")
    private String interpretation;

    /** Comma-separated tags for categorization */
    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    @Schema(description = "Comma-separated tags", example = "impressionism,landscape,oil-painting")
    private String tags;

    @Schema(description = "Creation timestamp", example = "2025-01-11T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-01-11T15:45:00")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
