package com.application.critik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile statistics.
 * Contains computed counts for followers, following, artworks, and total
 * reactions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDto {

    /** Number of users following this user */
    private Long followerCount;

    /** Number of users this user is following */
    private Long followingCount;

    /** Number of artworks posted by this user */
    private Long artworkCount;

    /** Total number of reactions received on all artworks */
    private Long totalReactions;
}
