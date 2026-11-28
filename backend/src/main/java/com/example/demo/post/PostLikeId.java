package com.example.demo.post;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PostLikeId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "post_id", nullable = false)
    private Long postId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public PostLikeId() {}
    public PostLikeId(Long postId, UUID userId) {
        this.postId = postId;
        this.userId = userId;
    }
    public Long getPostId() { return postId; }
    public UUID getUserId() { return userId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PostLikeId that)) return false;
        return Objects.equals(postId, that.postId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() { return Objects.hash(postId, userId); }
}
