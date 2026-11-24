package com.example.demo.favorite;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import com.example.demo.user.User;
import com.example.demo.webtoon.Webtoon;

@Entity
@Table(name = "favorite_webtoons", schema = "public")
public class FavoriteWebtoon {
    @EmbeddedId
    private FavoriteWebtoonId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("webtoonId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "webtoon_id", nullable = false)
    private Webtoon webtoon;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "last_clicked_at", nullable = false)
    private OffsetDateTime lastClickedAt = OffsetDateTime.now();

    public FavoriteWebtoon() {}

    public FavoriteWebtoon(User user, Webtoon webtoon) {
        this.user = user;
        this.webtoon = webtoon;
        this.id = new FavoriteWebtoonId(user.getId(), webtoon.getId());
    }

    public FavoriteWebtoonId getId() { return id; }
    public void setId(FavoriteWebtoonId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Webtoon getWebtoon() { return webtoon; }
    public void setWebtoon(Webtoon webtoon) { this.webtoon = webtoon; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getLastClickedAt() { return lastClickedAt; }
    public void setLastClickedAt(OffsetDateTime lastClickedAt) { this.lastClickedAt = lastClickedAt; }
}
