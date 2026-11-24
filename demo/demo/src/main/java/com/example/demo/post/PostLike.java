package com.example.demo.post;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import com.example.demo.user.User;

@Entity
@Table(name = "post_likes", schema = "public")
public class PostLike {
    @EmbeddedId
    private PostLikeId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public PostLike() {}

    public PostLike(Post post, User user) {
        this.post = post;
        this.user = user;
        this.id = new PostLikeId(post.getId(), user.getId());
    }

    public PostLikeId getId() { return id; }
    public void setId(PostLikeId id) { this.id = id; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
