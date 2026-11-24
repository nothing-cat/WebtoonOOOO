package com.example.demo.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);
    Page<Post> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Post> findByWebtoon_IdAndCategoryOrderByCreatedAtDesc(String webtoonId, PostCategory category);

    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
