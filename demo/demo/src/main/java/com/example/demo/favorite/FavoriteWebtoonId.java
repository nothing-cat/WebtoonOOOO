package com.example.demo.favorite;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FavoriteWebtoonId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "webtoon_id", nullable = false, columnDefinition = "text")
    private String webtoonId;

    public FavoriteWebtoonId() {}
    public FavoriteWebtoonId(UUID userId, String webtoonId) {
        this.userId = userId;
        this.webtoonId = webtoonId;
    }
    public UUID getUserId() { return userId; }
    public String getWebtoonId() { return webtoonId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof FavoriteWebtoonId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(webtoonId, that.webtoonId);
    }

    @Override
    public int hashCode() { return Objects.hash(userId, webtoonId); }
}
