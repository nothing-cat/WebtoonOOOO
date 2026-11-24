package com.example.demo.post;
import com.example.demo.common.AccessService;
import com.example.demo.user.*;
import com.example.demo.webtoon.WebtoonService;
import com.example.demo.postcomment.PostCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;
@Service @Transactional
public class PostService {
    private final PostRepository posts; private final PostLikeRepository likes; private final PostCommentRepository comments; private final AccessService access; private final WebtoonService webtoons; private final JdbcTemplate jdbc;
    public PostService(PostRepository posts,PostLikeRepository likes,PostCommentRepository comments,AccessService access,WebtoonService webtoons,JdbcTemplate jdbc){this.posts=posts;this.likes=likes;this.comments=comments;this.access=access;this.webtoons=webtoons;this.jdbc=jdbc;}
    public record PostDto(Long id,UUID userId,String username,String webtoonId,PostCategory category,String title,String content,long viewCount,long likeCount,long commentCount,boolean isLiked,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
    public record Input(PostCategory category,String title,String content,String webtoonId){}
    public Post require(Long id){return posts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"게시글이 없습니다."));}
    public PostDto dto(Post p,UUID u){return new PostDto(p.getId(),p.getUser().getId(),p.getUser().getUsername(),p.getWebtoon()==null?null:p.getWebtoon().getId(),p.getCategory(),p.getTitle(),p.getContent(),p.getViewCount(),likes.countByPost_Id(p.getId()),comments.countByPost_Id(p.getId()),u!=null&&likes.existsByPost_IdAndUser_Id(p.getId(),u),p.getCreatedAt(),p.getUpdatedAt());}
    @Transactional(readOnly=true) public PostDto get(Long id,UUID u){return dto(require(id),u);}
    @Transactional(readOnly=true) public List<PostDto> list(PostCategory category,boolean popular,UUID u){
        var rows=category==null?posts.findAll():posts.findByCategoryOrderByCreatedAtDesc(category,Pageable.unpaged()).getContent();
        var order=popular?Comparator.comparingLong(PostDto::likeCount).reversed().thenComparing(PostDto::id,Comparator.reverseOrder()):Comparator.comparing((PostDto p)->p.category()!=PostCategory.NOTICE).thenComparing(PostDto::id,Comparator.reverseOrder());
        return rows.stream().map(p->dto(p,u)).sorted(order).toList();
    }
    @Transactional(readOnly=true) public List<PostDto> reviews(String id,UUID u){return posts.findByWebtoon_IdAndCategoryOrderByCreatedAtDesc(id,PostCategory.REVIEW).stream().map(p->dto(p,u)).toList();}
    @Transactional(readOnly=true) public List<PostDto> mine(UUID u){access.require(u);return posts.findByUser_IdOrderByCreatedAtDesc(u,Pageable.unpaged()).stream().map(p->dto(p,u)).toList();}
    private void apply(Post p,Input in,User u){if(in.category()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"게시판을 선택하세요.");if(in.category()==PostCategory.NOTICE&&u.getRole()!=UserRole.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"관리자만 공지를 작성할 수 있습니다.");p.setCategory(in.category());p.setTitle(AccessService.nonblank(in.title()));p.setContent(AccessService.nonblank(in.content()));p.setWebtoon(in.category()==PostCategory.REVIEW?webtoons.require(AccessService.nonblank(in.webtoonId())):null);}
    public PostDto create(Input in,UUID id){var u=access.require(id);Post p=new Post();p.setUser(u);apply(p,in,u);return dto(posts.saveAndFlush(p),id);}
    public PostDto update(Long id,Input in,UUID actor){var u=access.require(actor);var p=require(id);access.owner(u,p.getUser(),false);apply(p,in,u);return dto(posts.saveAndFlush(p),actor);}
    public void delete(Long id,UUID actor){var p=require(id);access.owner(access.require(actor),p.getUser(),true);posts.delete(p);}
    public void like(Long id,UUID actor,boolean add){access.require(actor);require(id);if(add)jdbc.update("insert into public.post_likes(post_id,user_id) values (?,?) on conflict do nothing",id,actor);else jdbc.update("delete from public.post_likes where post_id=? and user_id=?",id,actor);}
    public void visit(Long id,UUID visitId){if(visitId==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"방문 ID가 필요합니다.");require(id);int inserted=jdbc.update("insert into public.post_views(post_id,visit_id) values (?,?) on conflict do nothing",id,visitId);if(inserted==1)posts.incrementViewCount(id);}
}
