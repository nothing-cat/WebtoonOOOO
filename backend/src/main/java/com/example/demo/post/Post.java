package com.example.demo.post;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import com.example.demo.user.User;
import com.example.demo.webtoon.Webtoon;

@Entity
@Table(name = "posts", schema = "public")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "webtoon_id", nullable = true)
    private Webtoon webtoon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PostCategory category;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Post() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Webtoon getWebtoon() { return webtoon; }
    public void setWebtoon(Webtoon webtoon) { this.webtoon = webtoon; }

    public PostCategory getCategory() { return category; }
    public void setCategory(PostCategory category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    void touchUpdatedAt() { updatedAt = OffsetDateTime.now(); }
}
