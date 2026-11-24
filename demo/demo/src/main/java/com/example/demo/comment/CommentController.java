package com.example.demo.comment;
import com.example.demo.auth.SessionUser;
import com.example.demo.common.AccessService;
import com.example.demo.webtoon.WebtoonService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;
@RestController @Transactional
@RequestMapping("/api")
public class CommentController {
    private final CommentRepository comments;private final CommentLikeRepository likes;private final AccessService access;private final WebtoonService webtoons;private final JdbcTemplate jdbc;
    public CommentController(CommentRepository comments,CommentLikeRepository likes,AccessService access,WebtoonService webtoons,JdbcTemplate jdbc){this.comments=comments;this.likes=likes;this.access=access;this.webtoons=webtoons;this.jdbc=jdbc;}
    public record Input(String text){}
    public record Dto(Long id,String webtoonId,UUID userId,String username,String text,long empathyCount,boolean isLiked,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
    private Comment require(Long id){return comments.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"댓글이 없습니다."));}
    private Dto dto(Comment c,UUID u){return new Dto(c.getId(),c.getWebtoon().getId(),c.getUser().getId(),c.getUser().getUsername(),c.getText(),likes.countByComment_Id(c.getId()),u!=null&&likes.existsByComment_IdAndUser_Id(c.getId(),u),c.getCreatedAt(),c.getUpdatedAt());}
    @GetMapping("/webtoons/{id}/comments") @Transactional(readOnly=true) public List<Dto> list(@PathVariable String id,HttpServletRequest r){return comments.findByWebtoon_IdOrderByCreatedAtDesc(id,Pageable.unpaged()).stream().map(c->dto(c,SessionUser.id(r))).toList();}
    @GetMapping("/users/me/comments") @Transactional(readOnly=true) public List<Dto> mine(HttpServletRequest r){var u=access.require(SessionUser.require(r));return comments.findByUser_IdOrderByCreatedAtDesc(u.getId(),Pageable.unpaged()).stream().map(c->dto(c,u.getId())).toList();}
    @PostMapping("/webtoons/{id}/comments") @ResponseStatus(HttpStatus.CREATED) public Dto create(@PathVariable String id,@RequestBody Input in,HttpServletRequest r){var u=access.require(SessionUser.require(r));Comment c=new Comment();c.setUser(u);c.setWebtoon(webtoons.require(id));c.setText(AccessService.nonblank(in.text()));return dto(comments.saveAndFlush(c),u.getId());}
    @PutMapping("/comments/{id}") public Dto update(@PathVariable Long id,@RequestBody Input in,HttpServletRequest r){var u=access.require(SessionUser.require(r));var c=require(id);access.owner(u,c.getUser(),false);c.setText(AccessService.nonblank(in.text()));return dto(comments.saveAndFlush(c),u.getId());}
    @DeleteMapping("/comments/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id,HttpServletRequest r){var c=require(id);access.owner(access.require(SessionUser.require(r)),c.getUser(),true);comments.delete(c);}
    private void toggle(Long id,HttpServletRequest r,boolean add){var u=access.require(SessionUser.require(r));require(id);if(add)jdbc.update("insert into public.comment_likes(comment_id,user_id) values (?,?) on conflict do nothing",id,u.getId());else jdbc.update("delete from public.comment_likes where comment_id=? and user_id=?",id,u.getId());}
    @PostMapping("/comments/{id}/like") @ResponseStatus(HttpStatus.NO_CONTENT) public void like(@PathVariable Long id,HttpServletRequest r){toggle(id,r,true);}
    @DeleteMapping("/comments/{id}/like") @ResponseStatus(HttpStatus.NO_CONTENT) public void unlike(@PathVariable Long id,HttpServletRequest r){toggle(id,r,false);}
}
