package com.application.critik.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artwork_id")
    @JsonIgnoreProperties({ "user", "comments" }) // Prevent circular reference
    private Artwork artwork;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String commentText;

    /**
     * Self-referential relationship for nested replies.
     * null = top-level comment, non-null = reply to another comment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnoreProperties({ "replies", "parentComment" })
    private Comment parentComment;

    /**
     * Child comments (replies) to this comment.
     * Eagerly fetched to build comment trees.
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({ "parentComment" })
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
