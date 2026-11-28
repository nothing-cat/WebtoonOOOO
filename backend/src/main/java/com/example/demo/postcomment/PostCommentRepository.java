package com.example.demo.postcomment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Page<PostComment> findByPost_IdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    Page<PostComment> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    long countByPost_Id(Long postId);
}
