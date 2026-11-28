package com.example.demo.comment;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import com.example.demo.user.User;

@Entity
@Table(name = "comment_likes", schema = "public")
public class CommentLike {
    @EmbeddedId
    private CommentLikeId id;

    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public CommentLike() {}

    public CommentLike(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
        this.id = new CommentLikeId(comment.getId(), user.getId());
    }

    public CommentLikeId getId() { return id; }
    public void setId(CommentLikeId id) { this.id = id; }

    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
