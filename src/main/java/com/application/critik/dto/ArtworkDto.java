package com.application.critik.dto;

import com.application.critik.entities.ReactionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtworkDto {
    private Long id;
    private UserDto user;
    private String title;
    private String artistName;
    private String imageUrl;
    private Double locationLat;
    private Double locationLon;
    private String locationName;
    private String interpretation;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Flag indicating if the artwork has been edited */
    private boolean edited;

    /** Timestamp of the last edit (null if never edited) */
    private LocalDateTime lastEditedAt;

    // User interaction fields
    private boolean isBookmarked;
    private ReactionType userReaction; // AGREE, DISAGREE, or null

    // Stats
    private long agreeCount;
    private long disagreeCount;
}
