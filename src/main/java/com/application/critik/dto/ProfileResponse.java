package com.application.critik.dto;

import com.application.critik.entities.Artwork;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {
    private UserDto user;
    private List<Artwork> artworks;
    private Boolean isFollowing;

    /** User statistics (followers, following, artworks, reactions) */
    private UserStatsDto stats;
}
