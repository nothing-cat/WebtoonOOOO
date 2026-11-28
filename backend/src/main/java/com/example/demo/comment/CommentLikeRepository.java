package com.example.demo.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    long countByComment_Id(Long commentId);
    boolean existsByComment_IdAndUser_Id(Long commentId, UUID userId);
}
