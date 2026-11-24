package com.example.demo.postcomment;
import com.example.demo.auth.SessionUser;
import com.example.demo.common.AccessService;
import com.example.demo.post.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;
@RestController @Transactional
@RequestMapping("/api")
public class PostCommentController {
    private final PostCommentRepository comments;private final AccessService access;private final PostService posts;
    public PostCommentController(PostCommentRepository comments,AccessService access,PostService posts){this.comments=comments;this.access=access;this.posts=posts;}
    public record Input(String content){}
    public record Dto(Long id,Long postId,UUID userId,String username,String content,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
    private PostComment require(Long id){return comments.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"댓글이 없습니다."));}
    private Dto dto(PostComment c){return new Dto(c.getId(),c.getPost().getId(),c.getUser().getId(),c.getUser().getUsername(),c.getContent(),c.getCreatedAt(),c.getUpdatedAt());}
    @GetMapping("/posts/{id}/comments") @Transactional(readOnly=true) public List<Dto> list(@PathVariable Long id){return comments.findByPost_IdOrderByCreatedAtDesc(id,Pageable.unpaged()).stream().map(this::dto).toList();}
    @GetMapping("/users/me/post-comments") @Transactional(readOnly=true) public List<Dto> mine(HttpServletRequest r){var u=access.require(SessionUser.require(r));return comments.findByUser_IdOrderByCreatedAtDesc(u.getId(),Pageable.unpaged()).stream().map(this::dto).toList();}
    @PostMapping("/posts/{id}/comments") @ResponseStatus(HttpStatus.CREATED) public Dto create(@PathVariable Long id,@RequestBody Input in,HttpServletRequest r){var u=access.require(SessionUser.require(r));var c=new PostComment();c.setUser(u);c.setPost(posts.require(id));c.setContent(AccessService.nonblank(in.content()));return dto(comments.saveAndFlush(c));}
    @PutMapping("/post-comments/{id}") public Dto update(@PathVariable Long id,@RequestBody Input in,HttpServletRequest r){var u=access.require(SessionUser.require(r));var c=require(id);access.owner(u,c.getUser(),false);c.setContent(AccessService.nonblank(in.content()));return dto(comments.saveAndFlush(c));}
    @DeleteMapping("/post-comments/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id,HttpServletRequest r){var c=require(id);access.owner(access.require(SessionUser.require(r)),c.getUser(),true);comments.delete(c);}
}
