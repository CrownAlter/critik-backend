package com.application.critik.dto;

import com.application.critik.entities.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for comment reaction responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReactionDto {

    private Long id;
    private Long commentId;
    private Long userId;
    private String username;
    private ReactionType type;
    private LocalDateTime createdAt;
}
