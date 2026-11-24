package com.example.demo.webtoon;

import jakarta.persistence.*;

@Entity
@Table(name = "webtoon_platforms", schema = "public")
public class WebtoonPlatform {
    @EmbeddedId
    private WebtoonPlatformId id;

    @MapsId("webtoonId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "webtoon_id", nullable = false)
    private Webtoon webtoon;

    @MapsId("platformId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    public WebtoonPlatform() {}

    public WebtoonPlatform(Webtoon webtoon, Platform platform) {
        this.webtoon = webtoon;
        this.platform = platform;
        this.id = new WebtoonPlatformId(webtoon.getId(), platform.getId());
    }

    public WebtoonPlatformId getId() { return id; }
    public void setId(WebtoonPlatformId id) { this.id = id; }

    public Webtoon getWebtoon() { return webtoon; }
    public void setWebtoon(Webtoon webtoon) { this.webtoon = webtoon; }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
}
