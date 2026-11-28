package com.example.demo.webtoon;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WebtoonPlatformId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "webtoon_id", nullable = false, columnDefinition = "text")
    private String webtoonId;
    @Column(name = "platform_id", nullable = false, columnDefinition = "text")
    private String platformId;

    public WebtoonPlatformId() {}
    public WebtoonPlatformId(String webtoonId, String platformId) {
        this.webtoonId = webtoonId;
        this.platformId = platformId;
    }
    public String getWebtoonId() { return webtoonId; }
    public String getPlatformId() { return platformId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof WebtoonPlatformId that)) return false;
        return Objects.equals(webtoonId, that.webtoonId) && Objects.equals(platformId, that.platformId);
    }

    @Override
    public int hashCode() { return Objects.hash(webtoonId, platformId); }
}
