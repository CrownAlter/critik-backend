package com.application.critik.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment entity for discussions under artwork posts.
 * 
 * Supports nested replies through self-referential parent-child relationship.
 * - parentComment: null for top-level comments, references parent for replies
 * - replies: list of child comments (replies to this comment)
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Comment entity with nested reply support")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Comment ID", example = "1")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_id")
    @JsonIgnoreProperties({"user", "comments"}) // Prevent circular reference
    @Schema(description = "Artwork being commented on")
    private Artwork artwork;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @Schema(description = "User who wrote the comment")
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    @Schema(description = "Comment text content", example = "Great artwork! Love the colors.", required = true)
    private String commentText;

    /**
     * Self-referential relationship for nested replies.
     * null = top-level comment, non-null = reply to another comment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnoreProperties({"replies", "parentComment"})
    @Schema(description = "Parent comment (null for top-level comments)")
    private Comment parentComment;

    /**
     * Child comments (replies) to this comment.
     * Eagerly fetched to build comment trees.
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"parentComment"})
    @Builder.Default
    @Schema(description = "List of replies to this comment")
    private List<Comment> replies = new ArrayList<>();

    @Schema(description = "Creation timestamp", example = "2025-01-11T10:30:00")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
