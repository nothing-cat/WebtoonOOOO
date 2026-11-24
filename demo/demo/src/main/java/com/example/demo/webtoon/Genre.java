package com.example.demo.webtoon;

import jakarta.persistence.*;

@Entity
@Table(name = "genres", schema = "public")
public class Genre {
    @Id
    @Column(columnDefinition = "text")
    private String id;

    @Column(name = "name", nullable = false, unique = true, columnDefinition = "text")
    private String name;

    public Genre() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
