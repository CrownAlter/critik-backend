package com.application.critik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for artwork revision (edit history) responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkRevisionDto {

    private Long id;
    private Long artworkId;
    private String previousTitle;
    private String previousArtistName;
    private String previousLocationName;
    private String previousInterpretation;
    private String previousTags;
    private LocalDateTime editedAt;
}
