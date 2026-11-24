package com.example.demo.comment;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CommentLikeId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "comment_id", nullable = false)
    private Long commentId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public CommentLikeId() {}
    public CommentLikeId(Long commentId, UUID userId) {
        this.commentId = commentId;
        this.userId = userId;
    }
    public Long getCommentId() { return commentId; }
    public UUID getUserId() { return userId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CommentLikeId that)) return false;
        return Objects.equals(commentId, that.commentId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() { return Objects.hash(commentId, userId); }
}
