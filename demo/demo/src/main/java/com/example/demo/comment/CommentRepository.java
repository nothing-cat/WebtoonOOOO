package com.example.demo.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByWebtoon_IdOrderByCreatedAtDesc(String webtoonId, Pageable pageable);
    Page<Comment> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
