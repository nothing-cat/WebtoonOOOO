package com.example.demo.webtoon;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "webtoons", schema = "public")
public class Webtoon {
    @Id
    @Column(columnDefinition = "text")
    private String id;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "picture_writer", columnDefinition = "text")
    private String pictureWriter;

    @Column(name = "story_writer", columnDefinition = "text")
    private String storyWriter;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "thumbnail_url", columnDefinition = "text")
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "genre_id", nullable = true)
    private Genre genre;

    @Column(name = "age_rating", columnDefinition = "text")
    private String ageRating;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Webtoon() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPictureWriter() { return pictureWriter; }
    public void setPictureWriter(String pictureWriter) { this.pictureWriter = pictureWriter; }

    public String getStoryWriter() { return storyWriter; }
    public void setStoryWriter(String storyWriter) { this.storyWriter = storyWriter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    void touchUpdatedAt() { updatedAt = OffsetDateTime.now(); }
}
