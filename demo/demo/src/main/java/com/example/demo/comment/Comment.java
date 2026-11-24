package com.example.demo.comment;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import com.example.demo.webtoon.Webtoon;
import com.example.demo.user.User;

@Entity
@Table(name = "comments", schema = "public")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "webtoon_id", nullable = false)
    private Webtoon webtoon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "empathy_count", nullable = false)
    private int empathyCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Comment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Webtoon getWebtoon() { return webtoon; }
    public void setWebtoon(Webtoon webtoon) { this.webtoon = webtoon; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getEmpathyCount() { return empathyCount; }
    public void setEmpathyCount(int empathyCount) { this.empathyCount = empathyCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    void touchUpdatedAt() { updatedAt = OffsetDateTime.now(); }
}
