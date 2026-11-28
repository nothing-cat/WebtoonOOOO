package com.example.demo.webtoon;

import jakarta.persistence.*;

@Entity
@Table(name = "platforms", schema = "public")
public class Platform {
    @Id
    @Column(columnDefinition = "text")
    private String id;

    @Column(name = "name", nullable = false, unique = true, columnDefinition = "text")
    private String name;

    public Platform() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
