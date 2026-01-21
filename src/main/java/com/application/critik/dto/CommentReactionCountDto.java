package com.application.critik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for comment reaction counts.
 * Returns the count of each reaction type for a comment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReactionCountDto {

    /** Count of AGREE reactions */
    private Long agree;

    /** Count of DISAGREE reactions */
    private Long disagree;

    /** Total number of reactions */
    private Long total;

    /**
     * Create from a map of reaction counts
     */
    public static CommentReactionCountDto fromMap(Map<String, Long> counts) {
        long agree = counts.getOrDefault("AGREE", 0L);
        long disagree = counts.getOrDefault("DISAGREE", 0L);

        return CommentReactionCountDto.builder()
                .agree(agree)
                .disagree(disagree)
                .total(agree + disagree)
                .build();
    }
}
