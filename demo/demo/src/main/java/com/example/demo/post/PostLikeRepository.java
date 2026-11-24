package com.example.demo.post;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    long countByPost_Id(Long postId);
    boolean existsByPost_IdAndUser_Id(Long postId, UUID userId);
}
